package com.commafeed.frontend.utils.exception;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;

import com.commafeed.frontend.references.bootstrap.BootstrapReference;

public class DisplayExceptionPage extends WebPage {

	private static final long serialVersionUID = 1L;

	public DisplayExceptionPage(Throwable t) {
		Throwable de = findDisplayException(t);
		if (de != null) {
			t = de;
		}

		add(new Label("message", t.getMessage()));

		add(new BookmarkablePageLink<Void>("homepage", getApplication()
				.getHomePage()));

		StringWriter stringWriter = new StringWriter();
		t.printStackTrace(new PrintWriter(stringWriter));
		t.printStackTrace();
		add(new Label("stacktrace", stringWriter.toString()));

	}

	private Throwable findDisplayException(Throwable t) {
		while (t != null && !(t instanceof DisplayException)) {
			t = t.getCause();
		}
		return t;
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		BootstrapReference.renderHead(response);
	}

}
