package com.commafeed.frontend;

import java.util.ResourceBundle;

import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.Cookie;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.authentication.strategy.DefaultAuthenticationStrategy;
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
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.filter.JavaScriptFilteredIntoFooterHeaderResponse;
import org.apache.wicket.markup.html.IHeaderResponseDecorator;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.cookies.CookieUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.frontend.pages.DemoLoginPage;
import com.commafeed.frontend.pages.HomePage;
import com.commafeed.frontend.pages.LogoutPage;
import com.commafeed.frontend.pages.NextUnreadRedirectPage;
import com.commafeed.frontend.pages.PasswordRecoveryCallbackPage;
import com.commafeed.frontend.pages.PasswordRecoveryPage;
import com.commafeed.frontend.pages.WelcomePage;
import com.commafeed.frontend.utils.exception.DisplayExceptionPage;

public class CommaFeedApplication extends AuthenticatedWebApplication {

	private static Logger log = LoggerFactory
			.getLogger(CommaFeedApplication.class);

	public CommaFeedApplication() {
		super();
		String prod = ResourceBundle.getBundle("application").getString(
				"production");
		setConfigurationType(Boolean.valueOf(prod) ? RuntimeConfigurationType.DEPLOYMENT
				: RuntimeConfigurationType.DEVELOPMENT);
	}

	@Override
	protected void init() {
		super.init();

		mountPage("welcome", WelcomePage.class);
		mountPage("demo", DemoLoginPage.class);
		
		mountPage("recover", PasswordRecoveryPage.class);
		mountPage("recover2", PasswordRecoveryCallbackPage.class);
		
		mountPage("logout", LogoutPage.class);
		mountPage("error", DisplayExceptionPage.class);
		
//		mountPage("google/import/redirect", GoogleImportRedirectPage.class);
//		mountPage(GoogleImportCallbackPage.PAGE_PATH,
//				GoogleImportCallbackPage.class);

		mountPage("next", NextUnreadRedirectPage.class);

		setupInjection();
		setupSecurity();

		getMarkupSettings().setStripWicketTags(true);
		getMarkupSettings().setCompressWhitespace(true);
		getMarkupSettings().setDefaultMarkupEncoding("UTF-8");

		setHeaderResponseDecorator(new IHeaderResponseDecorator() {
			@Override
			public IHeaderResponse decorate(IHeaderResponse response) {
				return new JavaScriptFilteredIntoFooterHeaderResponse(response,
						"footer-container");
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
	}

	private void setupSecurity() {
		getSecuritySettings().setAuthenticationStrategy(
				new DefaultAuthenticationStrategy("LoggedIn") {

					private CookieUtils cookieUtils = null;

					@Override
					protected CookieUtils getCookieUtils() {

						if (cookieUtils == null) {
							cookieUtils = new CookieUtils() {
								@Override
								protected void initializeCookie(Cookie cookie) {
									super.initializeCookie(cookie);
									cookie.setHttpOnly(true);
								}
							};
						}
						return cookieUtils;
					}
				});
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
		return WelcomePage.class;
	}

	@Override
	protected Class<? extends AbstractAuthenticatedWebSession> getWebSessionClass() {
		return CommaFeedSession.class;
	}

	public static CommaFeedApplication get() {

		return (CommaFeedApplication) Application.get();
	}
}
