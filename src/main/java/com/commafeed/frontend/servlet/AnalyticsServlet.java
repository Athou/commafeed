package com.commafeed.frontend.servlet;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.commafeed.CommaFeedConfiguration;

@SuppressWarnings("serial")
@Singleton
public class AnalyticsServlet extends HttpServlet {

	private final CommaFeedConfiguration config;
	private final String script;

	@Inject
	public AnalyticsServlet(CommaFeedConfiguration config) {
		this.config = config;

		// @formatter:off
		this.script = "(function(i, s, o, g, r, a, m) {" + 
				"i['GoogleAnalyticsObject'] = r;" + 
				"i[r] = i[r] || function() {" + 
				"(i[r].q = i[r].q || []).push(arguments)" + 
				"}, i[r].l = 1 * new Date();" + 
				"a = s.createElement(o), m = s.getElementsByTagName(o)[0];" + 
				"a.async = 1;" + 
				"a.src = g;" + 
				"m.parentNode.insertBefore(a, m)" + 
				"})(window, document, 'script', '//www.google-analytics.com/analytics.js', 'ga');" + 
			
				"ga('create', '" + config.getApplicationSettings().getGoogleAnalyticsTrackingCode() + "');" + 
				"ga('send', 'pageview');";
		// @formatter:on
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/javascript");
		if (StringUtils.isNotBlank(config.getApplicationSettings().getGoogleAnalyticsTrackingCode())) {
			resp.getWriter().write(script);
		}
	}

}
