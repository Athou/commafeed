package com.commafeed.frontend.pages.components;

import org.apache.wicket.authroles.authentication.panel.SignInPanel;
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;

@SuppressWarnings("serial")
public class LoginPanel extends SignInPanel {

	public LoginPanel(String id) {
		super(id);
		replace(new BootstrapFeedbackPanel("feedback",
				new ContainerFeedbackMessageFilter(this)));
	}

}
