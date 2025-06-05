package com.openclassrooms.tourguide.service;

import com.openclassrooms.tourguide.manager.InternalUsersManager;
import com.openclassrooms.tourguide.model.NearbyAttraction;
import com.openclassrooms.tourguide.model.user.User;
import com.openclassrooms.tourguide.service.model.AttractionService;
import com.openclassrooms.tourguide.service.model.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class AttractionServiceTest {

    @Autowired
    private AttractionService attractionService;

    @Autowired
    private UserService userService;

    @BeforeEach
    public void beforeEach() {
        InternalUsersManager.getInternalUsersMap().clear();
    }

    @Test
    public void getNearbyAttractionsTest() {

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        userService.addUser(user);
        userService.trackUserLocation(user).join();

        List<NearbyAttraction> attractions = attractionService.getNearbyAttractions(user);

        assertEquals(5, attractions.size());
        assertTrue(attractions.get(0).getDistance() < attractions.get(1).getDistance());
        assertTrue(attractions.get(1).getDistance() < attractions.get(2).getDistance());
        assertTrue(attractions.get(2).getDistance() < attractions.get(3).getDistance());
        assertTrue(attractions.get(3).getDistance() < attractions.get(4).getDistance());
    }
}
