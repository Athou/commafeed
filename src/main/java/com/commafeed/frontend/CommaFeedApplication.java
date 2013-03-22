package com.commafeed.frontend;

import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.authroles.authentication.AbstractAuthenticatedWebSession;
import org.apache.wicket.authroles.authentication.AuthenticatedWebApplication;
import org.apache.wicket.authroles.authorization.strategies.role.IRoleCheckingStrategy;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AnnotationsRoleAuthorizationStrategy;
import org.apache.wicket.cdi.CdiConfiguration;
import org.apache.wicket.cdi.ConversationPropagation;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler.RedirectPolicy;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.frontend.components.auth.LoginPage;
import com.commafeed.frontend.components.auth.LogoutPage;
import com.commafeed.frontend.pages.home.HomePage;
import com.commafeed.frontend.rest.FeedSubscriptionsREST;
import com.commafeed.frontend.utils.exception.DisplayExceptionPage;

public class CommaFeedApplication extends AuthenticatedWebApplication {

	private Logger log = LoggerFactory.getLogger(CommaFeedApplication.class);

	@Override
	protected void init() {
		super.init();

		mountPage("login", LoginPage.class);
		mountPage("logout", LogoutPage.class);
		mountPage("error", DisplayExceptionPage.class);
		
		mountPage("subscriptions", FeedSubscriptionsREST.class);

		setupInjection();

		getMarkupSettings().setStripWicketTags(true);
		getMarkupSettings().setCompressWhitespace(true);
		getMarkupSettings().setDefaultMarkupEncoding("UTF-8");

		getSecuritySettings().setAuthorizationStrategy(
				new AnnotationsRoleAuthorizationStrategy(
						new IRoleCheckingStrategy() {
							@Override
							public boolean hasAnyRole(Roles roles) {
								return CommaFeedSession.get().getRoles()
										.hasAnyRole(roles);
							}
						}));

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

}
