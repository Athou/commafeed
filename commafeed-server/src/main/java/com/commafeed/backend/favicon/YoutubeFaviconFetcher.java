package com.commafeed.backend.favicon;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import jakarta.inject.Singleton;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URIBuilder;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.HttpGetter;
import com.commafeed.backend.HttpGetter.HostNotAllowedException;
import com.commafeed.backend.HttpGetter.HttpResult;
import com.commafeed.backend.HttpGetter.NotModifiedException;
import com.commafeed.backend.HttpGetter.SchemeNotAllowedException;
import com.commafeed.backend.HttpGetter.TooManyRequestsException;
import com.commafeed.backend.model.Feed;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Singleton
public class YoutubeFaviconFetcher extends AbstractFaviconFetcher {

	private static final String PART_SNIPPET = "snippet";

	private static final JsonPointer CHANNEL_THUMBNAIL_URL = JsonPointer.compile("/items/0/snippet/thumbnails/default/url");
	private static final JsonPointer PLAYLIST_CHANNEL_ID = JsonPointer.compile("/items/0/snippet/channelId");

	private final HttpGetter getter;
	private final CommaFeedConfiguration config;
	private final ObjectMapper objectMapper;

	@Override
	public Favicon fetch(Feed feed) {
		String url = feed.getUrl();
		if (!url.toLowerCase().contains("youtube.com/feeds/videos.xml")) {
			return null;
		}

		Optional<String> googleAuthKey = config.googleAuthKey();
		if (googleAuthKey.isEmpty()) {
			log.debug("no google auth key configured");
			return null;
		}

		byte[] bytes = null;
		String contentType = null;
		try {
			List<NameValuePair> params = new URIBuilder(url).getQueryParams();
			Optional<NameValuePair> userId = params.stream().filter(nvp -> nvp.getName().equalsIgnoreCase("user")).findFirst();
			Optional<NameValuePair> channelId = params.stream().filter(nvp -> nvp.getName().equalsIgnoreCase("channel_id")).findFirst();
			Optional<NameValuePair> playlistId = params.stream().filter(nvp -> nvp.getName().equalsIgnoreCase("playlist_id")).findFirst();

			byte[] response = null;
			if (userId.isPresent()) {
				log.debug("contacting youtube api for user {}", userId.get().getValue());
				response = fetchForUser(googleAuthKey.get(), userId.get().getValue());
			} else if (channelId.isPresent()) {
				log.debug("contacting youtube api for channel {}", channelId.get().getValue());
				response = fetchForChannel(googleAuthKey.get(), channelId.get().getValue());
			} else if (playlistId.isPresent()) {
				log.debug("contacting youtube api for playlist {}", playlistId.get().getValue());
				response = fetchForPlaylist(googleAuthKey.get(), playlistId.get().getValue());
			}
			if (ArrayUtils.isEmpty(response)) {
				log.debug("youtube api returned empty response");
				return null;
			}

			JsonNode thumbnailUrl = objectMapper.readTree(response).at(CHANNEL_THUMBNAIL_URL);
			if (thumbnailUrl.isMissingNode()) {
				log.debug("youtube api returned invalid response");
				return null;
			}

			HttpResult iconResult = getter.get(thumbnailUrl.asText());
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

	private byte[] fetchForUser(String googleAuthKey, String userId)
			throws IOException, NotModifiedException, TooManyRequestsException, HostNotAllowedException, SchemeNotAllowedException {
		URI uri = UriBuilder.fromUri("https://www.googleapis.com/youtube/v3/channels")
				.queryParam("part", PART_SNIPPET)
				.queryParam("key", googleAuthKey)
				.queryParam("forUsername", userId)
				.build();
		return getter.get(uri.toString()).getContent();
	}

	private byte[] fetchForChannel(String googleAuthKey, String channelId)
			throws IOException, NotModifiedException, TooManyRequestsException, HostNotAllowedException, SchemeNotAllowedException {
		URI uri = UriBuilder.fromUri("https://www.googleapis.com/youtube/v3/channels")
				.queryParam("part", PART_SNIPPET)
				.queryParam("key", googleAuthKey)
				.queryParam("id", channelId)
				.build();
		return getter.get(uri.toString()).getContent();
	}

	private byte[] fetchForPlaylist(String googleAuthKey, String playlistId)
			throws IOException, NotModifiedException, TooManyRequestsException, HostNotAllowedException, SchemeNotAllowedException {
		URI uri = UriBuilder.fromUri("https://www.googleapis.com/youtube/v3/playlists")
				.queryParam("part", PART_SNIPPET)
				.queryParam("key", googleAuthKey)
				.queryParam("id", playlistId)
				.build();
		byte[] playlistBytes = getter.get(uri.toString()).getContent();

		JsonNode channelId = objectMapper.readTree(playlistBytes).at(PLAYLIST_CHANNEL_ID);
		if (channelId.isMissingNode()) {
			return new byte[0];
		}

		return fetchForChannel(googleAuthKey, channelId.asText());
	}

}
