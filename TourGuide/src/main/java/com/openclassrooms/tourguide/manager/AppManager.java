package com.openclassrooms.tourguide.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class AppManager {
    private final Logger logger = LoggerFactory.getLogger(AppManager.class);

    private final InternalUsersManager internalUsersManager;
    private final TrackerManager trackerManager;

    // Set this default up to 100,000 for testing
    private static int internalUserNumber = 3;
    public static boolean testMode = false;

    public AppManager(InternalUsersManager internalUsersManager, TrackerManager trackerManager) {
        this.internalUsersManager = internalUsersManager;
        this.trackerManager = trackerManager;

        Locale.setDefault(Locale.US);

        if (testMode) {
            logger.info("TestMode enabled");
        } else {
            InternalUsersManager.initializeInternalUsers(internalUserNumber);
            trackerManager.initializeTracker();
        }
    }

    public static void setInternalUserNumber(int internalUserNumber) {
        AppManager.internalUserNumber = internalUserNumber;
    }

    public static int getInternalUserNumber() {
        return internalUserNumber;
    }
}
