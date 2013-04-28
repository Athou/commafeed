package com.commafeed.frontend.pages.components;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;

@SuppressWarnings("serial")
public class BootstrapFeedbackPanel extends FeedbackPanel {

	public BootstrapFeedbackPanel(String id) {
		super(id);
		init();
	}

	public BootstrapFeedbackPanel(String id, IFeedbackMessageFilter filter) {
		super(id, filter);
		init();
	}

	private void init() {
		setOutputMarkupPlaceholderTag(true);
		add(new AttributeAppender("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				StringBuilder sb = new StringBuilder();
				if (anyMessage()) {
					sb.append(" bs-fb alert");
				}
				if (anyErrorMessage()) {
					sb.append(" alert-error");
				} else {
					sb.append(" alert-success");
				}
				return sb.toString();
			}
		}));

		get("feedbackul").add(new AttributeAppender("class", " unstyled"));
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		tag.setName("div");
		super.onComponentTag(tag);
	}
}
