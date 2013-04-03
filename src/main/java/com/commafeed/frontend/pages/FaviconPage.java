package com.commafeed.frontend.pages;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.backend.HttpGetter;
import com.commafeed.backend.model.UserRole.Role;
import com.commafeed.frontend.SecurityCheck;

@SuppressWarnings("serial")
@SecurityCheck(Role.USER)
public class FaviconPage extends BasePage {

	private static final Logger log = LoggerFactory
			.getLogger(FaviconPage.class);

	@Inject
	HttpGetter getter;

	public FaviconPage(PageParameters params) {
		final String url = params.get("url").toString();
		getRequestCycle().scheduleRequestHandlerAfterCurrent(
				new IRequestHandler() {

					@Override
					public void respond(IRequestCycle requestCycle) {
						requestCycle.getResponse().write(getImage(url));
					}

					@Override
					public void detach(IRequestCycle requestCycle) {
					}
				});

	}

	private byte[] getImage(String url) {
		byte[] img = null;
		try {
			if (StringUtils.isNotBlank(url)) {
				int index = Math.max(url.length(), url.lastIndexOf("?"));
				url = url.substring(0, index);

				String iconUrl = "http://g.etfv.co/"
						+ URLEncoder.encode(url, "UTF-8") + "?defaulticon=none";
				img = getter.getBinary(iconUrl);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		if (img == null) {
			img = getDefaultIcon();
		}
		return img;
	}

	private byte[] getDefaultIcon() {
		byte[] bytes = null;
		InputStream is = null;
		try {
			is = getClass().getResourceAsStream("/favicon.gif");
			bytes = IOUtils.toByteArray(is);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(is);
		}
		return bytes;
	}
}
