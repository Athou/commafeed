package com.commafeed.frontend.utils;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.ajax.WicketEventJQueryResourceReference;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.template.PackageTextTemplate;

public class WicketUtils {

	public static void loadJQuery(IHeaderResponse response) {
		response.render(JavaScriptHeaderItem
				.forReference(WicketEventJQueryResourceReference.get()));
	}

	public static JavaScriptHeaderItem buildJavaScriptHeaderItem(Class<?> klass) {
		return JavaScriptHeaderItem
				.forReference(new JavaScriptResourceReference(klass, klass
						.getSimpleName() + ".js"));
	}

	public static void loadJS(IHeaderResponse response, Class<?> klass) {
		response.render(buildJavaScriptHeaderItem(klass));
	}

	public static void loadJS(IHeaderResponse response, Class<?> klass,
			Map<String, ? extends Object> variables) {
		OnDomReadyHeaderItem result = null;
		PackageTextTemplate template = null;
		try {
			template = new PackageTextTemplate(klass, klass.getSimpleName()
					+ ".js");
			String script = template.asString(variables);
			result = OnDomReadyHeaderItem.forScript(script);
		} finally {
			IOUtils.closeQuietly(template);
		}
		response.render(result);
	}

	public static CssHeaderItem buildCssHeaderItem(Class<?> klass) {
		return CssHeaderItem.forReference(new CssResourceReference(klass, klass
				.getSimpleName() + ".css"));
	}

	public static void loadCSS(IHeaderResponse response, Class<?> klass) {
		response.render(buildCssHeaderItem(klass));
	}

	public static void loadCSS(IHeaderResponse response, Class<?> klass,
			Map<String, ? extends Object> variables) {
		CssHeaderItem result = null;
		PackageTextTemplate template = null;
		try {
			template = new PackageTextTemplate(klass, klass.getSimpleName()
					+ ".js");
			String css = template.asString(variables);
			result = CssHeaderItem.forCSS(css, null);
		} finally {
			IOUtils.closeQuietly(template);
		}
		response.render(result);
	}

	public static HttpServletRequest getHttpServletRequest() {
		ServletWebRequest servletWebRequest = (ServletWebRequest) RequestCycle
				.get().getRequest();
		return servletWebRequest.getContainerRequest();
	}

	public static HttpServletResponse getHttpServletResponse() {
		WebResponse webResponse = (WebResponse) RequestCycle.get()
				.getResponse();
		return (HttpServletResponse) webResponse.getContainerResponse();
	}
}
