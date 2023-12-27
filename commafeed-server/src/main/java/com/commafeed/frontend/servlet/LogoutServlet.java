package com.commafeed.frontend.servlet;

import java.io.IOException;

import com.commafeed.CommaFeedConfiguration;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@SuppressWarnings("serial")
@RequiredArgsConstructor(onConstructor = @__({ @Inject }))
@Singleton
public class LogoutServlet extends HttpServlet {

	private final CommaFeedConfiguration config;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		req.getSession().invalidate();
		resp.sendRedirect(resp.encodeRedirectURL(config.getApplicationSettings().getPublicUrl()));
	}
}
