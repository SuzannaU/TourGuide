package com.openclassrooms.tourguide.manager;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class AppManager {
    private final Logger logger = LoggerFactory.getLogger(AppManager.class);

    private final InternalUsersManager internalUsersManager;
    private final TrackerManager trackerManager;
    private final Environment environment;
    private static int internalUserNumber = 300;
    private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
    public static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(AVAILABLE_PROCESSORS * 4);

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

    @PreDestroy
    public void shutdownExecutor() {
        EXECUTOR_SERVICE.shutdown();
    }

    public static void setInternalUserNumber(int internalUserNumber) {
        AppManager.internalUserNumber = internalUserNumber;
    }

    public static int getInternalUserNumber() {
        return internalUserNumber;
    }
}
