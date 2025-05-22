package com.openclassrooms.tourguide;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.openclassrooms.tourguide.manager.InternalUsersManager;
import com.openclassrooms.tourguide.manager.TrackerManager;
import com.openclassrooms.tourguide.service.libs.GpsUtilService;
import com.openclassrooms.tourguide.service.libs.RewardCentralService;
import com.openclassrooms.tourguide.tracker.Tracker;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.manager.InternalTestHelper;
import com.openclassrooms.tourguide.service.model.RewardsService;
import com.openclassrooms.tourguide.service.model.TourGuideService;
import com.openclassrooms.tourguide.model.user.User;

public class TestPerformance {

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

	@Disabled
	@Test
	public void highVolumeTrackLocation() {
		GpsUtil gpsUtil = new GpsUtil();
		GpsUtilService gpsUtilService = new GpsUtilService(gpsUtil);
		RewardCentral rewardCentral = new RewardCentral();
		RewardCentralService rewardCentralService = new RewardCentralService(rewardCentral);
		RewardsService rewardsService = new RewardsService(gpsUtilService, rewardCentralService);
		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardCentralService, rewardsService);
		Tracker tracker = new Tracker(tourGuideService);
		TrackerManager trackerManager = new TrackerManager(tracker);

		// Users should be incremented up to 100,000, and test finishes within 15
		// minutes
		InternalUsersManager.setInternalUserNumber(100);

		List<User> allUsers = new ArrayList<>();
		allUsers = tourGuideService.getAllUsers();

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		for (User user : allUsers) {
			tourGuideService.trackUserLocation(user);
		}
		stopWatch.stop();
		trackerManager.stopTracking();

		System.out.println("highVolumeTrackLocation: Time Elapsed: "
				+ TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}

	@Disabled
	@Test
	public void highVolumeGetRewards() {
		GpsUtil gpsUtil = new GpsUtil();
		GpsUtilService gpsUtilService = new GpsUtilService(gpsUtil);
		RewardCentral rewardCentral = new RewardCentral();
		RewardCentralService rewardCentralService = new RewardCentralService(rewardCentral);
		RewardsService rewardsService = new RewardsService(gpsUtilService, rewardCentralService);
		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardCentralService, rewardsService);
		Tracker tracker = new Tracker(tourGuideService);
		TrackerManager trackerManager = new TrackerManager(tracker);

		// Users should be incremented up to 100,000, and test finishes within 20
		// minutes
		InternalUsersManager.setInternalUserNumber(100);
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		Attraction attraction = gpsUtilService.getAttractions().get(0);
		List<User> allUsers = new ArrayList<>();
		allUsers = tourGuideService.getAllUsers();
		allUsers.forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));

		allUsers.forEach(u -> rewardsService.calculateRewards(u));

		for (User user : allUsers) {
			assertTrue(user.getUserRewards().size() > 0);
		}
		stopWatch.stop();
		trackerManager.stopTracking();

		System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime())
				+ " seconds.");
		assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}

}
