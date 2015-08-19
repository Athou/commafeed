package com.commafeed.frontend.resource;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
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
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.Timed;
import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.dao.FeedDAO;
import com.commafeed.backend.feed.FeedParser;
import com.commafeed.backend.feed.FeedQueues;
import com.commafeed.backend.feed.FetchedFeed;
import com.commafeed.backend.model.Feed;
import com.google.common.base.Preconditions;

import io.dropwizard.hibernate.UnitOfWork;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Path("/push")
@Slf4j
@RequiredArgsConstructor(onConstructor = @__({ @Inject }) )
@Singleton
public class PubSubHubbubCallbackREST {

	@Context
	private HttpServletRequest request;

	private final FeedDAO feedDAO;
	private final FeedParser parser;
	private final FeedQueues queues;
	private final CommaFeedConfiguration config;
	private final MetricRegistry metricRegistry;

	@Path("/callback")
	@GET
	@UnitOfWork
	@Produces(MediaType.TEXT_PLAIN)
	@Timed
	public Response verify(@QueryParam("hub.mode") String mode, @QueryParam("hub.topic") String topic,
			@QueryParam("hub.challenge") String challenge, @QueryParam("hub.lease_seconds") String leaseSeconds,
			@QueryParam("hub.verify_token") String verifyToken) {
		if (!config.getApplicationSettings().getPubsubhubbub()) {
			return Response.status(Status.FORBIDDEN).entity("pubsubhubbub is disabled").build();
		}

		Preconditions.checkArgument(StringUtils.isNotEmpty(topic));
		Preconditions.checkArgument("subscribe".equals(mode));

		log.debug("confirmation callback received for {}", topic);

		List<Feed> feeds = feedDAO.findByTopic(topic);

		if (feeds.isEmpty() == false) {
			for (Feed feed : feeds) {
				log.debug("activated push notifications for {}", feed.getPushTopic());
				feed.setPushLastPing(new Date());
			}
			feedDAO.saveOrUpdate(feeds);
			return Response.ok(challenge).build();
		} else {
			log.debug("rejecting callback: no push info for {}", topic);
			return Response.status(Status.NOT_FOUND).build();
		}
	}

	@Path("/callback")
	@POST
	@UnitOfWork
	@Consumes({ MediaType.APPLICATION_ATOM_XML, "application/rss+xml" })
	@Timed
	public Response callback() {

		if (!config.getApplicationSettings().getPubsubhubbub()) {
			return Response.status(Status.FORBIDDEN).entity("pubsubhubbub is disabled").build();
		}

		try {
			byte[] bytes = IOUtils.toByteArray(request.getInputStream());

			if (ArrayUtils.isEmpty(bytes)) {
				return Response.status(Status.BAD_REQUEST).entity("empty body received").build();
			}

			FetchedFeed fetchedFeed = parser.parse(null, bytes);
			String topic = fetchedFeed.getFeed().getPushTopic();
			if (StringUtils.isBlank(topic)) {
				return Response.status(Status.BAD_REQUEST).entity("empty topic received").build();
			}

			log.debug("content callback received for {}", topic);
			List<Feed> feeds = feedDAO.findByTopic(topic);
			if (feeds.isEmpty()) {
				return Response.status(Status.BAD_REQUEST).entity("no feeds found for that topic").build();
			}

			for (Feed feed : feeds) {
				log.debug("pushing content to queue for {}", feed.getUrl());
				queues.add(feed, false);
			}
			metricRegistry.meter(MetricRegistry.name(getClass(), "pushReceived")).mark();

		} catch (Exception e) {
			log.error("Could not parse pubsub callback: " + e.getMessage());
		}
		return Response.ok().build();
	}
}
