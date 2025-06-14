package com.openclassrooms.tourguide.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import gpsUtil.GpsUtil;
import rewardCentral.RewardCentral;
import tripPricer.TripPricer;

/**
 * This configuration class provides Beans of external libraries needed for the project.
 */
@Configuration
public class TourGuideModule {

    @Bean
    public GpsUtil getGpsUtil() {
        return new GpsUtil();
    }

    @Bean
    public RewardCentral getRewardCentral() {
        return new RewardCentral();
    }

    @Bean
    public TripPricer getTripPricer() {
        return new TripPricer();
    }
}
