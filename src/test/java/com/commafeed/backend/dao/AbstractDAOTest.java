package com.commafeed.backend.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.fail;

public class AbstractDAOTest {
    private static SessionFactory sessionFactory;
    private static Session session;
    private static Configuration configuration;

    /**
     * Calls the different methods needed to configure hibernate so that the
     * DAO classes are usable.
     * @param classParam
     * @return
     */
    public static SessionFactory createSessionFactory(Class classParam) {
        configureDatabaseSettings();
        addModelToConfiguration(classParam);
        buildSessionFactory();
        return sessionFactory;
    }

    public static SessionFactory createSessionFactory(List<Class> classParams) {
        configureDatabaseSettings();
        addModelsToConfiguration(classParams);
        buildSessionFactory();
        return sessionFactory;
    }

    public static void beginTransaction() {
        session = sessionFactory.getCurrentSession();
        session.beginTransaction();
    }

    public static void closeTransaction() {
        session.close();
    }

    /**
     * Creates the configuration class with the database settings from the
     * config.dev.yml file
     */
    private static void configureDatabaseSettings() {
        // This portion of the configuration should mirror what you have in
        // config.dev.yml under database. We open up the file and
        File initialFile = new File("config.dev.yml");
        String line = null;
        String driverClass = null;
        String url = null;
        String user = null;
        String password = null;
        try (FileReader fileReader = new FileReader(initialFile)) {

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader =
                    new BufferedReader(fileReader);

            // We parse the YML file to get the driverClass, url, user and
            // password in the configs. This is an ugly way to do it but using a
            // more elegant solution using a YAML parser would involve creating
            // numerous new java classes which isn't the goal of the assignment.
            while ((line = bufferedReader.readLine()) != null) {
                Pattern driverClassPattern =
                        Pattern.compile("driverClass: (.*)");
                Pattern urlPattern = Pattern.compile("url: (.*)");
                Pattern userPattern = Pattern.compile("user: (.*)");
                Pattern passwordPattern = Pattern.compile("password: (.*)");
                Matcher matchDriver = driverClassPattern.matcher(line);
                Matcher matchUrl = urlPattern.matcher(line);
                Matcher matchUser = userPattern.matcher(line);
                Matcher matchPassword = passwordPattern.matcher(line);
                if (matchDriver.find()) {
                    driverClass = matchDriver.group(1); // " that is awesome"
                } else if (matchUrl.find()) {
                    url = matchUrl.group(1);
                } else if (matchUser.find()) {
                    user = matchUser.group(1);
                } else if (matchPassword.find()) {
                    password = matchPassword.group(1);
                }
            }
        } catch(IOException e) {
            fail("Error in the test configuration");
        }

        configuration = new Configuration();

        // We set up the properties we extracted from the YAML file
        configuration.setProperty("hibernate.connection.driver_class",
                driverClass);
        configuration.setProperty("hibernate.connection.url", url);
        configuration.setProperty("hibernate.connection.username", user);
        configuration.setProperty("hibernate.connection.password", password);
        // This one is hardcoded because it seems necessary for hibernate to
        // work properly
        configuration.setProperty("hibernate.current_session_context_class",
                "org.hibernate.context.internal.ThreadLocalSessionContext");
    }

    /**
     * Adds the model classes needed to map the data fetched from the
     * database to an actual class
     * @param classToAdd
     */
    private static void addModelToConfiguration(Class classToAdd) {
        configuration.addAnnotatedClass(classToAdd);
    }

    /**
     * Add a list of all the model classes needed to map the data fetched
     * from the database to an actual class
     * @param classesToAdd
     */
    private static void addModelsToConfiguration(List<Class> classesToAdd) {
        for (Class classToAdd: classesToAdd) {
            configuration.addAnnotatedClass(classToAdd);
        }
    }

    /**
     * Creates the SessionFactory class from the configuration class created
     */
    private static void buildSessionFactory() {
        StandardServiceRegistryBuilder
                builder = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties());
        sessionFactory = configuration.buildSessionFactory(builder.build());
    }
}
