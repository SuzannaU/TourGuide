package com.openclassrooms.tourguide.service.model;

import com.openclassrooms.tourguide.manager.InternalUsersManager;
import com.openclassrooms.tourguide.model.NearbyAttraction;
import com.openclassrooms.tourguide.service.libs.GpsUtilService;
import com.openclassrooms.tourguide.service.libs.RewardCentralService;
import com.openclassrooms.tourguide.service.libs.TripPricerService;
import com.openclassrooms.tourguide.model.user.User;
import com.openclassrooms.tourguide.model.user.UserReward;

import java.util.*;
import java.util.stream.Collectors;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tripPricer.Provider;
import tripPricer.TripPricer;

@Service
public class TourGuideService {
    private final Logger logger = LoggerFactory.getLogger(TourGuideService.class);
    private final GpsUtilService gpsUtilService;
    private final RewardCentralService rewardCentralService;
    private final RewardsService rewardsService;
    private final TripPricer tripPricer = new  TripPricer();
    private final TripPricerService tripPricerService = new TripPricerService(tripPricer);

    public TourGuideService(GpsUtilService gpsUtilService, RewardCentralService rewardCentralService, RewardsService rewardsService) {
        this.gpsUtilService = gpsUtilService;
        this.rewardCentralService = rewardCentralService;
        this.rewardsService = rewardsService;
    }

    public List<UserReward> getUserRewards(User user) {
        return user.getUserRewards();
    }

    public VisitedLocation getUserLocation(User user) {
        VisitedLocation visitedLocation = (user.getVisitedLocations().size() > 0) ? user.getLastVisitedLocation()
                :    trackUserLocation(user);
        return visitedLocation;
    }

    public User getUser(String userName) {
        return InternalUsersManager.getInternalUserMap().get(userName);
    }

    public List<User> getAllUsers() {
        return InternalUsersManager.getInternalUserMap().values().stream().collect(Collectors.toList());
    }

    public void addUser(User user) {
        if (!InternalUsersManager.getInternalUserMap().containsKey(user.getUserName())) {
            InternalUsersManager.getInternalUserMap().put(user.getUserName(), user);
        }
    }

    public List<Provider> getTripDeals(User user) {
        int cumulativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
        List<Provider> providers = tripPricerService.getPrice(InternalUsersManager.getTripPricerServiceApiKey(), user.getUserId(),
                user.getUserPreferences().getNumberOfAdults(), user.getUserPreferences().getNumberOfChildren(),
                user.getUserPreferences().getTripDuration(), cumulativeRewardPoints);
        user.setTripDeals(providers);
        return providers;
    }

    public VisitedLocation trackUserLocation(User user) {
        VisitedLocation visitedLocation = gpsUtilService.getUserLocation(user.getUserId());
        user.addToVisitedLocations(visitedLocation);
        rewardsService.calculateRewards(user);
        return visitedLocation;
    }

    public List<NearbyAttraction> getNearByAttractions(String userName) {
        VisitedLocation visitedLocation = getUserLocation(getUser(userName));
        Map<Attraction, Double> distances = new HashMap<>();

        for (Attraction attraction : gpsUtilService.getAttractions()) {
            double distance = rewardsService.getDistance(visitedLocation.location, attraction);
            distances.put(attraction, distance);
        }

        Map<Attraction, Double> sortedMap = distances.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        List<NearbyAttraction> nearbyAttractions = new ArrayList<>();
        Set<Attraction> attractions = sortedMap.keySet();
        for (Attraction attraction : attractions) {
            NearbyAttraction nearbyAttraction = new NearbyAttraction(
                    attraction.attractionName,
                    attraction.latitude,
                    attraction.longitude,
                    visitedLocation.location.latitude,
                    visitedLocation.location.longitude,
                    sortedMap.get(attraction),
                    rewardCentralService.getAttractionRewardPoints(attraction.attractionId, getUser(userName).getUserId())
            );
            nearbyAttractions.add(nearbyAttraction);
        }
        return nearbyAttractions;
    }
}
