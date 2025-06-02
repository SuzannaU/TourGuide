package com.openclassrooms.tourguide;

import java.util.List;
import java.util.UUID;

import com.openclassrooms.tourguide.manager.InternalUsersManager;
import com.openclassrooms.tourguide.service.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.openclassrooms.tourguide.model.user.User;
import org.springframework.test.context.ActiveProfiles;
import tripPricer.Provider;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class UserServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceTest.class);

    @Autowired
    private UserService userService;

    @BeforeEach
    public void beforeEach() {
        InternalUsersManager.getInternalUserMap().clear();
    }

    @Test
    public void getUserTest() {
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        userService.addUser(user);

        User retrievedUser = userService.getUser("jon");

        assertEquals(user, retrievedUser);
    }

    @Test
    public void getAllUsersTest() {

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

        userService.addUser(user);
        userService.addUser(user2);

        List<User> allUsers = userService.getAllUsers();

        assertTrue(allUsers.contains(user));
        assertTrue(allUsers.contains(user2));
    }

    @Test
    public void addUserTest() {

        User user1 = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");
        User user3 = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

        userService.addUser(user1);
        userService.addUser(user2);
        // user3 userName already exists, should not be added by addUser()
        userService.addUser(user3);

        User retrievedUser1 = userService.getUser(user1.getUserName());
        User retrievedUser2 = userService.getUser(user2.getUserName());

        assertEquals(user1, retrievedUser1);
        assertEquals(user2, retrievedUser2);
        assertEquals(2, userService.getAllUsers().size());
    }

    @Test
    public void getUserLocation_withVisitedLocations() {

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        // adds a VisitedLocation to user
        VisitedLocation trackedLocation = userService.trackUserLocation(user).join();

        VisitedLocation resultLocation = userService.getUserLocation(user);

        assertEquals(resultLocation.userId, user.getUserId());
        assertEquals(trackedLocation, resultLocation);
    }

    @Test
    public void getUserLocation_withEmptyVisitedLocations() {

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

        VisitedLocation resultLocation = userService.getUserLocation(user);

        assertEquals(resultLocation.userId, user.getUserId());
    }

    @Test
    public void trackUserLocationTest() {

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        VisitedLocation visitedLocation = userService.trackUserLocation(user).join();

        assertEquals(user.getUserId(), visitedLocation.userId);
        assertFalse(user.getVisitedLocations().isEmpty());
    }

    @Test
    public void getTripDeals() {

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

        List<Provider> providers = userService.getTripDeals(user);

        assertEquals(5, providers.size());
    }

    @Test
    public void getVisitedLocationsTest() {

        InternalUsersManager.initializeInternalUsers(1);
        User user = userService.getAllUsers().get(0);

        assertEquals(3, user.getVisitedLocations().size());
    }
}
