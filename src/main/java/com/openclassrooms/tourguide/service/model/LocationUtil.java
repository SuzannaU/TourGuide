package com.openclassrooms.tourguide.service.model;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Utility class that provides methods related to handling distances
 */
@Component
public class LocationUtil {
    private final Logger logger = LoggerFactory.getLogger(LocationUtil.class);
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

    // proximity in miles
    private static final int DEFAULT_PROXIMITY_BUFFER = 10;
    private int proximityBuffer = DEFAULT_PROXIMITY_BUFFER;

    /**
     * Is used to determine weather or not a location is close to an attraction, according to the proximity buffer.
     * @param attraction
     * @param visitedLocation
     * @return true if the distance between attraction and location is under proximity buffer
     */
    public boolean areWithinProximityBuffer(Attraction attraction, VisitedLocation visitedLocation) {
        return (getDistance(attraction, visitedLocation.location) < proximityBuffer);
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

    public void setProximityBuffer(int proximityBuffer) {
        this.proximityBuffer = proximityBuffer;
        logger.info("Proximity Buffer has been set to {}", this.proximityBuffer);
    }

    public int getProximityBuffer() {
        return proximityBuffer;
    }

    public int getDefaultProximityBuffer() {
        return DEFAULT_PROXIMITY_BUFFER;
    }
}
