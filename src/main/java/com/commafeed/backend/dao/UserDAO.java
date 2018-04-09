package com.commafeed.backend.dao;

import com.commafeed.backend.dao.datamigrationtoggles.MigrationToggles;
import com.commafeed.backend.model.QUser;
import com.commafeed.backend.model.User;
import org.hibernate.SessionFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

@Singleton
public class UserDAO extends GenericDAO<User> {

	private QUser user = QUser.user;
	private HashMap<Long, User> longTermHashMap;

	@Inject
	public UserDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	public User findByName(String name) {
		return query().selectFrom(user).where(user.name.equalsIgnoreCase(name)).fetchOne();
	}

	public User findByApiKey(String key) {
		return query().selectFrom(user).where(user.apiKey.equalsIgnoreCase(key)).fetchOne();
	}

	public User findByEmail(String email) {
		return query().selectFrom(user).where(user.email.equalsIgnoreCase(email)).fetchOne();
	}

	public long count() {
		return query().selectFrom(user).fetchCount();
	}

	public List<User> findAll() {
		return query().selectFrom(user).fetch();
	}

	public void forklift() {
		if (MigrationToggles.isForkLiftOn()) {
			List<User> users = findAll();
			for(User user: users) {
				saveOrUpdateToStorage(user);
			}
		}
	}

	public int consistencyChecker() {
		int inconsistencyCounter = 0;
		if (MigrationToggles.isLongTermConsistencyOn()) {
			Collection<User> users = this.longTermHashMap.values();
			for(User user: users) {
				if (!this.storage.isModelConsistent(user)) {
					++inconsistencyCounter;
				}
			}
		} else {
			if (MigrationToggles.isConsistencyCheckerOn()) {
				List<User> users = findAll();
				for(User user: users) {
					if (!this.storage.isModelConsistent(user)) {
						++inconsistencyCounter;
					}
				}
			}
		}
		return inconsistencyCounter;
	}

	public void setLongTermHashMap(HashMap<Long, User> hashMap) {
		this.longTermHashMap = hashMap;
	}
}
