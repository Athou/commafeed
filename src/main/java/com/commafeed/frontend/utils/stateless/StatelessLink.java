package com.commafeed.frontend.utils.stateless;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public abstract class StatelessLink<T> extends Link<T> {
	private static final long serialVersionUID = 1L;

	private final PageParameters parameters;

	public StatelessLink(final String id) {
		this(id, null, null);
	}

	public StatelessLink(final String id, final IModel<T> model) {
		this(id, model, null);
	}

	public StatelessLink(final String id, final IModel<T> model,
			final PageParameters params) {
		super(id, model);
		setMarkupId(id);
		this.parameters = params;
	}

	protected final PageParameters getPageParameters() {
		return parameters;
	}

	@Override
	protected boolean getStatelessHint() {
		return true;
	}

	@Override
	protected CharSequence getURL() {
		final Url url = Url.parse(super.getURL().toString());
		Url mergedUrl = StatelessEncoder.mergeParameters(url, parameters);
		return mergedUrl.toString();
	}
}
