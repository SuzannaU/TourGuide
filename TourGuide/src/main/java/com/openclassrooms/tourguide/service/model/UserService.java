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

@Service
public class UserService {
    private final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final GpsUtilService gpsUtilService;
    private final RewardCentralService rewardCentralService;
    private final TripPricerService  tripPricerService;
    private final LocationUtil locationUtil;

    public UserService(GpsUtilService gpsUtilService, RewardCentralService rewardCentralService, TripPricerService tripPricerService, LocationUtil locationUtil) {
        this.gpsUtilService = gpsUtilService;
        this.rewardCentralService = rewardCentralService;
        this.tripPricerService = tripPricerService;
        this.locationUtil = locationUtil;
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

    public VisitedLocation getUserLocation(String userName) {
        User user = getUser(userName);
        return (user.getVisitedLocations().size() > 0)
                ? user.getLastVisitedLocation()
                : trackUserLocation(user);
    }

    public VisitedLocation trackUserLocation(User user) {
        VisitedLocation visitedLocation = gpsUtilService.getUserLocation(user.getUserId());
        user.addToVisitedLocations(visitedLocation);
        calculateRewards(user);
        return visitedLocation;
    }

    public void calculateRewards(User user) {
        List<VisitedLocation> userLocations = user.getVisitedLocations();
        List<Attraction> attractions = gpsUtilService.getAttractions();
        for (VisitedLocation visitedLocation : userLocations) {
            for (Attraction attraction : attractions) {
                if (user.getUserRewards().stream().noneMatch(r -> r.attraction.attractionName.equals(attraction.attractionName))) {
                    if (locationUtil.areWithinProximityBuffer(attraction, visitedLocation)) {
                        user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
                    }
                }
            }
        }
    }

    private int getRewardPoints(Attraction attraction, User user) {
        return rewardCentralService.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
    }

    public List<UserReward> getUserRewards(String userName) {
        User user =  getUser(userName);
        return user.getUserRewards();
    }

    public List<Provider> getTripDeals(String userName) {
        User user =  getUser(userName);
        int cumulativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
        List<Provider> providers = tripPricerService.getPrice(InternalUsersManager.getTripPricerServiceApiKey(), user.getUserId(),
                user.getUserPreferences().getNumberOfAdults(), user.getUserPreferences().getNumberOfChildren(),
                user.getUserPreferences().getTripDuration(), cumulativeRewardPoints);
        user.setTripDeals(providers);
        return providers;
    }

    public List<VisitedLocation> getVisitedLocations(String userName) {
        User user =  getUser(userName);
        return user.getVisitedLocations();
    }
}
