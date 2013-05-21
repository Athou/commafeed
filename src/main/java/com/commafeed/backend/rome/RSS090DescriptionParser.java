package com.commafeed.backend.rome;

import org.jdom.Element;

import com.sun.syndication.feed.rss.Description;
import com.sun.syndication.feed.rss.Item;
import com.sun.syndication.io.impl.RSS090Parser;

public class RSS090DescriptionParser extends RSS090Parser {

	@Override
	protected Item parseItem(Element rssRoot, Element eItem) {
		Item item = super.parseItem(rssRoot, eItem);

		Element e = eItem.getChild("description", getRSSNamespace());
		if (e != null) {
			Description desc = new Description();
			desc.setValue(e.getText());
			item.setDescription(desc);
		}

		return item;
	}

}
