package com.commafeed.frontend.pages;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.UrlRenderer;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.commafeed.backend.dao.ApplicationSettingsService;
import com.commafeed.backend.dao.UserService;
import com.commafeed.backend.feeds.OPMLImporter;
import com.commafeed.backend.model.ApplicationSettings;
import com.commafeed.frontend.utils.WicketUtils;
import com.commafeed.frontend.utils.exception.DisplayException;
import com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl;
import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

@SuppressWarnings("serial")
public class GoogleImportCallbackPage extends WebPage {

	@Inject
	ApplicationSettingsService applicationSettingsService;

	@Inject
	OPMLImporter importer;
	@Inject
	UserService userService;

	private static final String TOKEN_URL = "https://accounts.google.com/o/oauth2/token";
	private static final String EXPORT_URL = "https://www.google.com/reader/subscriptions/export";

	public static String getCallbackUrl() {
		RequestCycle cycle = RequestCycle.get();
		UrlRenderer renderer = cycle.getUrlRenderer();
		return renderer.renderFullUrl(Url.parse(cycle.urlFor(
				GoogleImportCallbackPage.class, null).toString()));
	}

	public GoogleImportCallbackPage(PageParameters params) {

		HttpServletRequest request = WicketUtils.getHttpServletRequest();
		StringBuffer urlBuffer = request.getRequestURL();
		if (request.getQueryString() != null) {
			urlBuffer.append('?').append(request.getQueryString());
		}
		AuthorizationCodeResponseUrl responseUrl = new AuthorizationCodeResponseUrl(
				urlBuffer.toString());
		String code = responseUrl.getCode();

		if (responseUrl.getError() != null) {
			throw new DisplayException(responseUrl.getError());
		} else if (code == null) {
			throw new DisplayException("Missing authorization code");
		} else {
			ApplicationSettings settings = applicationSettingsService.get();
			String redirectUri = getCallbackUrl();
			String clientId = settings.getGoogleClientId();
			String clientSecret = settings.getGoogleClientSecret();

			HttpTransport httpTransport = new NetHttpTransport();
			JacksonFactory jsonFactory = new JacksonFactory();

			AuthorizationCodeTokenRequest tokenRequest = new AuthorizationCodeTokenRequest(
					httpTransport, jsonFactory, new GenericUrl(TOKEN_URL), code);
			tokenRequest.setRedirectUri(redirectUri);
			tokenRequest.put("client_id", clientId);
			tokenRequest.put("client_secret", clientSecret);
			tokenRequest.setGrantType("authorization_code");

			try {
				TokenResponse tokenResponse = tokenRequest.execute();
				String accessToken = tokenResponse.getAccessToken();

				HttpRequest httpRequest = httpTransport.createRequestFactory()
						.buildGetRequest(new GenericUrl(EXPORT_URL));
				BearerToken.authorizationHeaderAccessMethod().intercept(
						httpRequest, accessToken);
				String opml = httpRequest.execute().parseAsString();
				String state = responseUrl.getState();
				importer.importOpml(userService.findById(Long.valueOf(state)),
						opml);
			} catch (Exception e) {
				throw new DisplayException(e);
			}
		}
		setResponsePage(getApplication().getHomePage());
	}
}
