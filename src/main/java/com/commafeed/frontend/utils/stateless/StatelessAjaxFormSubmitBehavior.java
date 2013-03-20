package com.commafeed.frontend.utils.stateless;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public abstract class StatelessAjaxFormSubmitBehavior extends
		AjaxFormSubmitBehavior {

	private static final long serialVersionUID = 1L;

	public StatelessAjaxFormSubmitBehavior(final String event) {
		super(event);
	}

	public StatelessAjaxFormSubmitBehavior(Form<?> form, String event) {
		super(form, event);
	}

	@Override
	public CharSequence getCallbackUrl() {
		final Url url = Url.parse(super.getCallbackUrl().toString());
		final PageParameters params = getPageParameters();
		return StatelessEncoder.mergeParameters(url, params).toString();
	}

	@Override
	public boolean getStatelessHint(final Component component) {
		return true;
	}

	/**
	 * Override this to pass the context of the current page to the behavior,
	 * allowing it to recreate the context for the ajax request.
	 * 
	 */
	protected PageParameters getPageParameters() {
		return null;
	}

}
