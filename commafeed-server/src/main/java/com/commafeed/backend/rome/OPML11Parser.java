package com.commafeed.backend.rome;

import java.util.Locale;

import org.jdom2.Document;
import org.jdom2.Element;

import com.rometools.opml.io.impl.OPML10Parser;
import com.rometools.rome.feed.WireFeed;
import com.rometools.rome.io.FeedException;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Support for OPML 1.1 parsing
 * 
 */
@RegisterForReflection
public class OPML11Parser extends OPML10Parser {

	public OPML11Parser() {
		super("opml_1.1");
	}

	@Override
	public boolean isMyType(Document document) {
		Element e = document.getRootElement();

		return e.getName().equals("opml");

	}

	@Override
	public WireFeed parse(Document document, boolean validate, Locale locale) throws IllegalArgumentException, FeedException {
		document.getRootElement().getChildren().add(new Element("head"));
		return super.parse(document, validate, locale);
	}
}
