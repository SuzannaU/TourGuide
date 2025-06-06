package com.openclassrooms.tourguide.controller;

import java.util.List;

import com.openclassrooms.tourguide.model.NearbyAttraction;
import com.openclassrooms.tourguide.model.user.User;
import com.openclassrooms.tourguide.service.model.NearbyAttractionService;
import com.openclassrooms.tourguide.service.model.UserRewardService;
import com.openclassrooms.tourguide.service.model.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gpsUtil.location.VisitedLocation;

import com.openclassrooms.tourguide.model.user.UserReward;

import tripPricer.Provider;
/**
 * Handles http requests, returns JSON objects
 */
@RestController
public class TourGuideController {

    private final NearbyAttractionService nearbyAttractionService;
    private final UserService userService;
    private final UserRewardService userRewardService;

    public TourGuideController(NearbyAttractionService nearbyAttractionService, UserService userService, UserRewardService userRewardService) {
        this.nearbyAttractionService = nearbyAttractionService;
        this.userService = userService;
        this.userRewardService = userRewardService;
    }

    @GetMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }

    /**
     * Retrieves user from userName and fetches location as VisitedLocation
     *
     * @param userName
     * @return the visitedLocation
     */
    @GetMapping("/getLocation")
    public VisitedLocation getLocation(@RequestParam String userName) {
        User user = userService.getUser(userName);
        return userService.getUserLocation(user);
    }

    /**
     * Retrieves user from userName and fetches all its previous locations.
     *
     * @param userName
     * @return a list of the user's VisitedLocations
     */
    @GetMapping("/getVisitedLocations")
    public List<VisitedLocation> getVisitedLocations(@RequestParam String userName) {
        User user = userService.getUser(userName);
        return userService.getVisitedLocations(user);
    }

    /**
     * Retrieves user from userName and fetches 5 nearby attractions
     * <p>
     * They are the 5 closest attractions to the user location, no matter how far they are.
     * The attractions are ranked by distance from the user.
     *
     * @param userName
     * @return a list of 5 nearby attractions
     */
    @GetMapping("/getNearbyAttractions")
    public List<NearbyAttraction> getNearbyAttractions(@RequestParam String userName) {
        User user = userService.getUser(userName);
        return nearbyAttractionService.getNearbyAttractions(user);
    }

    /**
     * Retrieves user from userName and fetches user rewards
     *
     * @param userName
     * @return a list of userRewards
     */
    @GetMapping("/getRewards")
    public List<UserReward> getRewards(@RequestParam String userName) {
        User user = userService.getUser(userName);
        return userRewardService.getUserRewards(user);
    }

    /**
     *Retrieves user from userName and fetches corresponding trip deals
     *
     * @param userName
     * @return a list of 5 providers
     */
    @GetMapping("/getTripDeals")
    public List<Provider> getTripDeals(@RequestParam String userName) {
        User user = userService.getUser(userName);
        return userService.getTripDeals(user);
    }
}