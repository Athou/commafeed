package com.commafeed.backend.rome;

import org.jdom.Element;

import com.sun.syndication.feed.opml.Opml;

public class OPML11Generator extends com.sun.syndication.io.impl.OPML10Generator {

	public OPML11Generator() {
		super("opml_1.1");
	}

	protected Element generateHead(Opml opml) {
		Element head = new Element("head");
		addNotNullSimpleElement(head, "title", opml.getTitle());
		return head;
	}
}
