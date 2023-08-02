package com.commafeed.backend.service;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.ErrorHandler;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSStyleDeclaration;

import com.commafeed.backend.dao.FeedEntryContentDAO;
import com.commafeed.backend.feed.FeedUtils;
import com.commafeed.backend.model.FeedEntryContent;
import com.steadystate.css.parser.CSSOMParser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor(onConstructor = @__({ @Inject }))
@Slf4j
@Singleton
public class FeedEntryContentService {

	private static final Safelist HTML_WHITELIST = buildWhiteList();
	private static final List<String> ALLOWED_IFRAME_CSS_RULES = Arrays.asList("height", "width", "border");
	private static final List<String> ALLOWED_IMG_CSS_RULES = Arrays.asList("display", "width", "height");
	private static final char[] FORBIDDEN_CSS_RULE_CHARACTERS = new char[] { '(', ')' };

	private final FeedEntryContentDAO feedEntryContentDAO;

	/**
	 * this is NOT thread-safe
	 */
	public FeedEntryContent findOrCreate(FeedEntryContent content, String baseUrl) {
		content.setAuthor(FeedUtils.truncate(handleContent(content.getAuthor(), baseUrl, true), 128));
		content.setTitle(FeedUtils.truncate(handleContent(content.getTitle(), baseUrl, true), 2048));
		content.setContent(handleContent(content.getContent(), baseUrl, false));
		content.setMediaDescription(handleContent(content.getMediaDescription(), baseUrl, false));

		String contentHash = DigestUtils.sha1Hex(StringUtils.trimToEmpty(content.getContent()));
		content.setContentHash(contentHash);

		String titleHash = DigestUtils.sha1Hex(StringUtils.trimToEmpty(content.getTitle()));
		content.setTitleHash(titleHash);

		List<FeedEntryContent> existing = feedEntryContentDAO.findExisting(contentHash, titleHash);
		Optional<FeedEntryContent> equivalentContent = existing.stream().filter(content::equivalentTo).findFirst();
		if (equivalentContent.isPresent()) {
			return equivalentContent.get();
		}

		feedEntryContentDAO.saveOrUpdate(content);
		return content;
	}

	private static Safelist buildWhiteList() {
		Safelist whitelist = new Safelist();
		whitelist.addTags("a", "b", "blockquote", "br", "caption", "cite", "code", "col", "colgroup", "dd", "div", "dl", "dt", "em", "h1",
				"h2", "h3", "h4", "h5", "h6", "i", "iframe", "img", "li", "ol", "p", "pre", "q", "small", "strike", "strong", "sub", "sup",
				"table", "tbody", "td", "tfoot", "th", "thead", "tr", "u", "ul");

		whitelist.addAttributes("div", "dir");
		whitelist.addAttributes("pre", "dir");
		whitelist.addAttributes("code", "dir");
		whitelist.addAttributes("table", "dir");
		whitelist.addAttributes("p", "dir");
		whitelist.addAttributes("a", "href", "title");
		whitelist.addAttributes("blockquote", "cite");
		whitelist.addAttributes("col", "span", "width");
		whitelist.addAttributes("colgroup", "span", "width");
		whitelist.addAttributes("iframe", "src", "height", "width", "allowfullscreen", "frameborder", "style");
		whitelist.addAttributes("img", "align", "alt", "height", "src", "title", "width", "style");
		whitelist.addAttributes("ol", "start", "type");
		whitelist.addAttributes("q", "cite");
		whitelist.addAttributes("table", "border", "bordercolor", "summary", "width");
		whitelist.addAttributes("td", "border", "bordercolor", "abbr", "axis", "colspan", "rowspan", "width");
		whitelist.addAttributes("th", "border", "bordercolor", "abbr", "axis", "colspan", "rowspan", "scope", "width");
		whitelist.addAttributes("ul", "type");

		whitelist.addProtocols("a", "href", "ftp", "http", "https", "magnet", "mailto");
		whitelist.addProtocols("blockquote", "cite", "http", "https");
		whitelist.addProtocols("img", "src", "http", "https");
		whitelist.addProtocols("q", "cite", "http", "https");

		whitelist.addEnforcedAttribute("a", "target", "_blank");
		whitelist.addEnforcedAttribute("a", "rel", "noreferrer");
		return whitelist;
	}

	private String handleContent(String content, String baseUri, boolean keepTextOnly) {
		if (StringUtils.isNotBlank(content)) {
			baseUri = StringUtils.trimToEmpty(baseUri);

			Document dirty = Jsoup.parseBodyFragment(content, baseUri);
			Cleaner cleaner = new Cleaner(HTML_WHITELIST);
			Document clean = cleaner.clean(dirty);

			for (Element e : clean.select("iframe[style]")) {
				String style = e.attr("style");
				String escaped = escapeIFrameCss(style);
				e.attr("style", escaped);
			}

			for (Element e : clean.select("img[style]")) {
				String style = e.attr("style");
				String escaped = escapeImgCss(style);
				e.attr("style", escaped);
			}

			clean.outputSettings(new OutputSettings().escapeMode(EscapeMode.base).prettyPrint(false));
			Element body = clean.body();
			if (keepTextOnly) {
				content = body.text();
			} else {
				content = body.html();
			}
		}
		return content;
	}

	private String escapeIFrameCss(String orig) {
		String rule = "";
		try {
			List<String> rules = new ArrayList<>();
			CSSStyleDeclaration decl = buildCssParser().parseStyleDeclaration(new InputSource(new StringReader(orig)));

			for (int i = 0; i < decl.getLength(); i++) {
				String property = decl.item(i);
				String value = decl.getPropertyValue(property);
				if (StringUtils.isBlank(property) || StringUtils.isBlank(value)) {
					continue;
				}

				if (ALLOWED_IFRAME_CSS_RULES.contains(property) && StringUtils.containsNone(value, FORBIDDEN_CSS_RULE_CHARACTERS)) {
					rules.add(property + ":" + decl.getPropertyValue(property) + ";");
				}
			}
			rule = StringUtils.join(rules, "");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return rule;
	}

	private String escapeImgCss(String orig) {
		String rule = "";
		try {
			List<String> rules = new ArrayList<>();
			CSSStyleDeclaration decl = buildCssParser().parseStyleDeclaration(new InputSource(new StringReader(orig)));

			for (int i = 0; i < decl.getLength(); i++) {
				String property = decl.item(i);
				String value = decl.getPropertyValue(property);
				if (StringUtils.isBlank(property) || StringUtils.isBlank(value)) {
					continue;
				}

				if (ALLOWED_IMG_CSS_RULES.contains(property) && StringUtils.containsNone(value, FORBIDDEN_CSS_RULE_CHARACTERS)) {
					rules.add(property + ":" + decl.getPropertyValue(property) + ";");
				}
			}
			rule = StringUtils.join(rules, "");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return rule;
	}

	private CSSOMParser buildCssParser() {
		CSSOMParser parser = new CSSOMParser();

		parser.setErrorHandler(new ErrorHandler() {
			@Override
			public void warning(CSSParseException exception) throws CSSException {
				log.debug("warning while parsing css: {}", exception.getMessage(), exception);
			}

			@Override
			public void error(CSSParseException exception) throws CSSException {
				log.debug("error while parsing css: {}", exception.getMessage(), exception);
			}

			@Override
			public void fatalError(CSSParseException exception) throws CSSException {
				log.debug("fatal error while parsing css: {}", exception.getMessage(), exception);
			}
		});

		return parser;
	}
}
