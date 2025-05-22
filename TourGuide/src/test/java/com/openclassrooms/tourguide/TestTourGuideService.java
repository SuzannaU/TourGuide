package com.openclassrooms.tourguide;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import com.openclassrooms.tourguide.manager.InternalUsersManager;
import com.openclassrooms.tourguide.manager.TrackerManager;
import com.openclassrooms.tourguide.model.NearbyAttraction;
import com.openclassrooms.tourguide.service.libs.GpsUtilService;
import com.openclassrooms.tourguide.service.libs.RewardCentralService;
import com.openclassrooms.tourguide.tracker.Tracker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gpsUtil.GpsUtil;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.manager.InternalTestHelper;
import com.openclassrooms.tourguide.service.model.RewardsService;
import com.openclassrooms.tourguide.service.model.TourGuideService;
import com.openclassrooms.tourguide.model.user.User;
import tripPricer.Provider;

public class TestTourGuideService {

    private TourGuideService tourGuideService;
    private TrackerManager trackerManager;

    @BeforeEach
    public void setup() {

        GpsUtil gpsUtil = new GpsUtil();
        GpsUtilService gpsUtilService = new GpsUtilService(gpsUtil);
        RewardCentral rewardCentral = new RewardCentral();
        RewardCentralService  rewardCentralService = new RewardCentralService(rewardCentral);
        RewardsService rewardsService = new RewardsService(gpsUtilService, rewardCentralService);
        tourGuideService = new TourGuideService(gpsUtilService, rewardCentralService, rewardsService);
        Tracker tracker = new Tracker(tourGuideService);
        trackerManager = new TrackerManager(tracker);

        InternalUsersManager.setInternalUserNumber(0);
    }

    @Test
    public void getUserLocation() {

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);
        trackerManager.stopTracking();
        assertEquals(visitedLocation.userId, user.getUserId());
    }

    @Test
    public void addUser() {

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

        tourGuideService.addUser(user);
        tourGuideService.addUser(user2);

        User retrivedUser = tourGuideService.getUser(user.getUserName());
        User retrivedUser2 = tourGuideService.getUser(user2.getUserName());

        trackerManager.stopTracking();

        assertEquals(user, retrivedUser);
        assertEquals(user2, retrivedUser2);
    }

    @Test
    public void getAllUsers() {

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

        tourGuideService.addUser(user);
        tourGuideService.addUser(user2);

        List<User> allUsers = tourGuideService.getAllUsers();

        trackerManager.stopTracking();

        assertTrue(allUsers.contains(user));
        assertTrue(allUsers.contains(user2));
    }

    @Test
    public void trackUser() {

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);

        trackerManager.stopTracking();

        assertEquals(user.getUserId(), visitedLocation.userId);
    }

    @Test
    public void getNearbyAttractions() {

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        tourGuideService.addUser(user);
        VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);

        List<NearbyAttraction> attractions = tourGuideService.getNearByAttractions(user.getUserName());

        trackerManager.stopTracking();

        assertEquals(5, attractions.size());
    }

    public void getTripDeals() {

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

        List<Provider> providers = tourGuideService.getTripDeals(user);

        trackerManager.stopTracking();

        assertEquals(10, providers.size());
    }

}
