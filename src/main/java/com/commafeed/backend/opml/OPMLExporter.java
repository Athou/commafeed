package com.commafeed.backend.opml;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.RequiredArgsConstructor;

import com.commafeed.backend.dao.FeedCategoryDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.User;
import com.rometools.opml.feed.opml.Attribute;
import com.rometools.opml.feed.opml.Opml;
import com.rometools.opml.feed.opml.Outline;

@RequiredArgsConstructor(onConstructor = @__({ @Inject }))
@Singleton
public class OPMLExporter {

	private final FeedCategoryDAO feedCategoryDAO;
	private final FeedSubscriptionDAO feedSubscriptionDAO;

	public Opml export(User user) {
	    String userName = user.getName();
	    
	    List<FeedCategory> categories = feedCategoryDAO.findAll(user);
		List<FeedSubscription> subscriptions = feedSubscriptionDAO.findAll(user);
		
		return generateOPML(userName, categories, subscriptions);
	}

    Opml generateOPML(String userName, List<FeedCategory> categories, List<FeedSubscription> subscriptions) {
        Opml opml = new Opml();
        
        opml.setFeedType("opml_1.1");
        opml.setTitle(String.format("%s subscriptions in CommaFeed", userName));
        opml.setCreated(new Date());
        
        Map<Long, SubscriptionOutlines> categoryIdToSubscriptionOutlinesMap = mapCategoryIdToSubscriptionOutlines(subscriptions);

		// export root categories
		for (FeedCategory cat : categories) {
			if (cat.getParent() == null) {
				opml.getOutlines().add(buildCategoryOutline(cat, categoryIdToSubscriptionOutlinesMap));
			}
		}

		// export root subscriptions
		if (categoryIdToSubscriptionOutlinesMap.containsKey(null)) {
		    opml.getOutlines().addAll(categoryIdToSubscriptionOutlinesMap.get(null));
		}

		return opml;
    }
    
    private Map<Long, SubscriptionOutlines> mapCategoryIdToSubscriptionOutlines(List<FeedSubscription> subscriptions) {
        Map<Long, SubscriptionOutlines> map = new HashMap<>();
        
        for(FeedSubscription subscription : subscriptions) {
            Long categoryId = subscription.getCategory() == null ? null : subscription.getCategory().getId();
            
            SubscriptionOutlines outlines = map.get(categoryId) == null ? new SubscriptionOutlines() : map.get(categoryId);
            outlines.add(buildSubscriptionOutline(subscription));
            
            if (!map.containsKey(categoryId)) {
                map.put(categoryId, outlines);
            }
        }
        
        return map;
    }

	private Outline buildCategoryOutline(FeedCategory cat, Map<Long, SubscriptionOutlines> categoryIdToSubscriptionOutlinesMap) {
		Outline outline = new Outline();
		outline.setText(cat.getName());
		outline.setTitle(cat.getName());

		for (FeedCategory child : cat.getChildren()) {
			outline.getChildren().add(buildCategoryOutline(child, categoryIdToSubscriptionOutlinesMap));
		}

		Long categoryId = cat.getId();
		if (categoryIdToSubscriptionOutlinesMap.containsKey(categoryId)) {
		    outline.getChildren().addAll(categoryIdToSubscriptionOutlinesMap.get(categoryId));
		}
		
		return outline;
	}

	private Outline buildSubscriptionOutline(FeedSubscription sub) {
		Outline outline = new Outline();
		outline.setText(sub.getTitle());
		outline.setTitle(sub.getTitle());
		outline.setType("rss");
		outline.getAttributes().add(new Attribute("xmlUrl", sub.getFeed().getUrl()));
		if (sub.getFeed().getLink() != null) {
			outline.getAttributes().add(new Attribute("htmlUrl", sub.getFeed().getLink()));
		}
		return outline;
	}
	
	@SuppressWarnings("serial")
    private static class SubscriptionOutlines extends ArrayList<Outline> {}
}