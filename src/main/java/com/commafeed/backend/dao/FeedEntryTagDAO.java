package com.commafeed.backend.dao;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.SessionFactory;

import com.commafeed.backend.dao.datamigrationtoggles.MigrationToggles;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryTag;
import com.commafeed.backend.model.QFeedEntryTag;
import com.commafeed.backend.model.User;

@Singleton
public class FeedEntryTagDAO extends GenericDAO<FeedEntryTag> {

	private QFeedEntryTag tag = QFeedEntryTag.feedEntryTag;
	private HashMap<Long, FeedEntryTag> longTermHashMap;

	@Inject
	public FeedEntryTagDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	public List<String> findByUser(User user) {
		return query().selectDistinct(tag.name).from(tag).where(tag.user.eq(user)).fetch();
	}

	public List<FeedEntryTag> findByEntry(User user, FeedEntry entry) {
		return query().selectFrom(tag).where(tag.user.eq(user), tag.entry.eq(entry)).fetch();
	}
	
	public List<FeedEntryTag> findAllTags() {
		return query().selectFrom(tag).fetch();
	}
	
	public void forklift() {
		if (MigrationToggles.isForkLiftOn()) {
			List<FeedEntryTag> tags = findAllTags();
			for(FeedEntryTag tag: tags) {
				saveOrUpdateToStorage(tag);
			}
		}
	}
	
	public int consistencyChecker() {
		int inconsistencyCounter = 0;
		//if (MigrationToggles.isConsistencyCheckerOn()) {
			List<FeedEntryTag> tags = findAllTags();
			for(FeedEntryTag tag: tags) {
				if (!this.storage.isModelConsistent(tag)) {
					++inconsistencyCounter;
				}
			}
		//}
		return inconsistencyCounter;
	}

	public void setLongTermHashMap(HashMap<Long, FeedEntryTag> hashMap) {
		this.longTermHashMap = hashMap;
	}
}
