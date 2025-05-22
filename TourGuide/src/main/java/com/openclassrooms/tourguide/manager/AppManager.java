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
    boolean testMode = true;

    public AppManager(InternalUsersManager internalUsersManager, TrackerManager trackerManager) {
        this.internalUsersManager = internalUsersManager;
        this.trackerManager = trackerManager;

        Locale.setDefault(Locale.US);

        if (testMode) {
            logger.info("TestMode enabled");
            internalUsersManager.initializeInternalUsers();
            trackerManager.initializeTracker();
        }
    }
}
