package com.commafeed.frontend;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;

@WebFilter(urlPatterns = "/*")
public class InterceptingFilter implements Filter {

	private static final String HEADER_CORS = "Access-Control-Allow-Origin";
	private static final String HEADER_CORS_VALUE = "*";
	private static final String HEADER_CORS_METHODS = "Access-Control-Allow-Methods";
	private static final String HEADER_CORS_METHODS_VALUE = "POST, GET, OPTIONS";
	private static final String HEADER_CORS_MAXAGE = "Access-Control-Max-Age";
	private static final String HEADER_CORS_MAXAGE_VALUE = "2592000";
	private static final String HEADER_CORS_ALLOW_HEADERS = "Access-Control-Allow-Headers";
	private static final String HEADER_CORS_ALLOW_HEADERS_VALUE = "Authorization";
	private static final String HEADER_CORS_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
	private static final String HEADER_CORS_ALLOW_CREDENTIALS_VALUE = "true";

	private static final String HEADER_X_UA_COMPATIBLE = "X-UA-Compatible";
	private static final String HEADER_X_UA_COMPATIBLE_VALUE = "IE=Edge,chrome=1";

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletResponse resp = (HttpServletResponse) response;
		resp.addHeader(HEADER_CORS, HEADER_CORS_VALUE);
		resp.addHeader(HEADER_CORS_METHODS, HEADER_CORS_METHODS_VALUE);
		resp.addHeader(HEADER_CORS_MAXAGE, HEADER_CORS_MAXAGE_VALUE);
		resp.addHeader(HEADER_CORS_ALLOW_HEADERS, HEADER_CORS_ALLOW_HEADERS_VALUE);
		resp.addHeader(HEADER_CORS_ALLOW_CREDENTIALS, HEADER_CORS_ALLOW_CREDENTIALS_VALUE);

		resp.addHeader(HEADER_X_UA_COMPATIBLE, HEADER_X_UA_COMPATIBLE_VALUE);

		chain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void destroy() {
	}

}
