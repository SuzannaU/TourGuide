package com.openclassrooms.tourguide;

import com.openclassrooms.tourguide.service.model.LocationUtil;
import gpsUtil.location.Attraction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class LocationUtilTest {

    @Autowired
    private LocationUtil locationUtil;

    @Test
    public void areWithinProximityRange_returnsTrue() {
        Attraction attraction = new Attraction("name", "city", "date", 1.0,1.0);
        assertTrue(locationUtil.areWithinProximityRange(attraction, attraction));
    }

}
