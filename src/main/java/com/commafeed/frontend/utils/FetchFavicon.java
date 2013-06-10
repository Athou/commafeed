package com.commafeed.frontend.utils;

import java.net.MalformedURLException;
import java.net.URL;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

//Inspired/Ported from https://github.com/potatolondon/getfavicon
public class FetchFavicon {
	void inf(String message) {
		//
	}

	static long MIN_ICON_LENGTH = 100;
	static long MAX_ICON_LENGTH = 20000;
	static String[] ICON_MIMETYPES = new String[] { "image/x-icon",
			"image/vnd.microsoft.icon", "image/ico", "image/icon", "text/ico",
			"application/ico", "image/x-ms-bmp", "image/x-bmp", "image/gif",
			"image/png", "image/jpeg" };

	static String[] ICON_MIMETYPE_BLACKLIST = new String[] { "application/xml",
			"text/html" };

	boolean in(String[] array, String value) {
		for (String i : array) {
			if (i.equals(value)) {
				return true;
			}
		}
		return false;
	}

	boolean isValidIconResponse(Response iconResponse) {
		long iconLength = iconResponse.bodyAsBytes().length;

		String iconContentType = iconResponse.header("Content-Type");
		if (!iconContentType.isEmpty())
			iconContentType = iconContentType.split(";")[0];

		if (iconResponse.statusCode() != 200) {
			inf("Status code isn't 200");
			return false;
		}

		if (in(ICON_MIMETYPE_BLACKLIST, iconContentType)) {
			inf("Content-Type in ICON_MIMETYPE_BLACKLIST");
			return false;
		}

		if (iconLength < MIN_ICON_LENGTH) {
			inf("Length below MIN_ICON_LENGTH");
			return false;
		}

		if (iconLength > MAX_ICON_LENGTH) {
			inf("Length greater than MAX_ICON_LENGTH");
			return false;
		}
		return true;
	}

	byte[] iconAtRoot(String targetPath) {
		Response rootIconPath;
		try {
			URL url = new URL(new URL(targetPath), "/favicon.ico");
			inf(url.toString());
			rootIconPath = Jsoup
					.connect(url.toString())
					.followRedirects(true)
					.ignoreContentType(true).execute();
		} catch (Exception e) {
			inf("Failed to retrieve iconAtRoot");
			return null;
		}

		if (isValidIconResponse(rootIconPath)) {
			return rootIconPath.bodyAsBytes();
		}
		return null;
	}

	byte[] iconInPage(String targetPath) {
		inf("iconInPage, trying " + targetPath);

		Document pageSoup;
		try {
			pageSoup = Jsoup.connect(targetPath).followRedirects(true).get();
		} catch (Exception e) {
			inf("Failed to retrieve page to find icon");
			return null;
		}

		Elements pageSoupIcon = pageSoup
				.select("link[rel~=(?i)^(shortcut|icon|shortcut icon)$]");

		if (pageSoupIcon.size() == 0) {
			return null;
		}
		String pageIconHref = pageSoupIcon.get(0).attr("href");
		String pageIconPath;
		if (pageIconHref.isEmpty()) {
			inf("No icon found in page");
			return null;
		}

		try {
			pageIconPath = new URL(new URL(targetPath), pageIconHref).toString();
		} catch (MalformedURLException e1) {
			inf("URL concatination faild");
			return null;
		}

		inf("Found unconfirmed iconInPage at");

		Response pagePathFaviconResult;
		try {
			pagePathFaviconResult = Jsoup.connect(pageIconPath)
					.followRedirects(true).ignoreContentType(true)
					.execute();
		} catch (Exception e) {
			inf("Failed to retrieve icon found in page");
			return null;
		}

		if (isValidIconResponse(pagePathFaviconResult)) {
			return pagePathFaviconResult.bodyAsBytes();
		}
		inf("Invalid icon found");
		return null;
	}

	public byte[] get(String targetPath) {
		byte[] icon;

		icon = iconAtRoot(targetPath);
		if (icon != null) {
			return icon;
		}

		icon = iconInPage(targetPath);
		if (icon != null) {
			return icon;
		}

		return null; // or returning default feed
	}
}
