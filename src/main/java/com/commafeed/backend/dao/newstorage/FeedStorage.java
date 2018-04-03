package com.commafeed.backend.dao.newstorage;

import com.commafeed.backend.model.Feed;
import java.util.Objects;

public class FeedStorage implements
        IStorageModelDAO<Feed> {


    private GenericStorage<Long, Feed> storage;
    private static FeedStorage instance;

    private FeedStorage(){
        this.storage = new GenericStorage<Long, Feed>("Feed");
    }
    public static FeedStorage getInstance(){
        if(instance == null){
            instance = new FeedStorage();
        }
        return instance;
    }


    @Override
    public boolean exists(Feed model) {
        return this.storage.exists(model.getId());
    }

    @Override
    public void create(Feed model) {
        this.storage.create(model.getId(), model);
    }

    @Override
    public Feed read(Feed model) {
        return this.storage.read(model.getId());
    }

    @Override
    public Feed read(Long id) {
        return this.storage.read(id);
    }

    @Override
    public Feed update(Feed model) {
        return this.storage.update(model.getId(), model);
    }

    @Override
    public Feed delete(Feed model) {
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

    /**
     * This method will act as a consistency checker
     * @param model
     * @return true -> if consistency is ok or was corrected, false if failure to fix
     */
    @Override
    public boolean isModelConsistent(Feed model) {
        Feed feedImported = read(model);
        if(model.equals(feedImported)){
            return true;
        }else{
            update(model);
            verification(model, feedImported);
            return false;
        }
    }

    /**
     * This method will act as a log system
     * @param model
     * @param feedImported
     */
    private void verification(Feed model, Feed feedImported) {
        System.out.println("Inconsistency found!\n\nObject in real database: " +
                "" + model.toString() +
                "\n\nObject found in new storage: " + feedImported.toString());
    }
}
