package com.commafeed.backend.dao.newstorage;

import com.commafeed.backend.dao.datamigrationtoggles.MigrationToggles;
import com.commafeed.backend.model.FeedEntryTag;


public class FeedEntryTagStorage implements
IStorageModelDAO<FeedEntryTag>{
	
	private GenericStorage<Long, FeedEntryTag> storage;
    private static FeedEntryTagStorage instance;
    
    private FeedEntryTagStorage(){
        this.storage = new GenericStorage<Long, FeedEntryTag>("FeedEntryTag");
    }
    
    public static FeedEntryTagStorage getInstance() {
        if (instance == null) {
            instance = new FeedEntryTagStorage();
        }
        return instance;
    }

	@Override
	public boolean exists(FeedEntryTag model) {
		return this.storage.exists(model.getId());
	}

	@Override
	public void create(FeedEntryTag model) {
		 this.storage.create(model.getId(), model);
	}

	@Override
	public FeedEntryTag read(FeedEntryTag model) {
		 return this.storage.read(model.getId());
	}

	@Override
	public FeedEntryTag read(Long id) {
		return this.storage.read(id);
	}

	@Override
	public FeedEntryTag update(FeedEntryTag model) {
		 return this.storage.update(model.getId(), model);
	}

	@Override
	public FeedEntryTag delete(FeedEntryTag model) {
		return this.storage.delete(model.getId());
	}

	@Override
	public void serialize() {
		 this.storage.saveStorage();
		
	}

	@Override
	public void deserialize() {
		 this.storage.loadStorage();
		
	}

	@Override
	public boolean isModelConsistent(FeedEntryTag model) {
		if (MigrationToggles.isConsistencyCheckerOn()) {
			FeedEntryTag feedTagFromStorage = read(model);
            if (model.equals(feedTagFromStorage)) {
                return true;
            } else {
                update(model);
                verification(model, feedTagFromStorage);
                return false;
            }
        }
        return true;
	}
	
	private void verification(FeedEntryTag model, FeedEntryTag feedTag) {
        System.out.println("Inconsistency found!\n\nObject in real database: " +
                "" + model.toString() +
                "\n\nObject found in new storage: " + feedTag.toString());
    }

}
