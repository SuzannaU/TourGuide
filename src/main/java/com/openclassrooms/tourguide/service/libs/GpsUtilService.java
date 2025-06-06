package com.openclassrooms.tourguide.service.libs;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Intermediary layer that communicates with GpsUtil library
 */
@Service
public class GpsUtilService {
    private final GpsUtil gpsUtil;

    public GpsUtilService(GpsUtil gpsUtil) {
        this.gpsUtil = gpsUtil;
    }

    public VisitedLocation getUserLocation(UUID userId) {
        return gpsUtil.getUserLocation(userId);
    }

    public List<Attraction> getAttractions() {
        return gpsUtil.getAttractions();
    }
}
