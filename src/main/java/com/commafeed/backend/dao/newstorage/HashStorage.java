package com.commafeed.backend.dao.newstorage;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

public class HashStorage<Key, Value> implements Serializable {

    private HashMap<Key, Value> hashMap;

    public HashStorage() {
        this.hashMap = new HashMap();
    }

    // CRUD + exists + serialize?

    public boolean exists(Key key) {
        return this.hashMap.containsKey(key);
    }

    public void create(Key key, Value value) {
        this.hashMap.put(key, value);
    }

    public Value read(Key key) {
        Value returnValue = null;
        if (exists(key)) {
            returnValue = this.hashMap.get(key);
        }
        return returnValue;
    }

    public Value update(Key key, Value value) {
        return this.hashMap.replace(key, value);
    }

    public Value delete(Key key) {
        return this.hashMap.remove(key);
    }

     private void writeObject(java.io.ObjectOutputStream out) throws
             IOException {
        out.writeObject(hashMap);
     }
    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        this.hashMap = (HashMap) in.readObject();
    }
}
