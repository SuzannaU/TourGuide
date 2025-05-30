package com.openclassrooms.tourguide.tracker;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.openclassrooms.tourguide.manager.TrackerManager;
import com.openclassrooms.tourguide.service.model.UserService;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openclassrooms.tourguide.model.user.User;
import org.springframework.stereotype.Component;

@Component
public class Tracker extends Thread {
    private final Logger logger = LoggerFactory.getLogger(Tracker.class);
    private static final long trackingPollingInterval = TimeUnit.MINUTES.toSeconds(5);
    private final UserService UserService;

    public Tracker(UserService UserService) {
        this.UserService = UserService;
    }

    @Override
    public void run() {
        StopWatch stopWatch = new StopWatch();
        while (true) {
            if (Thread.currentThread().isInterrupted() || TrackerManager.isTrackerStopped()) {
                logger.debug("Tracker stopping");
                break;
            }

            List<User> users = UserService.getAllUsers();
            logger.debug("Begin Tracker. Tracking {} users.", users.size());
            stopWatch.start();
            users.forEach(UserService::trackUserLocation);
            stopWatch.stop();
            logger.debug("Tracker Time Elapsed: {} seconds.", TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
            stopWatch.reset();
            try {
                logger.debug("Tracker sleeping");
                TimeUnit.SECONDS.sleep(trackingPollingInterval);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
