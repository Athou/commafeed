package com.commafeed.backend.rome;

import java.util.Locale;

import org.jdom2.Element;

import com.rometools.rome.feed.rss.Description;
import com.rometools.rome.feed.rss.Item;
import com.rometools.rome.io.impl.RSS090Parser;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Support description tag for RSS09
 * 
 */
@RegisterForReflection
public class RSS090DescriptionParser extends RSS090Parser {

	@Override
	protected Item parseItem(Element rssRoot, Element eItem, Locale locale) {
		Item item = super.parseItem(rssRoot, eItem, locale);
		Element e = eItem.getChild("description", getRSSNamespace());
		if (e != null) {
			Description desc = new Description();
			desc.setValue(e.getText());
			item.setDescription(desc);
		}

		return item;
	}
}
