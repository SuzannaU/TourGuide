package com.openclassrooms.tourguide.manager;

import com.openclassrooms.tourguide.tracker.Tracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Handles Tracker: initialization in dev and prod profiles, shutdown
 * @see Tracker
 *
 */
@Component
public class TrackerManager {
    private static final Logger logger = LoggerFactory.getLogger(TrackerManager.class);
    private static boolean stop = false;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Tracker tracker;

    public TrackerManager(Tracker tracker) {
        this.tracker = tracker;
    }

    /**
     * Submits Tracker to executor service to initialize it.
     * Creates shutDownHook to handle shut down when the application stops.
     */
    void initializeTracker () {
        executorService.submit(tracker);
        addShutDownHook();
    }

    void addShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            stop = true;
            executorService.shutdownNow();
            logger.info("Tracker stopped");
        }));
    }
}
