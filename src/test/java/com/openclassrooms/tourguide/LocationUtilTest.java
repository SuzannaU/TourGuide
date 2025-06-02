package com.openclassrooms.tourguide;

import com.openclassrooms.tourguide.service.model.LocationUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class LocationUtilTest {

    @Autowired
    private LocationUtil locationUtil;

    @Test
    public void areWithinProximityBuffer_returnsTrue() {
        double latitude = 1.0;
        double longitude = 1.0;
        Attraction attraction = new Attraction("name", "city", "date", latitude, longitude);
        Location location = new Location(latitude, longitude);
        VisitedLocation visitedLocation = new VisitedLocation(UUID.randomUUID(), location, new Date());

        assertTrue(locationUtil.areWithinProximityBuffer(attraction, visitedLocation));
    }

    @Test
    public void areWithinProximityBuffer_returnsFalse() {
        Attraction attraction = new Attraction("name", "city", "date", 1.0, 1.0);

        Location location = new Location(Double.MAX_VALUE, Double.MAX_VALUE);
        VisitedLocation visitedLocation = new VisitedLocation(UUID.randomUUID(), location, new Date());

        assertFalse(locationUtil.areWithinProximityBuffer(attraction, visitedLocation));
    }
}
