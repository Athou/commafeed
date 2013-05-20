package com.commafeed.frontend.rest.resources;

import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.backend.dao.FeedPushInfoDAO;
import com.commafeed.backend.feeds.FeedParser;
import com.commafeed.backend.feeds.FeedRefreshTaskGiver;
import com.commafeed.backend.feeds.FetchedFeed;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedPushInfo;
import com.google.api.client.repackaged.com.google.common.base.Preconditions;

@Path("/push")
public class PubSubHubbubCallbackREST {

	private static Logger log = LoggerFactory
			.getLogger(PubSubHubbubCallbackREST.class);

	@Context
	HttpServletRequest request;

	@Inject
	FeedPushInfoDAO feedPushInfoDAO;

	@Inject
	FeedParser parser;

	@Inject
	FeedRefreshTaskGiver taskGiver;

	@Path("/callback")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Response verify(@QueryParam("hub.mode") String mode,
			@QueryParam("hub.topic") String topic,
			@QueryParam("hub.challenge") String challenge,
			@QueryParam("hub.lease_seconds") String leaseSeconds,
			@QueryParam("hub.verify_token") String verifyToken) {
		Preconditions.checkArgument(StringUtils.isNotEmpty(topic));
		Preconditions.checkArgument("subscribe".equals(mode));

		log.info("confirmation callback received for {}", topic);

		List<FeedPushInfo> infos = feedPushInfoDAO.findByTopic(topic);

		if (infos.isEmpty() == false) {
			for (FeedPushInfo info : infos) {
				log.info("activated push notifications for {}", info.getFeed()
						.getUrl());
				info.setActive(true);
			}
			feedPushInfoDAO.update(infos);
			return Response.ok(challenge).build();
		} else {
			log.info("rejecting callback: no push info for {}", topic);
			return Response.status(Status.NOT_FOUND).build();
		}
	}

	@Path("/callback")
	@POST
	@Consumes({ MediaType.APPLICATION_ATOM_XML, "application/rss+xml" })
	public Response callback() {
		log.info("content callback received");
		try {
			byte[] bytes = IOUtils.toByteArray(request.getInputStream());
			log.info(new String(bytes));
			FetchedFeed fetchedFeed = parser.parse(null, bytes);
			String topic = fetchedFeed.getTopic();
			if (topic != null) {
				log.info("content callback received for {}", topic);
				List<FeedPushInfo> infos = feedPushInfoDAO.findByTopic(topic);
				for (FeedPushInfo info : infos) {
					Feed feed = info.getFeed();
					log.info("pushing content to queue for {}", feed.getUrl());
					taskGiver.add(feed);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return Response.ok().build();
	}
}
