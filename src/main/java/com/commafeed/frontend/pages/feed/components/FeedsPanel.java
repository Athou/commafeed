package com.commafeed.frontend.pages.feed.components;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import com.commafeed.model.FeedEntryStatus;

import de.agilecoders.wicket.markup.html.bootstrap.behavior.CssClassNameAppender;
import de.agilecoders.wicket.util.Components;

@SuppressWarnings("serial")
public class FeedsPanel extends GenericPanel<List<FeedEntryStatus>> {

	private AttributeModifier dataParentModifier;

	public FeedsPanel(String id, IModel<List<FeedEntryStatus>> model) {
		super(id, model);
		add(new CssClassNameAppender("accordion"));

		dataParentModifier = new AttributeModifier("data-parent", getMarkupId());

		ListView<FeedEntryStatus> listView = new ListView<FeedEntryStatus>(
				"entry", model) {
			@Override
			protected void populateItem(ListItem<FeedEntryStatus> item) {
				FeedEntryStatus status = item.getModelObject();
				WebMarkupContainer link = new WebMarkupContainer("link");
				link.add(dataParentModifier);

				WebMarkupContainer body = new WebMarkupContainer("body");
				body.add(new Label("content", status.getEntry().getContent())
						.setEscapeModelStrings(false));

				link.add(new AttributeModifier("href", "#" + body.getMarkupId()));
			}
		};
		add(listView);

	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		Components.assertTag(this, tag, "div");
	}
}
