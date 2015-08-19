package com.commafeed.backend.favicon;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.HttpGetter;
import com.commafeed.backend.HttpGetter.HttpResult;
import com.commafeed.backend.model.Feed;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.Thumbnail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({ @Inject }) )
@Singleton
public class YoutubeFaviconFetcher extends AbstractFaviconFetcher {

	private final HttpGetter getter;
	private final CommaFeedConfiguration config;

	@Override
	public Favicon fetch(Feed feed) {
		String url = feed.getUrl();

		if (!url.toLowerCase().contains("youtube.com/feeds/videos.xml")) {
			return null;
		}

		String googleAuthKey = config.getApplicationSettings().getGoogleAuthKey();
		if (googleAuthKey == null) {
			log.debug("no google auth key configured");
			return null;
		}

		byte[] bytes = null;
		String contentType = null;
		try {
			List<NameValuePair> params = URLEncodedUtils.parse(url.substring(url.indexOf("?") + 1), StandardCharsets.UTF_8);
			Optional<NameValuePair> userId = params.stream().filter(nvp -> nvp.getName().equalsIgnoreCase("user")).findFirst();
			Optional<NameValuePair> channelId = params.stream().filter(nvp -> nvp.getName().equalsIgnoreCase("channel_id")).findFirst();
			if (!userId.isPresent() && !channelId.isPresent()) {
				return null;
			}

			YouTube youtube = new YouTube.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(),
					new HttpRequestInitializer() {
						@Override
						public void initialize(HttpRequest request) throws IOException {
						}
					}).setApplicationName("CommaFeed").build();

			YouTube.Channels.List list = youtube.channels().list("snippet");
			list.setKey(googleAuthKey);
			if (userId.isPresent()) {
				list.setForUsername(userId.get().getValue());
			} else {
				list.setId(channelId.get().getValue());
			}

			log.debug("contacting youtube api");
			ChannelListResponse response = list.execute();
			if (response.getItems().isEmpty()) {
				log.debug("youtube api returned no items");
				return null;
			}

			Channel channel = response.getItems().get(0);
			Thumbnail thumbnail = channel.getSnippet().getThumbnails().getDefault();

			log.debug("fetching favicon");
			HttpResult iconResult = getter.getBinary(thumbnail.getUrl(), TIMEOUT);
			bytes = iconResult.getContent();
			contentType = iconResult.getContentType();
		} catch (Exception e) {
			log.debug("Failed to retrieve YouTube icon", e);
		}

		if (!isValidIconResponse(bytes, contentType)) {
			return null;
		}
		return new Favicon(bytes, contentType);
	}
}
