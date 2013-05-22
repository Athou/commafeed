package com.commafeed.backend.services;

import java.util.List;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.inject.Inject;

import com.commafeed.backend.dao.FeedPushInfoDAO;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedPushInfo;
import com.commafeed.backend.model.FeedPushInfo_;

@Singleton
public class FeedPushInfoService {

	@Inject
	FeedPushInfoDAO feedPushInfoDAO;

	@Lock(LockType.WRITE)
	public FeedPushInfo findOrCreate(Feed feed, String hub, String topic) {
		FeedPushInfo info = null;

		List<FeedPushInfo> infos = feedPushInfoDAO.findByField(
				FeedPushInfo_.feed, feed);
		if (infos.isEmpty()) {
			info = new FeedPushInfo();
			info.setFeed(feed);
			info.setHub(hub);
			info.setTopic(topic);
			info.setActive(false);
			feedPushInfoDAO.save(info);
		} else {
			info = infos.get(0);
		}
		return info;
	}

}
