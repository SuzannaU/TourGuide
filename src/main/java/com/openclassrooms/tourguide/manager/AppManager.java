package com.openclassrooms.tourguide.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Locale;

@Component
public class AppManager {
    private final Logger logger = LoggerFactory.getLogger(AppManager.class);

    private final InternalUsersManager internalUsersManager;
    private final TrackerManager trackerManager;
    private final Environment environment;
    private static int internalUserNumber = 300;


    public AppManager(InternalUsersManager internalUsersManager, TrackerManager trackerManager, Environment environment) {
        this.internalUsersManager = internalUsersManager;
        this.trackerManager = trackerManager;
        this.environment = environment;

        Locale.setDefault(Locale.US);

        // Check active profile to choose what to initialize
        String[] activeProfiles = environment.getActiveProfiles();
        if (Arrays.asList(activeProfiles).contains("test")) {
            logger.info("Active profile: test");
        } else if (Arrays.asList(activeProfiles).contains("dev")) {
            InternalUsersManager.initializeInternalUsers(internalUserNumber);
            trackerManager.initializeTracker();
            logger.info("Active profile: dev");
        } else {
            InternalUsersManager.initializeInternalUsers(internalUserNumber);
            trackerManager.initializeTracker();
            logger.info("Other active profile");
        }
    }

    public static void setInternalUserNumber(int internalUserNumber) {
        AppManager.internalUserNumber = internalUserNumber;
    }

    public static int getInternalUserNumber() {
        return internalUserNumber;
    }
}
