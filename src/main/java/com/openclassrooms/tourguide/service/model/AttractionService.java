package com.openclassrooms.tourguide.service.model;

import com.openclassrooms.tourguide.model.NearbyAttraction;
import com.openclassrooms.tourguide.model.user.User;
import com.openclassrooms.tourguide.service.libs.GpsUtilService;
import com.openclassrooms.tourguide.service.libs.RewardCentralService;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
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

    public List<NearbyAttraction> getNearbyAttractions(User user) {
        Location userLocation = gpsUtilService.getUserLocation(user.getUserId()).location;
        Map<Attraction, Double> distances = new HashMap<>();

        for (Attraction attraction : gpsUtilService.getAttractions()) {
            double distance = locationUtil.getDistance(userLocation, attraction);
            distances.put(attraction, distance);
        }

        Map<Attraction, Double> sortedMap = distances.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(5)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));

        List<NearbyAttraction> nearbyAttractions = new ArrayList<>();
        Set<Attraction> sortedAttractions = sortedMap.keySet();
        for (Attraction attraction : sortedAttractions) {
            NearbyAttraction nearbyAttraction = new NearbyAttraction(
                    attraction.attractionName,
                    attraction.latitude,
                    attraction.longitude,
                    userLocation.latitude,
                    userLocation.longitude,
                    sortedMap.get(attraction),
                    rewardCentralService.getAttractionRewardPoints(
                            attraction.attractionId, user.getUserId())
            );
            nearbyAttractions.add(nearbyAttraction);
        }
        return nearbyAttractions;
    }
}
