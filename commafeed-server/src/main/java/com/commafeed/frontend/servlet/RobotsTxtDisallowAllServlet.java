package com.commafeed.frontend.servlet;

import java.io.IOException;

import jakarta.inject.Singleton;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Singleton
public class RobotsTxtDisallowAllServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("text/plain");
		resp.getWriter().write("User-agent: *");
		resp.getWriter().write("\n");
		resp.getWriter().write("Disallow: /");
	}
}
