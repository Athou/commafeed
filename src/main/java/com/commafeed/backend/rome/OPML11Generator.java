package com.commafeed.backend.rome;

import org.jdom2.Element;

import com.rometools.opml.feed.opml.Opml;

/**
 * Add missing title to the generated OPML
 * 
 */
public class OPML11Generator extends com.rometools.opml.io.impl.OPML10Generator {

	public OPML11Generator() {
		super("opml_1.1");
	}

	@Override
	protected Element generateHead(Opml opml) {
		Element head = new Element("head");
		addNotNullSimpleElement(head, "title", opml.getTitle());
		return head;
	}
}
