package com.openclassrooms.tourguide;

import com.openclassrooms.tourguide.controller.TourGuideController;
import com.openclassrooms.tourguide.model.NearbyAttraction;
import com.openclassrooms.tourguide.model.user.User;
import com.openclassrooms.tourguide.model.user.UserReward;
import com.openclassrooms.tourguide.service.model.AttractionService;
import com.openclassrooms.tourguide.service.model.UserRewardService;
import com.openclassrooms.tourguide.service.model.UserService;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tripPricer.Provider;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TourGuideController.class)
@ActiveProfiles("test")
public class TourGuideControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AttractionService attractionService;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private UserRewardService userRewardService;

    private User user;
    private VisitedLocation visitedLocation;

    @BeforeEach
    public void BeforeEach() {
        user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        visitedLocation = new VisitedLocation(user.getUserId(), new Location(1.0, 1.0), new Date());
    }

    @Test
    public void indexTest() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(content().string(containsString("Greetings from TourGuide!")));
    }

    @Test
    public void getLocationTest() throws Exception {
        when(userService.getUser(anyString())).thenReturn(user);
        when(userService.getUserLocation(any(User.class))).thenReturn(visitedLocation);

        mockMvc.perform(get("/getLocation")
                        .param("userName", user.getUserName()))
                .andExpect(status().isOk());

        verify(userService).getUser(anyString());
        verify(userService).getUserLocation(any(User.class));
    }

    @Test
    public void getVisitedLocationsTest() throws Exception {
        List<VisitedLocation> visitedLocations = List.of(visitedLocation);
        when(userService.getUser(anyString())).thenReturn(user);
        when(userService.getVisitedLocations(any(User.class))).thenReturn(visitedLocations);

        mockMvc.perform(get("/getVisitedLocations")
                        .param("userName", user.getUserName()))
                .andExpect(status().isOk());

        verify(userService).getUser(anyString());
        verify(userService).getVisitedLocations(any(User.class));
    }

    @Test
    public void getNearbyAttractionsTest() throws Exception {
        NearbyAttraction nearbyAttraction = new NearbyAttraction("name", 1.0, 1.0, 1.0, 1.0, 1.0, 1);
        List<NearbyAttraction> nearbyAttractions = List.of(nearbyAttraction);
        when(userService.getUser(anyString())).thenReturn(user);
        when(attractionService.getNearbyAttractions(any(User.class))).thenReturn(nearbyAttractions);

        mockMvc.perform(get("/getNearbyAttractions")
                        .param("userName", user.getUserName()))
                .andExpect(status().isOk());

        verify(userService).getUser(anyString());
        verify(attractionService).getNearbyAttractions(any(User.class));
    }

    @Test
    public void getRewardsTest() throws Exception {
        Attraction attraction = new Attraction("name", "city", "state", 1.0, 1.0);
        UserReward userReward = new UserReward(visitedLocation, attraction);
        List<UserReward> userRewards = List.of(userReward);
        when(userService.getUser(anyString())).thenReturn(user);
        when(userRewardService.getUserRewards(any(User.class))).thenReturn(userRewards);

        mockMvc.perform(get("/getRewards")
                        .param("userName", user.getUserName()))
                .andExpect(status().isOk());

        verify(userService).getUser(anyString());
        verify(userRewardService).getUserRewards(any(User.class));
    }

    @Test
    public void getTripDealsTest() throws Exception {
        List<Provider> providers = List.of(new Provider(UUID.randomUUID(), "name", 1.0));
        when(userService.getUser(anyString())).thenReturn(user);
        when(userService.getTripDeals(any(User.class))).thenReturn(providers);

        mockMvc.perform(get("/getTripDeals")
                        .param("userName", user.getUserName()))
                .andExpect(status().isOk());

        verify(userService).getUser(anyString());
        verify(userService).getTripDeals(any(User.class));
    }
}
