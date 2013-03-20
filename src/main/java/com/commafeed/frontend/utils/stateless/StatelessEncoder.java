package com.commafeed.frontend.utils.stateless;

import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

import org.apache.wicket.request.Url;
import org.apache.wicket.request.mapper.parameter.INamedParameters;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.encoding.UrlEncoder;

final class StatelessEncoder {

	static Url mergeParameters(final Url url, final PageParameters params) {
		if (params == null) {
			return url;
		}

		Charset charset = url.getCharset();
		Url mergedUrl = Url.parse(url.toString(), charset);
		UrlEncoder urlEncoder = UrlEncoder.QUERY_INSTANCE;
		Set<String> setParameters = new HashSet<String>();

		for (INamedParameters.NamedPair pair : params.getAllNamed()) {
			String key = urlEncoder.encode(pair.getKey(), charset);
			String value = urlEncoder.encode(pair.getValue(), charset);

			if (setParameters.contains(key)) {
				mergedUrl.addQueryParameter(key, value);
			} else {
				mergedUrl.setQueryParameter(key, value);
				setParameters.add(key);
			}
		}

		return mergedUrl;
	}

	private StatelessEncoder() {

	}
}
