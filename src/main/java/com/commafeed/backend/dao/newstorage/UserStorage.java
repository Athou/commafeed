package com.commafeed.backend.dao.newstorage;

import com.commafeed.backend.dao.datamigrationtoggles.MigrationToggles;
import com.commafeed.backend.model.User;
import java.util.Objects;

public class UserStorage implements
        IStorageModelDAO<User> {

    private GenericStorage<Long, User> storage;
    private static UserStorage instance;

    private UserStorage() {
        // Provide the name of the file it will be serialized to
        this.storage = new GenericStorage<Long, User>("UserSettings");
    }

    public static UserStorage getInstance() {
        if (instance == null) {
            instance = new UserStorage();
        }
        return instance;
    }
    public static UserStorage getTestInstance() {
        return new UserStorage();
    }

    @Override
    public boolean exists(User model) {
        return this.storage.exists(model.getId());
    }

    @Override
    public void create(User model) {
        this.storage.create(model.getId(), model);
    }

    @Override
    public User read(Long id) {
        return this.storage.read(id);
    }

    @Override
    public User read(User model) {
        return read(model.getId());
    }

    @Override
    public User update(User model) {
        return this.storage.update(model.getId(), model);
    }

    @Override
    public User delete(User model) {
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

    //TODO: DO WE REALLY NEED THIS
    @Override
    public int hashCode() {
        return Objects.hash(storage);
    }

    /**
     * This method will act as a consistency checker
     * @param model
     * @return true -> if consistency is ok or was corrected, false if failure to fix
     */
    @Override
    public boolean isModelConsistent(User model) {
        if (MigrationToggles.isConsistencyCheckerOn()) {
            User modelFromStorage = read(model);
            if (model.equals(modelFromStorage)) {
                return true;
            } else {
                update(model);
                verification(model, modelFromStorage);
                return false;
            }
        }
        return true;
    }

    public void verification(User expected, User received) {
        System.out.println("Inconsistency found!\n\nObject in real database: " +
                "" + expected.toString() +
                "\n\nObject found in new storage: " + received.toString());
    }
}
