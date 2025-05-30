package com.openclassrooms.tourguide.service.model;

import com.openclassrooms.tourguide.model.NearbyAttraction;
import com.openclassrooms.tourguide.model.user.User;
import com.openclassrooms.tourguide.service.libs.GpsUtilService;
import com.openclassrooms.tourguide.service.libs.RewardCentralService;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AttractionService {
    private final Logger logger = LoggerFactory.getLogger(AttractionService.class);

    private final GpsUtilService gpsUtilService;
    private final RewardCentralService rewardCentralService;
    private final LocationUtil locationUtil;

    public AttractionService(GpsUtilService gpsUtilService,
                             RewardCentralService rewardCentralService,
                             LocationUtil locationUtil) {
        this.gpsUtilService = gpsUtilService;
        this.rewardCentralService = rewardCentralService;
        this.locationUtil = locationUtil;
    }

    public List<NearbyAttraction> getNearByAttractions(User user) {
        VisitedLocation visitedLocation = gpsUtilService.getUserLocation(user.getUserId());
        Map<Attraction, Double> distances = new HashMap<>();

        for (Attraction attraction : gpsUtilService.getAttractions()) {
            double distance = locationUtil.getDistance(visitedLocation.location, attraction);
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
                    rewardCentralService.getAttractionRewardPoints(
                            attraction.attractionId, user.getUserId())
            );
            nearbyAttractions.add(nearbyAttraction);
        }
        return nearbyAttractions;
    }
}
