package com.commafeed.backend.dao.newstorage;

public class GenericStorage<Key, Value> {

    private HashStorage hashMap;
    private SerializeHashMap serialize;

    protected GenericStorage(String filename) {
        this.hashMap = new HashStorage<Key, Value>();
        this.serialize = new SerializeHashMap(hashMap, filename);
    }

    protected void loadStorage() {
        this.hashMap = this.serialize.loadMap();
    }

    protected void saveStorage() {
        this.serialize.persistMap();
    }

    protected boolean exists(Key key) {
        return this.hashMap.exists(key);
    }

    protected void create(Key key, Value value) {
        this.hashMap.create(key, value);
    }

    protected Value read(Key key) {
        return (Value) this.hashMap.read(key);
    }

    protected Value update(Key key, Value value) {
        return (Value) this.hashMap.update(key, value);
    }

    protected Value delete(Key key) {
        return (Value) this.hashMap.delete(key);
    }
}
