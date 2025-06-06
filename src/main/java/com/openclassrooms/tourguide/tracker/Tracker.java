package com.openclassrooms.tourguide.tracker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.openclassrooms.tourguide.manager.TrackerManager;
import com.openclassrooms.tourguide.service.model.UserService;
import gpsUtil.location.VisitedLocation;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openclassrooms.tourguide.model.user.User;
import org.springframework.stereotype.Component;

/**
 * Is responsible for tracking users' location at specified intervals.
 * Runs in its own separate Thread.
 * @see TrackerManager
 */
@Component
public class Tracker implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(Tracker.class);
    private final UserService userService;
    private static final long TRACKING_POLLING_INTERVAL = TimeUnit.MINUTES.toSeconds(5);

    public Tracker(UserService UserService) {
        this.userService = UserService;
    }

    /**
     * Tracks users' location according to the polling interval, sleeps in between polling
     *
     * @see UserService
     */
    @Override
    public void run() {
        StopWatch stopWatch = new StopWatch();
        while (true) {
            if (Thread.currentThread().isInterrupted()) {
                logger.debug("Tracker stopping");
                break;
            }

            List<User> users = userService.getAllUsers();
            logger.debug("Begin Tracker. Tracking {} users.", users.size());

            stopWatch.start();
            List<CompletableFuture<VisitedLocation>> locationsFutures = new ArrayList<>();
            for (User user : users) {
                CompletableFuture<VisitedLocation> visitedLocation = userService.trackUserLocation(user);
                locationsFutures.add(visitedLocation);
            }
            CompletableFuture<Void> allOf = CompletableFuture.allOf(locationsFutures.toArray(new CompletableFuture[0]));
            allOf.join();
            stopWatch.stop();
            logger.debug("Tracker Time Elapsed: {} seconds.", stopWatch.getDuration().getSeconds());
            stopWatch.reset();
            try {
                logger.debug("Tracker sleeping");
                TimeUnit.SECONDS.sleep(TRACKING_POLLING_INTERVAL);
            } catch (InterruptedException e) {
                logger.debug("Thread interrupted");
                break;
            }
        }
    }
}
