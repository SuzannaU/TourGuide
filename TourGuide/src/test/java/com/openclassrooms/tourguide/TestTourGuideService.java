package com.openclassrooms.tourguide;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import com.openclassrooms.tourguide.manager.AppManager;
import com.openclassrooms.tourguide.manager.InternalUsersManager;
import com.openclassrooms.tourguide.model.NearbyAttraction;
import com.openclassrooms.tourguide.service.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.openclassrooms.tourguide.model.user.User;
import tripPricer.Provider;

@SpringBootTest
public class TestTourGuideService {
    private static final Logger logger = LoggerFactory.getLogger(TestTourGuideService.class);

    @Autowired
    private UserService userService;
    @Autowired
    private AttractionService attractionService;

    @BeforeAll
    public static void setUpClass() {
        AppManager.testMode = true;
    }

    @AfterEach
    public void afterEach() {
        InternalUsersManager.getInternalUserMap().clear();
    }

    @Test
    public void getUserLocation() {

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        VisitedLocation visitedLocation = userService.trackUserLocation(user);
        assertEquals(visitedLocation.userId, user.getUserId());
    }

    @Test
    public void addUser() {

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

        userService.addUser(user);
        userService.addUser(user2);

        User retrievedUser = userService.getUser(user.getUserName());
        User retrievedUser2 = userService.getUser(user2.getUserName());

        assertEquals(user, retrievedUser);
        assertEquals(user2, retrievedUser2);
    }

    @Test
    public void getAllUsers() {

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

        userService.addUser(user);
        userService.addUser(user2);

        List<User> allUsers = userService.getAllUsers();

        assertTrue(allUsers.contains(user));
        assertTrue(allUsers.contains(user2));
    }

    @Test
    public void trackUser() {

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        VisitedLocation visitedLocation = userService.trackUserLocation(user);

        assertEquals(user.getUserId(), visitedLocation.userId);
    }

    @Test
    public void getNearbyAttractions() {

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        userService.addUser(user);
        VisitedLocation visitedLocation = userService.trackUserLocation(user);

        List<NearbyAttraction> attractions = attractionService.getNearByAttractions(user.getUserName());

        assertEquals(5, attractions.size());
    }

    public void getTripDeals() {

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

        List<Provider> providers = userService.getTripDeals(user.getUserName());

        assertEquals(10, providers.size());
    }

}
