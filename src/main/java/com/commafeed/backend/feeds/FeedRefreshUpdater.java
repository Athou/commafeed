package com.commafeed.backend.feeds;

import java.io.Serializable;
import java.util.Collection;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.backend.dao.FeedDAO;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.services.FeedUpdateService;

@MessageDriven(name = "FeedRefreshUpdater", activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "jms/refreshQueue") })
public class FeedRefreshUpdater implements MessageListener {

	private static Logger log = LoggerFactory
			.getLogger(FeedRefreshUpdater.class);

	@Inject
	FeedDAO feedDAO;

	@Inject
	FeedUpdateService feedUpdateService;

	@Override
	public void onMessage(Message message) {
		if (message instanceof ObjectMessage) {
			ObjectMessage objectMessage = (ObjectMessage) message;
			try {
				FeedRefreshTask task = (FeedRefreshTask) objectMessage
						.getObject();

				if (task.getEntries() != null) {
					feedUpdateService.updateEntries(task.getFeed(),
							task.getEntries());
				}
				feedDAO.update(task.getFeed());
			} catch (JMSException e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	public static class FeedRefreshTask implements Serializable {

		private static final long serialVersionUID = 1L;

		private Feed feed;
		private Collection<FeedEntry> entries;

		public FeedRefreshTask(Feed feed, Collection<FeedEntry> entries) {
			this.feed = feed;
			this.entries = entries;
		}

		public Feed getFeed() {
			return feed;
		}

		public Collection<FeedEntry> getEntries() {
			return entries;
		}

	}

}
