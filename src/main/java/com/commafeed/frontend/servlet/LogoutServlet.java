package com.commafeed.frontend.servlet;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import com.commafeed.CommaFeedConfiguration;

@SuppressWarnings("serial")
@RequiredArgsConstructor(onConstructor = @__({ @Inject }))
@Singleton
public class LogoutServlet extends HttpServlet {

	private final CommaFeedConfiguration config;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.getSession().invalidate();
		resp.sendRedirect(resp.encodeRedirectURL(config.getApplicationSettings().getPublicUrl()));
	}
}
