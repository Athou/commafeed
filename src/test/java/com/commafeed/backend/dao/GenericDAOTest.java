package com.commafeed.backend.dao;

import com.commafeed.backend.dao.datamigrationtoggles.MigrationToggles;
import com.commafeed.backend.dao.newstorage.UserSettingsStorage;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserSettings;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Date;

public class GenericDAOTest extends AbstractDAOTest {

    private static UserSettingsDAO userSettingsDAO;
    private static UserDAO userDAO;
    private static UserSettings userSettings1;
    private static UserSettings userSettings2;
    private static UserSettings userSettings3;
    private static User user1;
    private static User user2;
    private static User user3;
    private static User user4;
    private static User user5;
    private UserSettingsStorage storage;
    private static Session userSession;
    private static Session userSettingSession;

    @BeforeClass
    public static void beforeClass() {
        SessionFactory userSessionFactory = createSessionFactory(User.class);
        userSession = userSessionFactory.getCurrentSession();
        userDAO = new UserDAO(createSessionFactory(User.class));

//        List<Class> classList = new ArrayList<>();
//        classList.add(UserSettings.class);
//        classList.add(User.class);
//        SessionFactory userSettingsSessionFactory = createSessionFactory
//                (classList);
//
//        userSettingSession = userSettingsSessionFactory.getCurrentSession();
//        userSettingsDAO = new UserSettingsDAO(userSettingsSessionFactory);
        MigrationToggles.turnAllTogglesOff();
        // Creating all the users in the database
        user1 = getUser("Hello", "Hello@gmail.com");
        user2 = getUser("Hi", "Hi@gmail.com");
        user3 = getUser("Bonjour", "Bonjour@gmail.com");
        user4 = getUser("Salut", "Salut@gmail.com");
        user5 = getUser("Bonjourno", "Bonjourno@gmail.com");


        // DB TRANSACTIONS
        userSession.beginTransaction();
        userDAO.saveOrUpdate(user1);
//        userDAO.saveOrUpdate(user2);
//        userDAO.saveOrUpdate(user3);
//        userDAO.saveOrUpdate(user4);
//        userDAO.saveOrUpdate(user5);
//        userSession.close();
//        userSettings1 = getUserSettings(user1, "English",
//                true);
//        userSettings2 = getUserSettings(user2, "English",
//                false);
//        userSettings3 = getUserSettings(user3, "French",
//                false);
//        userSettingSession.beginTransaction();
//        userSettingsDAO.saveOrUpdate(userSettings1);
//        userSettingsDAO.saveOrUpdate(userSettings2);
//        userSettingsDAO.saveOrUpdate(userSettings3);
//        userSettingSession.close();
    }

    @Before
    public void beforeEachTest() {
        // Here we inject an empty storage map to be used only for the test
        // so that each test is independent of each other
        this.storage = UserSettingsStorage.getTestInstance();
        userSettingsDAO.supercedeIStorageModelDAOForTests(storage);
    }

    @AfterClass
    public static void afterClass() {
//        userSession.beginTransaction();
        userDAO.delete(user1);
//        userDAO.delete(user2);
//        userDAO.delete(user3);
//        userDAO.delete(user4);
//        userDAO.delete(user5);
        userSession.close();
//        userSettingSession.beginTransaction();
//        userSettingsDAO.delete(userSettings1);
//        userSettingsDAO.delete(userSettings2);
//        userSettingsDAO.delete(userSettings3);
//        userSettingSession.close();
    }

    @Test
    public void test(){

    }

    @Test
    public void testShadowWrite() {
        // Turning the toggle for shadow writes on
        MigrationToggles.turnShadowWritesOn();

        // Creating the user settings
        UserSettings userSettings1 = getUserSettings(user4, "Spanish", true);
        UserSettings userSettings2 = getUserSettings(user5, "Italian", false);

        // Saving the user settings
        userSettingsDAO.saveOrUpdate(userSettings1);
        userSettingsDAO.saveOrUpdate(userSettings2);

        // Fetching the user settings from the implemented new storage
        UserSettings storageUserSettings1 = this.storage.read(userSettings1);
        UserSettings storageUserSettings2 = this.storage.read(userSettings2);

        assert(storageUserSettings1.equals(userSettings1));
        assert(storageUserSettings2.equals(userSettings2));

        MigrationToggles.turnAllTogglesOff();
    }

    private static UserSettings getUserSettings(User user,
                                                String language, boolean
            facebook) {
        UserSettings userSettings = new UserSettings();
        userSettings.setUser(user);
        userSettings.setBuffer(false);
        userSettings.setCustomCss("CustomCSS");
        userSettings.setEmail(false);
        userSettings.setFacebook(facebook);
        userSettings.setGmail(false);
        userSettings.setGoogleplus(true);
        userSettings.setInstapaper(false);
        userSettings.setLanguage(language);
        userSettings.setPocket(true);
        userSettings.setReadability(false);
        userSettings.setReadingMode(UserSettings.ReadingMode.all);
        userSettings.setReadingOrder(UserSettings.ReadingOrder.abc);
        userSettings.setScrollMarks(true);
        userSettings.setScrollSpeed(2);
        userSettings.setShowRead(false);
        userSettings.setTheme("Nice");
        userSettings.setTumblr(true);
        userSettings.setTwitter(false);
        userSettings.setViewMode(UserSettings.ViewMode.title);
        return userSettings;
    }

    private static User getUser(String name, String email) {
        User user = new User();
        Date date = new Date(000000000);
        user.setApiKey("ApiKey");
        user.setCreated(date);
        user.setDisabled(false);
        user.setEmail(email);
        Date dateFullRefresh = new Date(000000123);
        user.setLastFullRefresh(dateFullRefresh);
        Date dateLastLogin = new Date(001234567);
        user.setLastLogin(dateLastLogin);
        user.setName(name);
        byte[] passwordArray = "Hello".getBytes();
        user.setPassword(passwordArray);
        user.setRecoverPasswordToken("token");
        Date dateToken = new Date(12345678);
        user.setRecoverPasswordTokenDate(dateToken);
        byte[] saltArray = "Salt".getBytes();
        user.setSalt(saltArray);
        return user;
    }
}
