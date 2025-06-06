package com.openclassrooms.tourguide.service.model;

import com.openclassrooms.tourguide.manager.InternalUsersManager;
import com.openclassrooms.tourguide.model.user.User;
import com.openclassrooms.tourguide.model.user.UserPreferences;
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

/**
 * Provides methods relating to User model object.
 * @see User
 */
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

    /**
     * Used to determine the user's location. Either fetches the last visited location if available, or tracks its location if not.
     * @param user
     * @return the user's location as VisitedLocation
     */
    public VisitedLocation getUserLocation(User user) {
        return (!user.getVisitedLocations().isEmpty())
                ? user.getLastVisitedLocation()
                : trackUserLocation(user).join();
    }

    /**
     * Used to track the user's current location using GpsUtil locating method.
     * Async operations are using the app's common executor service.
     * @param user
     * @return a CompletableFuture
     */
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

    /**
     * Fetches a user's TripDeals using the TripPricer service. It returns a list of providers according to the UserPreferences
     * @param user
     * @return a list of 5 providers
     */
    public List<Provider> getTripDeals(User user) {
        int cumulativeRewardPoints = user.getUserRewards().stream().mapToInt(UserReward::getRewardPoints).sum();
        UserPreferences userPreferences = user.getUserPreferences();
        List<Provider> providers = tripPricerService.getProviders(user.getUserId(),
                userPreferences.getNumberOfAdults(), userPreferences.getNumberOfChildren(),
                userPreferences.getTripDuration(), cumulativeRewardPoints);
        user.setTripDeals(providers);
        return providers;
    }

    /**
     * Adds a user to the internal users Map if it doesn't already exist.
     * Used solely for testing.
     * @param user
     */
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
