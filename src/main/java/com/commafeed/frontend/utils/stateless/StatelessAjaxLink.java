package com.commafeed.frontend.utils.stateless;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.IAjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public abstract class StatelessAjaxLink<T> extends StatelessLink<T>
		implements IAjaxLink {

	private static final long serialVersionUID = -133600842398684777L;

	public StatelessAjaxLink(final String id) {
		this(id, null, null);
	}

	public StatelessAjaxLink(final String id,
			final PageParameters params) {
		this(id, null, params);
	}

	public StatelessAjaxLink(final String id, final IModel<T> model) {
		this(id, model, null);
	}

	public StatelessAjaxLink(final String id, final IModel<T> model,
			final PageParameters params) {
		super(id, model, params);

		add(new StatelessAjaxEventBehavior("click") {
			private static final long serialVersionUID = -8445395501430605953L;

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				StatelessAjaxLink.this.updateAjaxAttributes(attributes);
			}

			@Override
			protected PageParameters getPageParameters() {
				return StatelessAjaxLink.this.getPageParameters();
			}

			@Override
			protected void onComponentTag(final ComponentTag tag) {
				if (isLinkEnabled()) {
					super.onComponentTag(tag);
				}
			}

			@Override
			protected void onEvent(final AjaxRequestTarget target) {
				onClick(target);
				target.add(StatelessAjaxLink.this);
			}
		});
	}

	protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
	}

	@Override
	public final void onClick() {
		onClick(null);
	}

	public abstract void onClick(final AjaxRequestTarget target);
}
