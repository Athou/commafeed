package com.commafeed.frontend.utils.stateless;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import de.agilecoders.wicket.markup.html.bootstrap.button.BootstrapAjaxButton;
import de.agilecoders.wicket.markup.html.bootstrap.button.Buttons;

public abstract class BootstrapStatelessAjaxButton extends BootstrapAjaxButton {

	private static final long serialVersionUID = 1L;

	private PageParameters parameters;

	public BootstrapStatelessAjaxButton(final String componentId,
			final Buttons.Type buttonType) {
		super(componentId, buttonType);
	}

	public BootstrapStatelessAjaxButton(final String componentId,
			final IModel<String> model, final Buttons.Type buttonType) {
		super(componentId, model, buttonType);
	}

	public BootstrapStatelessAjaxButton(final String componentId,
			final IModel<String> model, final Buttons.Type buttonType,
			PageParameters parameters) {
		super(componentId, model, buttonType);
		this.parameters = parameters;
	}

	public BootstrapStatelessAjaxButton(String id, Form<?> form,
			Buttons.Type buttonType) {
		super(id, form, buttonType);
	}

	public BootstrapStatelessAjaxButton(String id, IModel<String> model,
			Form<?> form, Buttons.Type buttonType) {
		super(id, model, form, buttonType);

	}

	public BootstrapStatelessAjaxButton(String id, IModel<String> model,
			Form<?> form, Buttons.Type buttonType, PageParameters parameters) {
		super(id, model, form, buttonType);
		this.parameters = parameters;
	}

	@Override
	protected AjaxFormSubmitBehavior newAjaxFormSubmitBehavior(String event) {
		return new StatelessAjaxFormSubmitBehavior(getForm(), event) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				BootstrapStatelessAjaxButton.this.onSubmit(target,
						BootstrapStatelessAjaxButton.this.getForm());
			}

			@Override
			protected void onAfterSubmit(AjaxRequestTarget target) {
				BootstrapStatelessAjaxButton.this.onAfterSubmit(target,
						BootstrapStatelessAjaxButton.this.getForm());
			}

			@Override
			protected void onError(AjaxRequestTarget target) {
				BootstrapStatelessAjaxButton.this.onError(target,
						BootstrapStatelessAjaxButton.this.getForm());
			}

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				BootstrapStatelessAjaxButton.this.updateAjaxAttributes(attributes);
			}

			@Override
			public boolean getDefaultProcessing() {
				return BootstrapStatelessAjaxButton.this.getDefaultFormProcessing();
			}

			@Override
			protected PageParameters getPageParameters() {
				return parameters;
			}
		};
	}
}
