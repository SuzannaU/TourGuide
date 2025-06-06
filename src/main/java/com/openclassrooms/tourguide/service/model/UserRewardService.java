package com.openclassrooms.tourguide.service.model;

import com.openclassrooms.tourguide.model.user.User;
import com.openclassrooms.tourguide.model.user.UserReward;
import com.openclassrooms.tourguide.service.libs.GpsUtilService;
import com.openclassrooms.tourguide.service.libs.RewardCentralService;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

import static com.openclassrooms.tourguide.manager.AppManager.EXECUTOR_SERVICE;

/**
 * Provides methods related to UserReward model object
 * @see UserReward
 */
@Service
public class UserRewardService {

    private final GpsUtilService gpsUtilService;
    private final RewardCentralService rewardCentralService;
    private final LocationUtil locationUtil;

    public UserRewardService(GpsUtilService gpsUtilService, RewardCentralService rewardCentralService, LocationUtil locationUtil) {
        this.gpsUtilService = gpsUtilService;
        this.rewardCentralService = rewardCentralService;
        this.locationUtil = locationUtil;
    }

    /**
     * Retrieves a list of Attractions from GpsUtil and goes through them.
     * If an attraction is close to the user's location, it creates a UserReward related to that attraction and adds it to the user's list of UserRewards.
     * Async operations are using the app's common executor service.
     *
     * @param user
     * @return a CompletableFuture<Void>
     */
    public CompletableFuture<Void> calculateRewards(User user) {
        List<VisitedLocation> userLocations = user.getVisitedLocations();
        CompletableFuture<List<Attraction>> attractionsFuture = CompletableFuture.supplyAsync(gpsUtilService::getAttractions, EXECUTOR_SERVICE);

        return attractionsFuture.thenCompose(attractions -> {
            List<CompletableFuture<Void>> rewardTasks = new ArrayList<>();
            for (Attraction attraction : attractions) {
                if (isNotAUserReward(user, attraction)) {
                    for (VisitedLocation location : userLocations) {
                        if (locationUtil.areWithinProximityBuffer(attraction, location)) {
                            CompletableFuture<Void> rewardTask = CompletableFuture
                                    .supplyAsync(() -> getRewardPoints(attraction, user), EXECUTOR_SERVICE)
                                    .thenAccept(points ->
                                            user.addUserReward(new UserReward(location, attraction, points)));
                            rewardTasks.add(rewardTask);
                        }
                    }
                }
            }
            return CompletableFuture.allOf(rewardTasks.toArray(new CompletableFuture[0]));
        });
    }

    private boolean isNotAUserReward(User user, Attraction attraction) {
        return user.getUserRewards().stream().noneMatch(r -> r.getAttraction().attractionName.equals(attraction.attractionName));
    }

    private int getRewardPoints(Attraction attraction, User user) {
        return rewardCentralService.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
    }

    public List<UserReward> getUserRewards(User user) {
        return user.getUserRewards();
    }
}
