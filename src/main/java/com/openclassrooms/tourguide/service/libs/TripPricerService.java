package com.openclassrooms.tourguide.service.libs;

import org.springframework.stereotype.Service;
import tripPricer.Provider;
import tripPricer.TripPricer;

import java.util.List;
import java.util.UUID;

/**
 * Intermediary layer that communicates with TripPricer library
 */
@Service
public class TripPricerService {
    private final TripPricer tripPricer;
    private static final String TRIP_PRICER_SERVICE_API_KEY = "test-server-api-key";

    public TripPricerService(TripPricer tripPricer) {
        this.tripPricer = tripPricer;
    }

    public List<Provider> getProviders(UUID attractionId, int adults, int children, int nightsStay, int rewardPoints) {
        return tripPricer.getPrice(TRIP_PRICER_SERVICE_API_KEY, attractionId, adults, children, nightsStay, rewardPoints);
    }
}
