package com.openclassrooms.tourguide.controller;

import java.util.List;

import com.openclassrooms.tourguide.model.NearbyAttraction;
import com.openclassrooms.tourguide.model.user.User;
import com.openclassrooms.tourguide.service.model.AttractionService;
import com.openclassrooms.tourguide.service.model.UserRewardService;
import com.openclassrooms.tourguide.service.model.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gpsUtil.location.VisitedLocation;

import com.openclassrooms.tourguide.model.user.UserReward;

import tripPricer.Provider;

@RestController
public class TourGuideController {

    private final AttractionService attractionService;
    private final UserService userService;
    private final UserRewardService userRewardService;

    public TourGuideController(AttractionService attractionService, UserService userService, UserRewardService userRewardService) {
        this.attractionService = attractionService;
        this.userService = userService;
        this.userRewardService = userRewardService;
    }

    @GetMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }

    @GetMapping("/getLocation")
    public VisitedLocation getLocation(@RequestParam String userName) {
        User user = userService.getUser(userName);
        return userService.getUserLocation(user);
    }

    @GetMapping("/getVisitedLocations")
    public List<VisitedLocation> getVisitedLocations(@RequestParam String userName) {
        User user = userService.getUser(userName);
        return userService.getVisitedLocations(user);
    }

    @GetMapping("/getNearbyAttractions")
    public List<NearbyAttraction> getNearbyAttractions(@RequestParam String userName) {
        User user = userService.getUser(userName);
        return attractionService.getNearbyAttractions(user);
    }

    @GetMapping("/getRewards")
    public List<UserReward> getRewards(@RequestParam String userName) {
        User user = userService.getUser(userName);
        return userRewardService.getUserRewards(user);
    }

    @GetMapping("/getTripDeals")
    public List<Provider> getTripDeals(@RequestParam String userName) {
        User user = userService.getUser(userName);
        return userService.getTripDeals(user);
    }
}