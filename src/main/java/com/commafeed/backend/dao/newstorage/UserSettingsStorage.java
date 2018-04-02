package com.commafeed.backend.dao.newstorage;

import com.commafeed.backend.model.UserSettings;

public class UserSettingsStorage extends GenericStorage<String, UserSettings> {

    private static UserSettingsStorage instance;

    private UserSettingsStorage() {
        // Provide the name of the file it will be serialized to
        super("UserSettings");
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
}
