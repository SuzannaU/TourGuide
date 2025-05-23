package com.openclassrooms.tourguide;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.openclassrooms.tourguide.manager.AppManager;
import com.openclassrooms.tourguide.manager.InternalUsersManager;
import com.openclassrooms.tourguide.service.libs.GpsUtilService;
import com.openclassrooms.tourguide.service.model.*;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.openclassrooms.tourguide.model.user.User;

@SpringBootTest
public class TestPerformance {
	private static final Logger logger = LoggerFactory.getLogger(TestPerformance.class);

    /*
     * A note on performance improvements:
     *
     * The number of users generated for the high volume tests can be easily
     * adjusted via this method:
     *
     * InternalTestHelper.setInternalUserNumber(100000);
     *
     *
     * These tests can be modified to suit new solutions, just as long as the
     * performance metrics at the end of the tests remains consistent.
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

    private static final Logger logger = LoggerFactory.getLogger(TestPerformance.class);

    @Autowired
    private GpsUtilService gpsUtilService;
    @Autowired
    private UserService userService;

    @BeforeAll
    public static void setUpClass() {
        AppManager.testMode = true;
    }

    @AfterEach
    public void afterEach() {
        InternalUsersManager.getInternalUserMap().clear();
    }

    //@Disabled
    @Test
    public void highVolumeTrackLocation() {

        // Users should be incremented up to 100,000, and test finishes within 15 minutes
        InternalUsersManager.initializeInternalUsers(10);

        List<User> allUsers = userService.getAllUsers();
        logger.info("allUsers size {}", allUsers.size());

		StopWatch stopWatch = new StopWatch();
		List<CompletableFuture<VisitedLocation>> locations = new ArrayList<>();
		stopWatch.start();
		for (User user : allUsers) {
			CompletableFuture<VisitedLocation> visitedLocation = tourGuideService.trackUserLocation2(user);
			locations.add(visitedLocation);
			System.out.println("size of VisitedLocations: " + user.getVisitedLocations().size());
		}
		CompletableFuture<Void> allOf =  CompletableFuture.allOf(locations.toArray(new CompletableFuture[0]));
		allOf.get();
		stopWatch.stop();
		tourGuideService.tracker.stopTracking();

        logger.info("highVolumeTrackLocation: Time Elapsed: {} seconds.", TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
        assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
    }

	@Disabled
	@Test
	public void highVolumeGetRewards() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardCentral rewardCentral = new RewardCentral();
		RewardsService rewardsService = new RewardsService(gpsUtil, rewardCentral);

        // Users should be incremented up to 100,000, and test finishes within 20 minutes
        InternalUsersManager.initializeInternalUsers(10);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Attraction attraction = gpsUtilService.getAttractions().get(0);
        List<User> allUsers = userService.getAllUsers();
        allUsers.forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));

        allUsers.forEach(userService::calculateRewards);

        for (User user : allUsers) {
            assertTrue(user.getUserRewards().size() > 0);
        }
        stopWatch.stop();
		for (User user : allUsers) {
			logger.info("user has {} UserRewards", user.getUserRewards().size());
			assertTrue(user.getUserRewards().size() > 0);
		}
		stopWatch.stop();
		tourGuideService.tracker.stopTracking();

        logger.info("highVolumeGetRewards: Time Elapsed: {} seconds.", TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
        assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
    }
}
