package com.openclassrooms.tourguide;

import com.openclassrooms.tourguide.manager.InternalUsersManager;
import com.openclassrooms.tourguide.model.NearbyAttraction;
import com.openclassrooms.tourguide.model.user.User;
import com.openclassrooms.tourguide.service.model.AttractionService;
import com.openclassrooms.tourguide.service.model.UserService;
import gpsUtil.location.VisitedLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class AttractionServiceTest {

    @Autowired
    private AttractionService attractionService;

    @Autowired
    private UserService userService;

    @AfterEach
    public void afterEach() {
        InternalUsersManager.getInternalUserMap().clear();
    }

    @Test
    public void getNearbyAttractions() {

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        userService.addUser(user);
        VisitedLocation visitedLocation = userService.trackUserLocation(user).join();

        List<NearbyAttraction> attractions = attractionService.getNearByAttractions(user);

        assertEquals(5, attractions.size());
    }
}
