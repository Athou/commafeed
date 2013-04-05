package com.commafeed.frontend;

import java.io.IOException;
import java.net.URL;
import java.util.jar.JarFile;

import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.authroles.authentication.AbstractAuthenticatedWebSession;
import org.apache.wicket.authroles.authentication.AuthenticatedWebApplication;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.cdi.CdiConfiguration;
import org.apache.wicket.cdi.ConversationPropagation;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler.RedirectPolicy;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.jboss.vfs.VirtualFile;
import org.reflections.ReflectionsException;
import org.reflections.vfs.SystemDir;
import org.reflections.vfs.Vfs;
import org.reflections.vfs.ZipDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.frontend.pages.FaviconPage;
import com.commafeed.frontend.pages.GoogleImportCallbackPage;
import com.commafeed.frontend.pages.GoogleImportRedirectPage;
import com.commafeed.frontend.pages.HomePage;
import com.commafeed.frontend.pages.LoginPage;
import com.commafeed.frontend.pages.LogoutPage;
import com.commafeed.frontend.utils.exception.DisplayExceptionPage;

import de.agilecoders.wicket.Bootstrap;
import de.agilecoders.wicket.settings.BootstrapSettings;

public class CommaFeedApplication extends AuthenticatedWebApplication {

	private Logger log = LoggerFactory.getLogger(CommaFeedApplication.class);

	@Override
	protected void init() {
		super.init();

		mountPage("login", LoginPage.class);
		mountPage("logout", LogoutPage.class);
		mountPage("error", DisplayExceptionPage.class);
		mountPage("favicon", FaviconPage.class);
		mountPage("google/import/redirect", GoogleImportRedirectPage.class);
		mountPage("google/import/callback", GoogleImportCallbackPage.class);

		setupInjection();

		getMarkupSettings().setStripWicketTags(true);
		getMarkupSettings().setCompressWhitespace(true);
		getMarkupSettings().setDefaultMarkupEncoding("UTF-8");

		getSecuritySettings().setAuthorizationStrategy(
				new IAuthorizationStrategy() {

					@Override
					public <T extends IRequestableComponent> boolean isInstantiationAuthorized(
							Class<T> componentClass) {
						boolean authorized = true;

						boolean restricted = componentClass
								.isAnnotationPresent(SecurityCheck.class);
						if (restricted) {
							SecurityCheck annotation = componentClass
									.getAnnotation(SecurityCheck.class);
							Roles roles = CommaFeedSession.get().getRoles();
							authorized = roles.hasAnyRole(new Roles(annotation
									.value().name()));
						}
						return authorized;
					}

					@Override
					public boolean isActionAuthorized(Component component,
							Action action) {
						return true;
					}
				});

		getRequestCycleListeners().add(new AbstractRequestCycleListener() {
			@Override
			public IRequestHandler onException(RequestCycle cycle, Exception ex) {
				AjaxRequestTarget target = cycle.find(AjaxRequestTarget.class);
				// redirect to the error page if ajax request, render error on
				// current page otherwise
				RedirectPolicy policy = target == null ? RedirectPolicy.NEVER_REDIRECT
						: RedirectPolicy.AUTO_REDIRECT;
				return new RenderPageRequestHandler(new PageProvider(
						new DisplayExceptionPage(ex)), policy);
			}
		});
		Bootstrap.install(Application.get(), new BootstrapSettings());
	}

	@Override
	public Class<? extends Page> getHomePage() {
		return HomePage.class;
	}

	protected void setupInjection() {
		try {
			BeanManager beanManager = (BeanManager) new InitialContext()
					.lookup("java:comp/BeanManager");
			new CdiConfiguration(beanManager).setPropagation(
					ConversationPropagation.NONE).configure(this);
		} catch (NamingException e) {
			log.warn("Could not locate bean manager. CDI is disabled.");
		}
	}

	@Override
	public Session newSession(Request request, Response response) {
		return new CommaFeedSession(request);
	}

	@Override
	protected Class<? extends WebPage> getSignInPageClass() {
		return LoginPage.class;
	}

	@Override
	protected Class<? extends AbstractAuthenticatedWebSession> getWebSessionClass() {
		return CommaFeedSession.class;
	}

	public static CommaFeedApplication get() {

		return (CommaFeedApplication) Application.get();
	}

	/**
	 * Reflections fix for JbossAS7
	 * 
	 * https://code.google.com/p/reflections/wiki/JBossIntegration
	 */
	static {

		Vfs.addDefaultURLTypes(new Vfs.UrlType() {
			public boolean matches(URL url) {
				return url.getProtocol().equals("vfs");
			}

			public Vfs.Dir createDir(URL url) {
				VirtualFile content;
				try {
					content = (VirtualFile) url.openConnection().getContent();
				} catch (Throwable e) {
					throw new ReflectionsException(
							"could not open url connection as VirtualFile ["
									+ url + "]", e);
				}

				Vfs.Dir dir = null;
				try {
					dir = createDir(new java.io.File(content.getPhysicalFile()
							.getParentFile(), content.getName()));
				} catch (IOException e) { /* continue */
				}
				if (dir == null) {
					try {
						dir = createDir(content.getPhysicalFile());
					} catch (IOException e) { /* continue */
					}
				}
				return dir;
			}

			Vfs.Dir createDir(java.io.File file) {
				try {
					return file.exists() && file.canRead() ? file.isDirectory() ? new SystemDir(
							file) : new ZipDir(new JarFile(file))
							: null;
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}
		});
	}

}
