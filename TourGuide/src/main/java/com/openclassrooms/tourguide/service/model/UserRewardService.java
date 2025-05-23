package com.openclassrooms.tourguide.service.model;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class UserRewardService {
    private final Logger logger = LoggerFactory.getLogger(UserRewardService.class);

    private final GpsUtilService gpsUtilService;
    private final RewardCentralService rewardCentralService;
    private final TripPricerService tripPricerService;
    private final AttractionService attractionService;
    private final LocationUtil locationUtil;

    public UserRewardService(GpsUtilService gpsUtilService, RewardCentralService rewardCentralService, TripPricerService tripPricerService, AttractionService attractionService, LocationUtil locationUtil) {
        this.gpsUtilService = gpsUtilService;
        this.rewardCentralService = rewardCentralService;
        this.tripPricerService = tripPricerService;
        this.attractionService = attractionService;
        this.locationUtil = locationUtil;
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

    public void calculateRewardsAsync(User user) {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        List<VisitedLocation> userLocations = user.getVisitedLocations();
        CompletableFuture<List<Attraction>> attractionsFuture = CompletableFuture.supplyAsync(gpsUtilService::getAttractions, executorService);

        CompletableFuture<Void> allRewards = attractionsFuture.thenCompose(attractions -> {
            logger.info("inside thenCompose");
            List<CompletableFuture<Void>> rewardTasks = new ArrayList<>();
            for (VisitedLocation location : userLocations) {
                logger.info("inside 1st for, location: {}", location);
                for (Attraction attraction : attractions) {
                    logger.info("inside 2nd for, attraction: {}", attraction.attractionName);
                    if (isNotAUserReward(user, attraction) && locationUtil.areWithinProximityBuffer(attraction, location)) {
                        logger.info("inside if");
                        CompletableFuture<Void> rewardTask = CompletableFuture
                                .supplyAsync(() -> getRewardPoints(attraction, user), executorService)
                                .thenAccept(points -> {
                                    logger.info("inside thenAccept");
                                    user.addUserReward(new UserReward(location, attraction, points));
                                });
                        rewardTasks.add(rewardTask);
                        logger.info("rewardTasks has {} tasks", rewardTasks.size());
                    }
                }
            }
            logger.info("now we return");
            return CompletableFuture.allOf(rewardTasks.toArray(new CompletableFuture[0]));
        });
        try {
            allRewards.get();
            logger.info("inside try");
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void calculateRewardsAsync2(User user) {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        List<VisitedLocation> userLocations = user.getVisitedLocations();
        CompletableFuture<List<Attraction>> attractionsFuture = CompletableFuture.supplyAsync(gpsUtilService::getAttractions, executorService);
        CompletableFuture<Map<String, Integer>> rewardPointsMapFuture = attractionsFuture
                .thenApplyAsync(attractions -> getRewardPointsMap(attractions, user), executorService);
        CompletableFuture<Void> allRewards = attractionsFuture
                .thenCombineAsync(rewardPointsMapFuture, (attractions, rewardsPoints) -> {
                    logger.info("inside thenCombine");
                    List<CompletableFuture<Void>> rewardTasks = new ArrayList<>();
                    for (VisitedLocation location : userLocations) {
                        logger.info("inside 1st for, location: {}", location);
                        for (Attraction attraction : attractions) {
                            logger.info("inside 2nd for, attraction: {}", attraction.attractionName);
                            if (isNotAUserReward(user, attraction) && locationUtil.areWithinProximityBuffer(attraction, location)) {
                                logger.info("inside if");
                                CompletableFuture<Void> rewardTask = CompletableFuture
                                        .supplyAsync(() -> rewardsPoints.get(attraction.attractionName), executorService)
                                        .thenAccept(points -> {
                                            logger.info("inside thenAccept, points = {}", points);
                                            user.addUserReward(new UserReward(location, attraction, points));
                                        });
                                rewardTasks.add(rewardTask);
                                logger.info("rewardTasks has {} tasks", rewardTasks.size());
                            }
                        }
                    }
                    logger.info("now we return");
                    return CompletableFuture.allOf(rewardTasks.toArray(new CompletableFuture[0]));
                }, executorService)
                .thenCompose(future -> future);
        try {
            allRewards.get();
            logger.info("inside try");
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private boolean isNotAUserReward(User user, Attraction attraction) {
        return user.getUserRewards().stream().noneMatch(r -> r.attraction.attractionName.equals(attraction.attractionName));
    }

    private int getRewardPoints(Attraction attraction, User user) {
        return rewardCentralService.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
    }

    private Map<String, Integer> getRewardPointsMap(List<Attraction> attractions, User user) {
        logger.info("inside getRewardPointsMap");
        Map<String, Integer> rewardPointsMap = new HashMap<>();

        for (Attraction attraction : attractions) {
            int points = rewardCentralService.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
            rewardPointsMap.put(attraction.attractionName, points);
        }
        return rewardPointsMap;
    }

    public List<UserReward> getUserRewards(User user) {
        return user.getUserRewards();
    }
}
