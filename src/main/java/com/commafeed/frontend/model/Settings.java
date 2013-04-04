package com.commafeed.frontend.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Settings implements Serializable {

	private String readingMode;
	private String customCss;

	public String getReadingMode() {
		return readingMode;
	}

	public void setReadingMode(String readingMode) {
		this.readingMode = readingMode;
	}

	public String getCustomCss() {
		return customCss;
	}

	public void setCustomCss(String customCss) {
		this.customCss = customCss;
	}

}
