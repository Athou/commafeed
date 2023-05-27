package com.commafeed;

import java.io.IOException;
import java.util.Date;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.DispatcherType;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.ServerEndpointConfig;

import org.hibernate.cfg.AvailableSettings;

import com.codahale.metrics.json.MetricsModule;
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
import com.commafeed.backend.service.DatabaseStartupService;
import com.commafeed.backend.service.UserService;
import com.commafeed.backend.task.ScheduledTask;
import com.commafeed.frontend.auth.SecurityCheckFactoryProvider;
import com.commafeed.frontend.resource.AdminREST;
import com.commafeed.frontend.resource.CategoryREST;
import com.commafeed.frontend.resource.EntryREST;
import com.commafeed.frontend.resource.FeedREST;
import com.commafeed.frontend.resource.PubSubHubbubCallbackREST;
import com.commafeed.frontend.resource.ServerREST;
import com.commafeed.frontend.resource.UserREST;
import com.commafeed.frontend.servlet.AnalyticsServlet;
import com.commafeed.frontend.servlet.CustomCssServlet;
import com.commafeed.frontend.servlet.CustomJsServlet;
import com.commafeed.frontend.servlet.LogoutServlet;
import com.commafeed.frontend.servlet.NextUnreadServlet;
import com.commafeed.frontend.session.SessionHelperFactoryProvider;
import com.commafeed.frontend.ws.WebSocketConfigurator;
import com.commafeed.frontend.ws.WebSocketEndpoint;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

import be.tomcools.dropwizard.websocket.WebsocketBundle;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.configuration.DefaultConfigurationFactoryFactory;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.servlets.CacheBustingFilter;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.web.WebBundle;
import io.dropwizard.web.conf.WebConfiguration;
import io.whitfin.dropwizard.configuration.EnvironmentSubstitutor;

public class CommaFeedApplication extends Application<CommaFeedConfiguration> {

	public static final String USERNAME_ADMIN = "admin";
	public static final String USERNAME_DEMO = "demo";

	public static final Date STARTUP_TIME = new Date();

	private HibernateBundle<CommaFeedConfiguration> hibernateBundle;
	private WebsocketBundle<CommaFeedConfiguration> websocketBundle;

	@Override
	public String getName() {
		return "CommaFeed";
	}

	@Override
	public void initialize(Bootstrap<CommaFeedConfiguration> bootstrap) {
		bootstrap.setConfigurationFactoryFactory(new DefaultConfigurationFactoryFactory<CommaFeedConfiguration>() {
			@Override
			protected ObjectMapper configureObjectMapper(ObjectMapper objectMapper) {
				// disable case sensitivity because EnvironmentSubstitutor maps MYPROPERTY to myproperty and not to myProperty
				return objectMapper
						.setConfig(objectMapper.getDeserializationConfig().with(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES));
			}
		});

		// enable config.yml string substitution
		// e.g. having a custom config.yml file with app.session.path=${SOME_ENV_VAR} will substitute SOME_ENV_VAR
		SubstitutingSourceProvider substitutingSourceProvider = new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
				new EnvironmentVariableSubstitutor(false));
		// enable config.yml properties override with env variables prefixed with CF_
		// e.g. setting CF_APP_ALLOWREGISTRATIONS=true will set app.allowRegistrations to true
		EnvironmentSubstitutor environmentSubstitutor = new EnvironmentSubstitutor("CF", substitutingSourceProvider);
		bootstrap.setConfigurationSourceProvider(environmentSubstitutor);

		bootstrap.getObjectMapper().registerModule(new MetricsModule(TimeUnit.SECONDS, TimeUnit.SECONDS, false));

		bootstrap.addBundle(websocketBundle = new WebsocketBundle<>());
		bootstrap.addBundle(hibernateBundle = new HibernateBundle<CommaFeedConfiguration>(AbstractModel.class, Feed.class,
				FeedCategory.class, FeedEntry.class, FeedEntryContent.class, FeedEntryStatus.class, FeedEntryTag.class,
				FeedSubscription.class, User.class, UserRole.class, UserSettings.class) {
			@Override
			public DataSourceFactory getDataSourceFactory(CommaFeedConfiguration configuration) {
				DataSourceFactory factory = configuration.getDataSourceFactory();

				// keep using old id generator for backward compatibility
				factory.getProperties().put(AvailableSettings.USE_NEW_ID_GENERATOR_MAPPINGS, "false");

				factory.getProperties().put(AvailableSettings.STATEMENT_BATCH_SIZE, "50");
				factory.getProperties().put(AvailableSettings.BATCH_VERSIONED_DATA, "true");
				factory.getProperties().put(AvailableSettings.ORDER_INSERTS, "true");
				factory.getProperties().put(AvailableSettings.ORDER_UPDATES, "true");
				return factory;
			}
		});

		bootstrap.addBundle(new WebBundle<CommaFeedConfiguration>() {
			@Override
			public WebConfiguration getWebConfiguration(CommaFeedConfiguration configuration) {
				WebConfiguration config = new WebConfiguration();
				config.getFrameOptionsHeaderFactory().setEnabled(true);
				return config;
			}
		});

		bootstrap.addBundle(new MigrationsBundle<CommaFeedConfiguration>() {
			@Override
			public DataSourceFactory getDataSourceFactory(CommaFeedConfiguration configuration) {
				return configuration.getDataSourceFactory();
			}
		});

		bootstrap.addBundle(new AssetsBundle("/assets/", "/", "index.html"));
		bootstrap.addBundle(new MultiPartBundle());
	}

	@Override
	public void run(CommaFeedConfiguration config, Environment environment) throws Exception {
		// guice init
		Injector injector = Guice.createInjector(new CommaFeedModule(hibernateBundle.getSessionFactory(), config, environment.metrics()));

		// session management
		environment.servlets().setSessionHandler(config.getSessionHandlerFactory().build());

		// support for "@SecurityCheck User user" injection
		environment.jersey().register(new SecurityCheckFactoryProvider.Binder(injector.getInstance(UserService.class)));
		// support for "@Context SessionHelper sessionHelper" injection
		environment.jersey().register(new SessionHelperFactoryProvider.Binder());

		// REST resources
		environment.jersey().setUrlPattern("/rest/*");
		environment.jersey().register(injector.getInstance(AdminREST.class));
		environment.jersey().register(injector.getInstance(CategoryREST.class));
		environment.jersey().register(injector.getInstance(EntryREST.class));
		environment.jersey().register(injector.getInstance(FeedREST.class));
		environment.jersey().register(injector.getInstance(PubSubHubbubCallbackREST.class));
		environment.jersey().register(injector.getInstance(ServerREST.class));
		environment.jersey().register(injector.getInstance(UserREST.class));

		// Servlets
		environment.servlets().addServlet("next", injector.getInstance(NextUnreadServlet.class)).addMapping("/next");
		environment.servlets().addServlet("logout", injector.getInstance(LogoutServlet.class)).addMapping("/logout");
		environment.servlets().addServlet("customCss", injector.getInstance(CustomCssServlet.class)).addMapping("/custom_css.css");
		environment.servlets().addServlet("customJs", injector.getInstance(CustomJsServlet.class)).addMapping("/custom_js.js");
		environment.servlets().addServlet("analytics.js", injector.getInstance(AnalyticsServlet.class)).addMapping("/analytics.js");

		// WebSocket endpoint
		ServerEndpointConfig serverEndpointConfig = ServerEndpointConfig.Builder.create(WebSocketEndpoint.class, "/ws")
				.configurator(injector.getInstance(WebSocketConfigurator.class))
				.build();
		websocketBundle.addEndpoint(serverEndpointConfig);

		// Scheduled tasks
		Set<ScheduledTask> tasks = injector.getInstance(Key.get(new TypeLiteral<Set<ScheduledTask>>() {
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
