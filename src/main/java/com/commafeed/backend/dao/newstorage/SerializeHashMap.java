package com.commafeed.backend.dao.newstorage;

import java.io.*;

public class SerializeHashMap<Key, Value> {

    //Largely inspired by the class notes
    private HashStorage<Key, Value> hashMap;
    private String fileName;
    private String path;

    public SerializeHashMap(
            HashStorage hashMap, String fileName) {
        this.hashMap = hashMap;
        this.fileName = fileName;
        this.path = System.getProperty("user.dir") + "/output/" + fileName +
                ".ser";
    }

    @SuppressWarnings("unchecked")
    public HashStorage<Key, Value> loadMap() {
        try (FileInputStream fileIn = new FileInputStream(path);
             ObjectInputStream in = new ObjectInputStream(fileIn)) {
            hashMap = (HashStorage<Key, Value>)in.readObject();
            return hashMap;
        }
        catch(FileNotFoundException | ClassCastException e) {
            return null;
        }
        catch (IOException i) {
            i.printStackTrace();
            return null;
        } catch (ClassNotFoundException c) {
            c.printStackTrace();
            return null;
        }
    }

    public void persistMap() {

        try(FileOutputStream fileOut = new FileOutputStream(path);
            ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(hashMap);
        }
        catch (IOException i) {
            i.printStackTrace();
            return;
        }
    }
}
