package com.commafeed.backend.rome;

import com.rometools.rome.feed.rss.Description;
import com.rometools.rome.feed.rss.Item;
import com.rometools.rome.feed.synd.SyndContentImpl;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.impl.ConverterForRSS090;

/**
 * Support description tag for RSS09
 * 
 */
public class RSS090DescriptionConverter extends ConverterForRSS090 {

	@Override
	protected SyndEntry createSyndEntry(Item item, boolean preserveWireItem) {
		SyndEntry entry = super.createSyndEntry(item, preserveWireItem);
		Description desc = item.getDescription();
		if (desc != null) {
			SyndContentImpl syndDesc = new SyndContentImpl();
			syndDesc.setValue(desc.getValue());
			entry.setDescription(syndDesc);
		}
		return entry;
	}

}
