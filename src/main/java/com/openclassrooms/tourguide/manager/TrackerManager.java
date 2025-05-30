package com.openclassrooms.tourguide.manager;

import com.openclassrooms.tourguide.tracker.Tracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class TrackerManager {
    private static final Logger logger = LoggerFactory.getLogger(TrackerManager.class);
    private static boolean stop = false;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Tracker tracker;

    public TrackerManager(Tracker tracker) {
        this.tracker = tracker;
    }

    void initializeTracker () {
        executorService.submit(tracker);
        addShutDownHook();
    }

    void addShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                stop = true;
                executorService.shutdownNow();
                logger.info("Tracker stopped");
            }
        });
    }

    public void stopTracking() {
        stop = true;
        executorService.shutdownNow();
    }

    public static boolean isTrackerStopped() {
        return stop;
    }
}
