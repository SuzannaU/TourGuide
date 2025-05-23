package com.openclassrooms.tourguide.service.model;

import com.openclassrooms.tourguide.manager.InternalUsersManager;
import com.openclassrooms.tourguide.model.user.User;
import com.openclassrooms.tourguide.model.user.UserReward;
import com.openclassrooms.tourguide.service.libs.GpsUtilService;
import com.openclassrooms.tourguide.service.libs.RewardCentralService;
import com.openclassrooms.tourguide.service.libs.TripPricerService;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tripPricer.Provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class UserService {
    private final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final GpsUtilService gpsUtilService;
    private final RewardCentralService rewardCentralService;
    private final TripPricerService  tripPricerService;
    private final LocationUtil locationUtil;
    private final UserRewardService userRewardService;

    public UserService(GpsUtilService gpsUtilService, RewardCentralService rewardCentralService, TripPricerService tripPricerService, LocationUtil locationUtil, UserRewardService userRewardService) {
        this.gpsUtilService = gpsUtilService;
        this.rewardCentralService = rewardCentralService;
        this.tripPricerService = tripPricerService;
        this.locationUtil = locationUtil;
        this.userRewardService = userRewardService;
    }

    public User getUser(String userName) {
        return InternalUsersManager.getInternalUserMap().get(userName);
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(InternalUsersManager.getInternalUserMap().values());
    }

    public void addUser(User user) {
        if (!InternalUsersManager.getInternalUserMap().containsKey(user.getUserName())) {
            InternalUsersManager.getInternalUserMap().put(user.getUserName(), user);
        }
    }

    public VisitedLocation getUserLocation(User user) {
        return (user.getVisitedLocations().size() > 0)
                ? user.getLastVisitedLocation()
                : trackUserLocation(user);
    }

    public VisitedLocation trackUserLocation(User user) {
        VisitedLocation visitedLocation = gpsUtilService.getUserLocation(user.getUserId());
        user.addToVisitedLocations(visitedLocation);
        userRewardService.calculateRewards(user);
        return visitedLocation;
    }

    public CompletableFuture<VisitedLocation> trackUserLocation2(User user) {
        CompletableFuture<VisitedLocation> future = CompletableFuture.supplyAsync(() -> {
                    VisitedLocation visitedLocation = gpsUtilService.getUserLocation(user.getUserId());
                    user.addToVisitedLocations(visitedLocation);
                    return visitedLocation;
                })
                .thenApplyAsync(visitedLocation -> {
                    userRewardService.calculateRewards(user);
                    return visitedLocation;
                });
        return future;
    }

    public List<Provider> getTripDeals(User user) {
        int cumulativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
        List<Provider> providers = tripPricerService.getPrice(InternalUsersManager.getTripPricerServiceApiKey(), user.getUserId(),
                user.getUserPreferences().getNumberOfAdults(), user.getUserPreferences().getNumberOfChildren(),
                user.getUserPreferences().getTripDuration(), cumulativeRewardPoints);
        user.setTripDeals(providers);
        return providers;
    }

    public List<VisitedLocation> getVisitedLocations(User user) {
        return user.getVisitedLocations();
    }
}
