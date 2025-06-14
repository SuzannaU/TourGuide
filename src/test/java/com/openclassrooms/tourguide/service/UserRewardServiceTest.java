package com.openclassrooms.tourguide.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.openclassrooms.tourguide.manager.InternalUsersManager;
import com.openclassrooms.tourguide.service.libs.GpsUtilService;
import com.openclassrooms.tourguide.service.model.LocationUtil;
import com.openclassrooms.tourguide.service.model.UserRewardService;
import com.openclassrooms.tourguide.service.model.UserService;
import org.junit.jupiter.api.*;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.openclassrooms.tourguide.model.user.User;
import com.openclassrooms.tourguide.model.user.UserReward;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class UserRewardServiceTest {

    @Autowired
    private GpsUtilService gpsUtilService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRewardService userRewardService;
    @Autowired
    private LocationUtil locationUtil;

    @BeforeEach
    public void beforeEach() {
        locationUtil.setProximityBuffer(locationUtil.getDefaultProximityBuffer());
        InternalUsersManager.getInternalUsersMap().clear();
    }

    @Test
    public void calculateRewardsTest() {

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        Attraction attraction = gpsUtilService.getAttractions().get(0);
        user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));

        userRewardService.calculateRewards(user).join();
        List<UserReward> userRewards = user.getUserRewards();

        assertEquals(1, userRewards.size());
    }

    @Test
    public void getUserRewardsTest() {
        locationUtil.setProximityBuffer(Integer.MAX_VALUE);
        InternalUsersManager.initializeInternalUsers(1);
        User user = userService.getAllUsers().get(0);

        userRewardService.calculateRewards(user).join();
        List<UserReward> userRewards = userRewardService.getUserRewards(user);

        assertEquals(gpsUtilService.getAttractions().size(), userRewards.size());
    }
}
