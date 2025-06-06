package com.openclassrooms.tourguide.service.model;

import com.openclassrooms.tourguide.model.NearbyAttraction;
import com.openclassrooms.tourguide.model.user.User;
import com.openclassrooms.tourguide.service.libs.GpsUtilService;
import com.openclassrooms.tourguide.service.libs.RewardCentralService;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Provides methods related to NearbyAttraction model object.
 *
 * @see NearbyAttraction
 */
@Service
public class NearbyAttractionService {

    private final GpsUtilService gpsUtilService;
    private final RewardCentralService rewardCentralService;
    private final LocationUtil locationUtil;

    public NearbyAttractionService(GpsUtilService gpsUtilService,
                                   RewardCentralService rewardCentralService,
                                   LocationUtil locationUtil) {
        this.gpsUtilService = gpsUtilService;
        this.rewardCentralService = rewardCentralService;
        this.locationUtil = locationUtil;
    }

    /**
     * Used to fetch the 5 closest attractions regarding the current location of the user, no matter how far they are
     * @param user
     * @return a list of 5 NearbyAttraction
     */
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
