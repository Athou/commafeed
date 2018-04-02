package com.commafeed.backend.dao.newstorage;

import com.commafeed.backend.model.UserSettings;

public class UserSettingsStorage implements
        IStorageModelDAO<UserSettings> {

    private UserSettings model;
    private GenericStorage<Long, UserSettings> storage;
    private static UserSettingsStorage instance;

    private UserSettingsStorage() {
        // Provide the name of the file it will be serialized to
        this.storage = new GenericStorage<Long, UserSettings>("UserSettings");
    }

    public static UserSettingsStorage getInstance() {
        if (instance == null) {
            instance = new UserSettingsStorage();
        }
        return instance;
    }

    public static UserSettingsStorage getTestInstance() {
        return new UserSettingsStorage();
    }

    @Override
    public boolean exists(UserSettings model) {
        return this.storage.exists(model.getUser().getId());
    }

    @Override
    public void create(UserSettings model) {
        this.storage.create(model.getUser().getId(), model);
    }

    @Override
    public UserSettings read(Long id) {
        return this.storage.read(id);
    }

    @Override
    public UserSettings read(UserSettings model) {
        return read(model.getUser().getId());
    }

    @Override
    public UserSettings update(UserSettings model) {
        return this.storage.update(model.getUser().getId(), model);
    }

    @Override
    public UserSettings delete(UserSettings model) {
        return this.storage.delete(model.getUser().getId());
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
    public boolean isModelConsistent(UserSettings model) {
        UserSettings modelFromStorage = read(model);
        if (model.equals(modelFromStorage)) {
            return true;
        } else {
            update(model);
            verification(model, modelFromStorage);
            return false;
        }
    }

    public void verification(UserSettings expected, UserSettings received) {
        System.out.println("Inconsistency found!\n\nObject in real database: " +
                "" + expected +
                "\n\nObject found in new storage: " + received);
    }
}
