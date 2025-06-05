package com.openclassrooms.tourguide.service.model;

import com.openclassrooms.tourguide.manager.InternalUsersManager;
import com.openclassrooms.tourguide.model.user.User;
import com.openclassrooms.tourguide.model.user.UserReward;
import com.openclassrooms.tourguide.service.libs.GpsUtilService;
import com.openclassrooms.tourguide.service.libs.TripPricerService;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tripPricer.Provider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.openclassrooms.tourguide.manager.AppManager.EXECUTOR_SERVICE;

@Service
public class UserService {
    private final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final GpsUtilService gpsUtilService;
    private final TripPricerService tripPricerService;
    private final UserRewardService userRewardService;

    public UserService(GpsUtilService gpsUtilService, TripPricerService tripPricerService, UserRewardService userRewardService) {
        this.gpsUtilService = gpsUtilService;
        this.tripPricerService = tripPricerService;
        this.userRewardService = userRewardService;
    }

    public VisitedLocation getUserLocation(User user) {
        return (!user.getVisitedLocations().isEmpty())
                ? user.getLastVisitedLocation()
                : trackUserLocation(user).join();
    }

    public CompletableFuture<VisitedLocation> trackUserLocation(User user) {
        return CompletableFuture
                .supplyAsync(() -> {
                    VisitedLocation visitedLocation = gpsUtilService.getUserLocation(user.getUserId());
                    user.addToVisitedLocations(visitedLocation);
                    return visitedLocation;
                }, EXECUTOR_SERVICE)
                .thenCompose(visitedLocation ->
                        userRewardService.calculateRewards(user)
                                .thenApply(v -> visitedLocation)
                );
    }

    public List<Provider> getTripDeals(User user) {
        int cumulativeRewardPoints = user.getUserRewards().stream().mapToInt(UserReward::getRewardPoints).sum();
        List<Provider> providers = tripPricerService.getPrice(InternalUsersManager.getTripPricerServiceApiKey(), user.getUserId(),
                user.getUserPreferences().getNumberOfAdults(), user.getUserPreferences().getNumberOfChildren(),
                user.getUserPreferences().getTripDuration(), cumulativeRewardPoints);
        user.setTripDeals(providers);
        return providers;
    }

    public void addUser(User user) {
        if (!InternalUsersManager.getInternalUsersMap().containsKey(user.getUserName())) {
            InternalUsersManager.getInternalUsersMap().put(user.getUserName(), user);
        } else {
            logger.debug("User {} already exists", user.getUserName());
        }
    }

    public User getUser(String userName) {
        return InternalUsersManager.getInternalUsersMap().get(userName);
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(InternalUsersManager.getInternalUsersMap().values());
    }

    public List<VisitedLocation> getVisitedLocations(User user) {
        return user.getVisitedLocations();
    }
}
