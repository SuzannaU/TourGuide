package com.openclassrooms.tourguide.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;

@Service
public class RewardsService {
    private static final Logger logger = LoggerFactory.getLogger(RewardsService.class);
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

    // proximity in miles
    private int defaultProximityBuffer = 10;
    private int proximityBuffer = defaultProximityBuffer;
    private int attractionProximityRange = 200;
    private final GpsUtil gpsUtil;
    private final RewardCentral rewardCentral;

    public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
        this.gpsUtil = gpsUtil;
        this.rewardCentral = rewardCentral;
    }

    public void setProximityBuffer(int proximityBuffer) {
        this.proximityBuffer = proximityBuffer;
    }

    public void setDefaultProximityBuffer() {
        proximityBuffer = defaultProximityBuffer;
    }

    public void calculateRewards(User user) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length > 0) {
            StackTraceElement caller = stackTrace[2];
            logger.info("Method calculateRewards called by : {}.{} line {}", caller.getClassName(), caller.getMethodName(), caller.getLineNumber());
        }
        List<VisitedLocation> userLocations = user.getVisitedLocations();
        List<Attraction> attractions = gpsUtil.getAttractions();
        for (VisitedLocation visitedLocation : userLocations) {
            logger.info("inside 1st for, location: {}", visitedLocation);
            for (Attraction attraction : attractions) {
                logger.info("inside 2nd for, attraction: {}", attraction.attractionName);
                if (user.getUserRewards().stream().noneMatch(r -> r.attraction.attractionName.equals(attraction.attractionName))) {
                    logger.info("inside 1st if");
                    if (nearAttraction(visitedLocation, attraction)) {
                        logger.info("inside 2nd if");
                        user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
                    }
                }
            }
        }
    }

    public void calculateRewardsAsync(User user) {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        List<VisitedLocation> userLocations = user.getVisitedLocations();
        CompletableFuture<List<Attraction>> attractionsFuture = CompletableFuture.supplyAsync(gpsUtil::getAttractions, executorService);

        CompletableFuture<Void> allRewards = attractionsFuture.thenCompose(attractions -> {
            logger.info("inside thenCompose");
            List<CompletableFuture<Void>> rewardTasks = new ArrayList<>();
            for (VisitedLocation location : userLocations) {
                logger.info("inside 1st for, location: {}", location);
                for (Attraction attraction : attractions) {
                    logger.info("inside 2nd for, attraction: {}", attraction.attractionName);
                    if (isNotAUserReward(user, attraction) && nearAttraction(location, attraction)) {
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
        CompletableFuture<List<Attraction>> attractionsFuture = CompletableFuture.supplyAsync(gpsUtil::getAttractions, executorService);
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
                            if (isNotAUserReward(user, attraction) && nearAttraction(location, attraction)) {
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

    public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
        return getDistance(attraction, location) > attractionProximityRange ? false : true;
    }

    private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
        return getDistance(attraction, visitedLocation.location) > proximityBuffer ? false : true;
    }

    private int getRewardPoints(Attraction attraction, User user) {
        logger.info("inside getRewardPoints");
        return rewardCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
    }

    private Map<String, Integer> getRewardPointsMap(List<Attraction> attractions, User user) {
        logger.info("inside getRewardPointsMap");
        Map<String, Integer> rewardPointsMap = new HashMap<>();

        for (Attraction attraction : attractions) {
            int points = rewardCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
            rewardPointsMap.put(attraction.attractionName, points);
        }
        return rewardPointsMap;
    }

    public double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
        return statuteMiles;
    }

}
