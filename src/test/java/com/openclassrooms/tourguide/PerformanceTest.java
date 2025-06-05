package com.openclassrooms.tourguide;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.openclassrooms.tourguide.manager.InternalUsersManager;
import com.openclassrooms.tourguide.service.libs.GpsUtilService;
import com.openclassrooms.tourguide.service.model.*;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.openclassrooms.tourguide.model.user.User;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class PerformanceTest {

    /*
     * A note on performance improvements:
     *
     * The number of users generated for the high volume tests can be easily
     * adjusted via this method:
     *
     * InternalUsersManager.initializeInternalUsers(100000);
     *
     *
     * These tests can be modified to suit new solutions, just as long as the
     * performance metrics at the end of the tests remain consistent.
     *
     * These are performance metrics that we are trying to hit:
     *
     * highVolumeTrackLocation: 100,000 users within 15 minutes:
     * assertTrue(TimeUnit.MINUTES.toSeconds(15) >=
     * TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
     *
     * highVolumeGetRewards: 100,000 users within 20 minutes:
     * assertTrue(TimeUnit.MINUTES.toSeconds(20) >=
     * TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
     */

    private static final Logger logger = LoggerFactory.getLogger(PerformanceTest.class);

    @Autowired
    private GpsUtilService gpsUtilService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRewardService userRewardService;

    @BeforeEach
    public void beforeEach() {
        InternalUsersManager.getInternalUsersMap().clear();
    }

    @Disabled
    @Test
    public void highVolumeTrackLocation() {

        // Users should be incremented up to 100,000 and test finishes within 15 minutes
        InternalUsersManager.initializeInternalUsers(10000);

        List<User> allUsers = userService.getAllUsers();
        List<CompletableFuture<VisitedLocation>> locationsFutures = new ArrayList<>();
        StopWatch stopWatch = new StopWatch();

        stopWatch.start();
        for (User user : allUsers) {
            CompletableFuture<VisitedLocation> visitedLocation = userService.trackUserLocation(user);
            locationsFutures.add(visitedLocation);
        }
        CompletableFuture<Void> allOf = CompletableFuture.allOf(locationsFutures.toArray(new CompletableFuture[0]));
        allOf.join();
        stopWatch.stop();

        logger.info("highVolumeTrackLocation: Time Elapsed: {} seconds.", stopWatch.getDuration().toSeconds());
        assertTrue(stopWatch.getDuration().toMinutes() < 15);
    }

    @Disabled
    @Test
    public void highVolumeGetRewards() {

        // Users should be incremented up to 100,000 and test finishes within 20 minutes
        InternalUsersManager.initializeInternalUsers(100_000);
        List<User> allUsers = userService.getAllUsers();
        Attraction attraction = gpsUtilService.getAttractions().get(0);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        StopWatch stopWatch = new StopWatch();

        stopWatch.start();
        allUsers.forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));
        for (User user : allUsers) {
            CompletableFuture<Void> visitedLocationFuture = userRewardService.calculateRewards(user);
            futures.add(visitedLocationFuture);
        }
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allOf.join();
        stopWatch.stop();

        for (User user : allUsers) {
            assertFalse(user.getUserRewards().isEmpty());
        }
        logger.info("highVolumeGetRewards: Time Elapsed: {} seconds.", stopWatch.getDuration().toSeconds());
        assertTrue(stopWatch.getDuration().toMinutes() < 20);
    }
}
