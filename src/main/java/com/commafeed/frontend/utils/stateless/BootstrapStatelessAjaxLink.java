package com.commafeed.frontend.utils.stateless;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import de.agilecoders.wicket.markup.html.bootstrap.button.BootstrapAjaxLink;
import de.agilecoders.wicket.markup.html.bootstrap.button.Buttons;

public abstract class BootstrapStatelessAjaxLink<T> extends BootstrapAjaxLink<T> {

	private static final long serialVersionUID = 1L;

	private PageParameters parameters;

	public BootstrapStatelessAjaxLink(final String id, final Buttons.Type buttonType) {
		super(id, buttonType);
	}

	public BootstrapStatelessAjaxLink(String id, IModel<T> model,
			Buttons.Type buttonType) {
		super(id, model, buttonType);
	}

	public BootstrapStatelessAjaxLink(String id, IModel<T> model,
			Buttons.Type buttonType, PageParameters parameters) {
		super(id, model, buttonType);
		this.parameters = parameters;
	}

	@Override
	protected AjaxEventBehavior newAjaxEventBehavior(String event) {
		return new StatelessAjaxEventBehavior(event) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onEvent(AjaxRequestTarget target) {
				BootstrapStatelessAjaxLink.this.onClick(target);
			}

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				BootstrapStatelessAjaxLink.this.updateAjaxAttributes(attributes);
			}

			@Override
			protected PageParameters getPageParameters() {
				return parameters;
			}
		};
	}
}
