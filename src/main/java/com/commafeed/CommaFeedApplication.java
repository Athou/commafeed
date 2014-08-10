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

import lombok.extern.slf4j.Slf4j;

import org.eclipse.jetty.server.session.SessionHandler;
import org.hibernate.SessionFactory;

import com.codahale.metrics.MetricRegistry;
import com.commafeed.CommaFeedConfiguration.CacheType;
import com.commafeed.backend.HttpGetter;
import com.commafeed.backend.cache.CacheService;
import com.commafeed.backend.cache.NoopCacheService;
import com.commafeed.backend.cache.RedisCacheService;
import com.commafeed.backend.dao.FeedCategoryDAO;
import com.commafeed.backend.dao.FeedDAO;
import com.commafeed.backend.dao.FeedEntryContentDAO;
import com.commafeed.backend.dao.FeedEntryDAO;
import com.commafeed.backend.dao.FeedEntryStatusDAO;
import com.commafeed.backend.dao.FeedEntryTagDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.dao.UserDAO;
import com.commafeed.backend.dao.UserRoleDAO;
import com.commafeed.backend.dao.UserSettingsDAO;
import com.commafeed.backend.feed.FaviconFetcher;
import com.commafeed.backend.feed.FeedFetcher;
import com.commafeed.backend.feed.FeedParser;
import com.commafeed.backend.feed.FeedQueues;
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
import com.commafeed.backend.opml.OPMLExporter;
import com.commafeed.backend.opml.OPMLImporter;
import com.commafeed.backend.service.ApplicationPropertiesService;
import com.commafeed.backend.service.DatabaseCleaningService;
import com.commafeed.backend.service.FeedEntryContentService;
import com.commafeed.backend.service.FeedEntryService;
import com.commafeed.backend.service.FeedEntryTagService;
import com.commafeed.backend.service.FeedService;
import com.commafeed.backend.service.FeedSubscriptionService;
import com.commafeed.backend.service.FeedUpdateService;
import com.commafeed.backend.service.PasswordEncryptionService;
import com.commafeed.backend.service.PubSubService;
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
import com.commafeed.frontend.servlet.LogoutServlet;
import com.commafeed.frontend.servlet.NextUnreadServlet;

@Slf4j
public class CommaFeedApplication extends Application<CommaFeedConfiguration> {

	public static final String USERNAME_ADMIN = "admin";
	public static final String USERNAME_DEMO = "demo";

	public static final String SESSION_USER = "user";

	public static final Date STARTUP_TIME = new Date();

	private HibernateBundle<CommaFeedConfiguration> hibernateBundle;
	private MigrationsBundle<CommaFeedConfiguration> migrationsBundle;

	@Override
	public void initialize(Bootstrap<CommaFeedConfiguration> bootstrap) {
		hibernateBundle = new HibernateBundle<CommaFeedConfiguration>(AbstractModel.class, Feed.class, FeedCategory.class, FeedEntry.class,
				FeedEntryContent.class, FeedEntryStatus.class, FeedEntryTag.class, FeedSubscription.class, User.class, UserRole.class,
				UserSettings.class) {
			@Override
			public DataSourceFactory getDataSourceFactory(CommaFeedConfiguration configuration) {
				return configuration.getDatabase();
			}
		};
		bootstrap.addBundle(hibernateBundle);

		migrationsBundle = new MigrationsBundle<CommaFeedConfiguration>() {
			@Override
			public DataSourceFactory getDataSourceFactory(CommaFeedConfiguration configuration) {
				return configuration.getDatabase();
			}
		};
		bootstrap.addBundle(migrationsBundle);

		bootstrap.addBundle(new AssetsBundle("/assets/", "/", "index.html"));
	}

	@Override
	public void run(CommaFeedConfiguration config, Environment environment) throws Exception {
		MetricRegistry metrics = environment.metrics();
		SessionFactory sessionFactory = hibernateBundle.getSessionFactory();

		CacheService cacheService = config.getApplicationSettings().getCache() == CacheType.NOOP ? new NoopCacheService()
				: new RedisCacheService();
		log.info("using cache {}", cacheService.getClass());

		// DAOs
		FeedCategoryDAO feedCategoryDAO = new FeedCategoryDAO(sessionFactory);
		FeedDAO feedDAO = new FeedDAO(sessionFactory);
		FeedEntryContentDAO feedEntryContentDAO = new FeedEntryContentDAO(sessionFactory);
		FeedEntryDAO feedEntryDAO = new FeedEntryDAO(sessionFactory);
		FeedEntryTagDAO feedEntryTagDAO = new FeedEntryTagDAO(sessionFactory);
		FeedSubscriptionDAO feedSubscriptionDAO = new FeedSubscriptionDAO(sessionFactory);
		UserDAO userDAO = new UserDAO(sessionFactory);
		UserRoleDAO userRoleDAO = new UserRoleDAO(sessionFactory);
		UserSettingsDAO userSettingsDAO = new UserSettingsDAO(sessionFactory);
		FeedEntryStatusDAO feedEntryStatusDAO = new FeedEntryStatusDAO(sessionFactory, feedEntryDAO, feedEntryTagDAO, config);

		// Queuing system
		FeedQueues queues = new FeedQueues(feedDAO, config, metrics);

		// Services
		ApplicationPropertiesService applicationPropertiesService = new ApplicationPropertiesService();
		DatabaseCleaningService cleaningService = new DatabaseCleaningService(feedDAO, feedEntryDAO, feedEntryContentDAO,
				feedEntryStatusDAO, feedSubscriptionDAO);
		FeedEntryContentService feedEntryContentService = new FeedEntryContentService(feedEntryContentDAO);
		FeedEntryService feedEntryService = new FeedEntryService(feedSubscriptionDAO, feedEntryDAO, feedEntryStatusDAO, cacheService);
		FeedEntryTagService feedEntryTagService = new FeedEntryTagService(feedEntryDAO, feedEntryTagDAO);
		FeedService feedService = new FeedService(feedDAO);
		FeedSubscriptionService feedSubscriptionService = new FeedSubscriptionService(feedEntryStatusDAO, feedSubscriptionDAO, feedService,
				queues, cacheService, config);
		FeedUpdateService feedUpdateService = new FeedUpdateService(feedEntryDAO, feedEntryContentService);
		PasswordEncryptionService encryptionService = new PasswordEncryptionService();
		PubSubService pubSubService = new PubSubService(config, queues);
		UserService userService = new UserService(feedCategoryDAO, userDAO, userSettingsDAO, feedSubscriptionService, encryptionService,
				config);
		StartupService startupService = new StartupService(sessionFactory, userDAO, userService);
		OPMLImporter opmlImporter = new OPMLImporter(feedCategoryDAO, feedSubscriptionService, cacheService);
		OPMLExporter opmlExporter = new OPMLExporter(feedCategoryDAO, feedSubscriptionDAO);

		// Feed fetching/parsing
		HttpGetter httpGetter = new HttpGetter();
		FeedParser feedParser = new FeedParser();
		FaviconFetcher faviconFetcher = new FaviconFetcher(httpGetter);
		FeedFetcher feedFetcher = new FeedFetcher(feedParser, httpGetter);
		FeedRefreshUpdater feedUpdater = new FeedRefreshUpdater(sessionFactory, feedUpdateService, pubSubService, queues, config, metrics,
				feedSubscriptionDAO, cacheService);
		FeedRefreshWorker feedWorker = new FeedRefreshWorker(feedUpdater, feedFetcher, queues, config, metrics);
		FeedRefreshTaskGiver taskGiver = new FeedRefreshTaskGiver(sessionFactory, queues, feedDAO, feedWorker, config, metrics);

		// Auth/session management

		environment.servlets().setSessionHandler(new SessionHandler());
		environment.jersey().register(new SecurityCheckUserServiceProvider(userService));
		environment.jersey().register(SecurityCheckProvider.class);
		environment.jersey().register(HttpSessionProvider.class);

		// REST resources
		environment.jersey().setUrlPattern("/rest/*");
		environment.jersey()
				.register(new AdminREST(userDAO, userRoleDAO, userService, encryptionService, cleaningService, config, metrics));
		environment.jersey().register(
				new CategoryREST(feedCategoryDAO, feedEntryStatusDAO, feedSubscriptionDAO, feedEntryService, feedSubscriptionService,
						cacheService, config));
		environment.jersey().register(new EntryREST(feedEntryTagDAO, feedEntryService, feedEntryTagService));
		environment.jersey().register(
				new FeedREST(feedSubscriptionDAO, feedCategoryDAO, feedEntryStatusDAO, faviconFetcher, feedFetcher, feedEntryService,
						feedSubscriptionService, queues, opmlImporter, opmlExporter, cacheService, config));
		environment.jersey().register(new PubSubHubbubCallbackREST(feedDAO, feedParser, queues, config, metrics));
		environment.jersey().register(new ServerREST(httpGetter, config, applicationPropertiesService));
		environment.jersey().register(new UserREST(userDAO, userRoleDAO, userSettingsDAO, userService, encryptionService));

		// Servlets
		NextUnreadServlet nextUnreadServlet = new NextUnreadServlet(sessionFactory, feedSubscriptionDAO, feedEntryStatusDAO,
				feedCategoryDAO, config);
		LogoutServlet logoutServlet = new LogoutServlet(config);
		environment.servlets().addServlet("next", nextUnreadServlet).addMapping("/next");
		environment.servlets().addServlet("logout", logoutServlet).addMapping("/logout");

		// Tasks
		SchedulingService schedulingService = new SchedulingService();
		schedulingService.register(new OldStatusesCleanupTask(sessionFactory, config, cleaningService));
		schedulingService.register(new OrphansCleanupTask(sessionFactory, cleaningService));

		// Managed objects
		environment.lifecycle().manage(startupService);
		environment.lifecycle().manage(taskGiver);
		environment.lifecycle().manage(feedWorker);
		environment.lifecycle().manage(feedUpdater);
		environment.lifecycle().manage(schedulingService);

		// TODO swagger ui
	}

	public static void main(String[] args) throws Exception {
		new CommaFeedApplication().run(args);
	}
}
