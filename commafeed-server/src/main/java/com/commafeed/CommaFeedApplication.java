package com.commafeed;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer;
import org.hibernate.cfg.AvailableSettings;

import com.codahale.metrics.json.MetricsModule;
import com.commafeed.backend.dao.UserDAO;
import com.commafeed.backend.feed.FeedRefreshEngine;
import com.commafeed.backend.model.AbstractModel;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryContent;
import com.commafeed.backend.model.FeedEntryStatus;
import com.commafeed.backend.model.FeedEntryTag;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserRole;
import com.commafeed.backend.model.UserSettings;
import com.commafeed.backend.service.UserService;
import com.commafeed.backend.service.db.DatabaseStartupService;
import com.commafeed.backend.service.db.H2MigrationService;
import com.commafeed.backend.task.ScheduledTask;
import com.commafeed.frontend.auth.PasswordConstraintValidator;
import com.commafeed.frontend.auth.SecurityCheckFactoryProvider;
import com.commafeed.frontend.resource.AdminREST;
import com.commafeed.frontend.resource.CategoryREST;
import com.commafeed.frontend.resource.EntryREST;
import com.commafeed.frontend.resource.FeedREST;
import com.commafeed.frontend.resource.ServerREST;
import com.commafeed.frontend.resource.UserREST;
import com.commafeed.frontend.resource.fever.FeverREST;
import com.commafeed.frontend.servlet.CustomCssServlet;
import com.commafeed.frontend.servlet.CustomJsServlet;
import com.commafeed.frontend.servlet.LogoutServlet;
import com.commafeed.frontend.servlet.NextUnreadServlet;
import com.commafeed.frontend.servlet.RobotsTxtDisallowAllServlet;
import com.commafeed.frontend.session.SessionHelperFactoryProvider;
import com.commafeed.frontend.ws.WebSocketConfigurator;
import com.commafeed.frontend.ws.WebSocketEndpoint;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.configuration.DefaultConfigurationFactoryFactory;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.core.Application;
import io.dropwizard.core.ConfiguredBundle;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.servlets.CacheBustingFilter;
import io.whitfin.dropwizard.configuration.EnvironmentSubstitutor;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.websocket.server.ServerEndpointConfig;

public class CommaFeedApplication extends Application<CommaFeedConfiguration> {

	public static final String USERNAME_ADMIN = "admin";
	public static final String USERNAME_DEMO = "demo";

	public static final Instant STARTUP_TIME = Instant.now();

	private HibernateBundle<CommaFeedConfiguration> hibernateBundle;

	@Override
	public String getName() {
		return "CommaFeed";
	}

	@Override
	public void initialize(Bootstrap<CommaFeedConfiguration> bootstrap) {
		configureEnvironmentSubstitutor(bootstrap);
		configureObjectMapper(bootstrap.getObjectMapper());

		// run h2 migration as the first bundle because we need to migrate before hibernate is initialized
		bootstrap.addBundle(new ConfiguredBundle<>() {
			@Override
			public void run(CommaFeedConfiguration config, Environment environment) {
				DataSourceFactory dataSourceFactory = config.getDataSourceFactory();
				String url = dataSourceFactory.getUrl();
				if (isFileBasedH2(url)) {
					Path path = getFilePath(url);
					String user = dataSourceFactory.getUser();
					String password = dataSourceFactory.getPassword();
					new H2MigrationService().migrateIfNeeded(path, user, password);
				}
			}

			private boolean isFileBasedH2(String url) {
				return url.startsWith("jdbc:h2:") && !url.startsWith("jdbc:h2:mem:");
			}

			private Path getFilePath(String url) {
				String name = url.substring("jdbc:h2:".length()).split(";")[0];
				return Paths.get(name + ".mv.db");
			}
		});

		bootstrap.addBundle(hibernateBundle = new HibernateBundle<>(AbstractModel.class, Feed.class, FeedCategory.class, FeedEntry.class,
				FeedEntryContent.class, FeedEntryStatus.class, FeedEntryTag.class, FeedSubscription.class, User.class, UserRole.class,
				UserSettings.class) {
			@Override
			public DataSourceFactory getDataSourceFactory(CommaFeedConfiguration configuration) {
				DataSourceFactory factory = configuration.getDataSourceFactory();

				factory.getProperties().put(AvailableSettings.PREFERRED_POOLED_OPTIMIZER, "pooled-lo");

				factory.getProperties().put(AvailableSettings.STATEMENT_BATCH_SIZE, "50");
				factory.getProperties().put(AvailableSettings.BATCH_VERSIONED_DATA, "true");
				factory.getProperties().put(AvailableSettings.ORDER_INSERTS, "true");
				factory.getProperties().put(AvailableSettings.ORDER_UPDATES, "true");
				return factory;
			}
		});

		bootstrap.addBundle(new MigrationsBundle<>() {
			@Override
			public DataSourceFactory getDataSourceFactory(CommaFeedConfiguration configuration) {
				return configuration.getDataSourceFactory();
			}
		});

		bootstrap.addBundle(new AssetsBundle("/assets/", "/", "index.html"));
		bootstrap.addBundle(new MultiPartBundle());
	}

	private static void configureEnvironmentSubstitutor(Bootstrap<CommaFeedConfiguration> bootstrap) {
		bootstrap.setConfigurationFactoryFactory(new DefaultConfigurationFactoryFactory<>() {
			@Override
			protected ObjectMapper configureObjectMapper(ObjectMapper objectMapper) {
				// disable case sensitivity because EnvironmentSubstitutor maps MYPROPERTY to myproperty and not to myProperty
				return objectMapper
						.setConfig(objectMapper.getDeserializationConfig().with(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES));
			}
		});

		bootstrap.setConfigurationSourceProvider(buildEnvironmentSubstitutor(bootstrap));
	}

	private static void configureObjectMapper(ObjectMapper objectMapper) {
		// read and write instants as milliseconds instead of nanoseconds
		objectMapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
				.configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);

		// add support for serializing metrics
		objectMapper.registerModule(new MetricsModule(TimeUnit.SECONDS, TimeUnit.SECONDS, false));
	}

	private static EnvironmentSubstitutor buildEnvironmentSubstitutor(Bootstrap<CommaFeedConfiguration> bootstrap) {
		// enable config.yml string substitution
		// e.g. having a custom config.yml file with app.session.path=${SOME_ENV_VAR} will substitute SOME_ENV_VAR
		SubstitutingSourceProvider substitutingSourceProvider = new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
				new EnvironmentVariableSubstitutor(false));

		// enable config.yml properties override with env variables prefixed with CF_
		// e.g. setting CF_APP_ALLOWREGISTRATIONS=true will set app.allowRegistrations to true
		return new EnvironmentSubstitutor("CF", substitutingSourceProvider);
	}

	@Override
	public void run(CommaFeedConfiguration config, Environment environment) {
		PasswordConstraintValidator.setStrict(config.getApplicationSettings().getStrictPasswordPolicy());

		// guice init
		Injector injector = Guice.createInjector(new CommaFeedModule(hibernateBundle.getSessionFactory(), config, environment.metrics()));

		// session management
		environment.servlets().setSessionHandler(config.getSessionHandlerFactory().build(config.getDataSourceFactory()));

		// support for "@SecurityCheck User user" injection
		environment.jersey()
				.register(new SecurityCheckFactoryProvider.Binder(injector.getInstance(UserDAO.class),
						injector.getInstance(UserService.class), config));
		// support for "@Context SessionHelper sessionHelper" injection
		environment.jersey().register(new SessionHelperFactoryProvider.Binder());

		// REST resources
		environment.jersey().setUrlPattern("/rest/*");
		environment.jersey().register(injector.getInstance(AdminREST.class));
		environment.jersey().register(injector.getInstance(CategoryREST.class));
		environment.jersey().register(injector.getInstance(EntryREST.class));
		environment.jersey().register(injector.getInstance(FeedREST.class));
		environment.jersey().register(injector.getInstance(ServerREST.class));
		environment.jersey().register(injector.getInstance(UserREST.class));
		environment.jersey().register(injector.getInstance(FeverREST.class));

		// Servlets
		environment.servlets().addServlet("next", injector.getInstance(NextUnreadServlet.class)).addMapping("/next");
		environment.servlets().addServlet("logout", injector.getInstance(LogoutServlet.class)).addMapping("/logout");
		environment.servlets().addServlet("customCss", injector.getInstance(CustomCssServlet.class)).addMapping("/custom_css.css");
		environment.servlets().addServlet("customJs", injector.getInstance(CustomJsServlet.class)).addMapping("/custom_js.js");
		if (Boolean.TRUE.equals(config.getApplicationSettings().getHideFromWebCrawlers())) {
			environment.servlets()
					.addServlet("robots.txt", injector.getInstance(RobotsTxtDisallowAllServlet.class))
					.addMapping("/robots.txt");
		}

		// WebSocket endpoint
		JakartaWebSocketServletContainerInitializer.configure(environment.getApplicationContext(), (context, container) -> {
			container.setDefaultMaxSessionIdleTimeout(config.getApplicationSettings().getWebsocketPingInterval().toMilliseconds() + 10000);

			container.addEndpoint(ServerEndpointConfig.Builder.create(WebSocketEndpoint.class, "/ws")
					.configurator(injector.getInstance(WebSocketConfigurator.class))
					.build());
		});

		// Scheduled tasks
		Set<ScheduledTask> tasks = injector.getInstance(Key.get(new TypeLiteral<>() {
		}));
		ScheduledExecutorService executor = environment.lifecycle()
				.scheduledExecutorService("task-scheduler", true)
				.threads(tasks.size())
				.build();
		for (ScheduledTask task : tasks) {
			task.register(executor);
		}

		// database init/changelogs
		environment.lifecycle().manage(injector.getInstance(DatabaseStartupService.class));

		// start feed fetching engine
		environment.lifecycle().manage(injector.getInstance(FeedRefreshEngine.class));

		// prevent caching index.html, so that the webapp is always up to date
		environment.servlets()
				.addFilter("index-cache-busting-filter", new CacheBustingFilter())
				.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/");

		// prevent caching openapi files, so that the documentation is always up to date
		environment.servlets()
				.addFilter("openapi-cache-busting-filter", new CacheBustingFilter())
				.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/openapi.json", "/openapi.yaml");

		// prevent caching REST resources, except for favicons
		environment.servlets().addFilter("rest-cache-busting-filter", new CacheBustingFilter() {
			@Override
			public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
				String path = ((HttpServletRequest) request).getRequestURI();
				if (path.contains("/feed/favicon")) {
					chain.doFilter(request, response);
				} else {
					super.doFilter(request, response, chain);
				}
			}
		}).addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/rest/*");

	}

	public static void main(String[] args) throws Exception {
		new CommaFeedApplication().run(args);
	}
}
