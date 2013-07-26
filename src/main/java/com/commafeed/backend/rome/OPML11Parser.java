package com.commafeed.backend.rome;

import org.jdom.Document;
import org.jdom.Element;

import com.sun.syndication.io.impl.OPML10Parser;

/**
 * Support for OPML 1.1 parsing
 * 
 */
public class OPML11Parser extends OPML10Parser {

	public OPML11Parser() {
		super("opml_1.1");
	}

	@Override
	public boolean isMyType(Document document) {
		Element e = document.getRootElement();

		if (e.getName().equals("opml") && (e.getChild("head") == null || e.getChild("head").getChild("docs") == null)
				&& (e.getAttributeValue("version") == null || e.getAttributeValue("version").equals("1.1"))) {
			return true;
		}

		return false;

	};
}
