package com.commafeed;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.jersey.sessions.HttpSessionProvider;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.util.Date;

import org.eclipse.jetty.server.session.SessionHandler;

import com.commafeed.backend.feed.FeedRefreshTaskGiver;
import com.commafeed.backend.feed.FeedRefreshUpdater;
import com.commafeed.backend.feed.FeedRefreshWorker;
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
import com.commafeed.backend.service.StartupService;
import com.commafeed.backend.service.UserService;
import com.commafeed.backend.task.OldStatusesCleanupTask;
import com.commafeed.backend.task.OrphansCleanupTask;
import com.commafeed.backend.task.SchedulingService;
import com.commafeed.frontend.auth.SecurityCheckProvider;
import com.commafeed.frontend.auth.SecurityCheckProvider.SecurityCheckUserServiceProvider;
import com.commafeed.frontend.resource.AdminREST;
import com.commafeed.frontend.resource.CategoryREST;
import com.commafeed.frontend.resource.EntryREST;
import com.commafeed.frontend.resource.FeedREST;
import com.commafeed.frontend.resource.PubSubHubbubCallbackREST;
import com.commafeed.frontend.resource.ServerREST;
import com.commafeed.frontend.resource.UserREST;
import com.commafeed.frontend.servlet.AnalyticsServlet;
import com.commafeed.frontend.servlet.CustomCssServlet;
import com.commafeed.frontend.servlet.LogoutServlet;
import com.commafeed.frontend.servlet.NextUnreadServlet;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.wordnik.swagger.config.ConfigFactory;
import com.wordnik.swagger.config.ScannerFactory;
import com.wordnik.swagger.config.SwaggerConfig;
import com.wordnik.swagger.jaxrs.config.DefaultJaxrsScanner;
import com.wordnik.swagger.jaxrs.listing.ApiDeclarationProvider;
import com.wordnik.swagger.jaxrs.listing.ApiListingResourceJSON;
import com.wordnik.swagger.jaxrs.listing.ResourceListingProvider;
import com.wordnik.swagger.jaxrs.reader.DefaultJaxrsApiReader;
import com.wordnik.swagger.reader.ClassReaders;

public class CommaFeedApplication extends Application<CommaFeedConfiguration> {

	public static final String USERNAME_ADMIN = "admin";
	public static final String USERNAME_DEMO = "demo";

	public static final Date STARTUP_TIME = new Date();

	private HibernateBundle<CommaFeedConfiguration> hibernateBundle;

	@Override
	public void initialize(Bootstrap<CommaFeedConfiguration> bootstrap) {
		bootstrap.addBundle(hibernateBundle = new HibernateBundle<CommaFeedConfiguration>(AbstractModel.class, Feed.class,
				FeedCategory.class, FeedEntry.class, FeedEntryContent.class, FeedEntryStatus.class, FeedEntryTag.class,
				FeedSubscription.class, User.class, UserRole.class, UserSettings.class) {
			@Override
			public DataSourceFactory getDataSourceFactory(CommaFeedConfiguration configuration) {
				return configuration.getDatabase();
			}
		});

		bootstrap.addBundle(new MigrationsBundle<CommaFeedConfiguration>() {
			@Override
			public DataSourceFactory getDataSourceFactory(CommaFeedConfiguration configuration) {
				return configuration.getDatabase();
			}
		});

		bootstrap.addBundle(new AssetsBundle("/assets/", "/", "index.html"));
	}

	@Override
	public void run(CommaFeedConfiguration config, Environment environment) throws Exception {
		// configure context path
		environment.getApplicationContext().setContextPath(config.getApplicationSettings().getContextPath());

		// guice init
		Injector injector = Guice.createInjector(new CommaFeedModule(hibernateBundle.getSessionFactory(), config, environment.metrics()));

		// Auth/session management
		environment.servlets().setSessionHandler(new SessionHandler(config.getSessionManagerFactory().build()));
		environment.jersey().register(new SecurityCheckUserServiceProvider(injector.getInstance(UserService.class)));
		environment.jersey().register(SecurityCheckProvider.class);
		environment.jersey().register(HttpSessionProvider.class);

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
		environment.servlets().addServlet("analytics.js", injector.getInstance(AnalyticsServlet.class)).addMapping("/analytics.js");

		// Scheduled tasks
		SchedulingService schedulingService = new SchedulingService();
		schedulingService.register(injector.getInstance(OldStatusesCleanupTask.class));
		schedulingService.register(injector.getInstance(OrphansCleanupTask.class));
		environment.lifecycle().manage(schedulingService);

		// database init/changelogs
		environment.lifecycle().manage(injector.getInstance(StartupService.class));

		// background feed fetching
		environment.lifecycle().manage(injector.getInstance(FeedRefreshTaskGiver.class));
		environment.lifecycle().manage(injector.getInstance(FeedRefreshWorker.class));
		environment.lifecycle().manage(injector.getInstance(FeedRefreshUpdater.class));

		// Swagger
		environment.jersey().register(new ApiListingResourceJSON());
		environment.jersey().register(new ApiDeclarationProvider());
		environment.jersey().register(new ResourceListingProvider());
		ScannerFactory.setScanner(new DefaultJaxrsScanner());
		ClassReaders.setReader(new DefaultJaxrsApiReader());
		SwaggerConfig swaggerConfig = ConfigFactory.config();
		swaggerConfig.setApiVersion("1");
		swaggerConfig.setBasePath("/rest");
	}

	public static void main(String[] args) throws Exception {
		new CommaFeedApplication().run(args);
	}
}
