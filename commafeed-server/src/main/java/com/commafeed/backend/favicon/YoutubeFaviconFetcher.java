package com.commafeed.backend.favicon;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URIBuilder;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.HttpGetter;
import com.commafeed.backend.HttpGetter.HttpResult;
import com.commafeed.backend.model.Feed;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTube.Channels;
import com.google.api.services.youtube.YouTube.Playlists;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.PlaylistListResponse;
import com.google.api.services.youtube.model.Thumbnail;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({ @Inject }))
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
			List<NameValuePair> params = new URIBuilder(url).getQueryParams();
			Optional<NameValuePair> userId = params.stream().filter(nvp -> nvp.getName().equalsIgnoreCase("user")).findFirst();
			Optional<NameValuePair> channelId = params.stream().filter(nvp -> nvp.getName().equalsIgnoreCase("channel_id")).findFirst();
			Optional<NameValuePair> playlistId = params.stream().filter(nvp -> nvp.getName().equalsIgnoreCase("playlist_id")).findFirst();

			YouTube youtube = new YouTube.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance(), request -> {
			}).setApplicationName("CommaFeed").build();

			ChannelListResponse response = null;
			if (userId.isPresent()) {
				log.debug("contacting youtube api for user {}", userId.get().getValue());
				response = fetchForUser(youtube, googleAuthKey, userId.get().getValue());
			} else if (channelId.isPresent()) {
				log.debug("contacting youtube api for channel {}", channelId.get().getValue());
				response = fetchForChannel(youtube, googleAuthKey, channelId.get().getValue());
			} else if (playlistId.isPresent()) {
				log.debug("contacting youtube api for playlist {}", playlistId.get().getValue());
				response = fetchForPlaylist(youtube, googleAuthKey, playlistId.get().getValue());
			}

			if (MapUtils.isEmpty(response) || CollectionUtils.isEmpty(response.getItems())) {
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

	private ChannelListResponse fetchForUser(YouTube youtube, String googleAuthKey, String userId) throws IOException {
		Channels.List list = youtube.channels().list(List.of("snippet"));
		list.setKey(googleAuthKey);
		list.setForUsername(userId);
		return list.execute();
	}

	private ChannelListResponse fetchForChannel(YouTube youtube, String googleAuthKey, String channelId) throws IOException {
		Channels.List list = youtube.channels().list(List.of("snippet"));
		list.setKey(googleAuthKey);
		list.setId(List.of(channelId));
		return list.execute();
	}

	private ChannelListResponse fetchForPlaylist(YouTube youtube, String googleAuthKey, String playlistId) throws IOException {
		Playlists.List list = youtube.playlists().list(List.of("snippet"));
		list.setKey(googleAuthKey);
		list.setId(List.of(playlistId));

		PlaylistListResponse response = list.execute();
		if (response.getItems().isEmpty()) {
			return null;
		}

		String channelId = response.getItems().get(0).getSnippet().getChannelId();
		return fetchForChannel(youtube, googleAuthKey, channelId);
	}

}
