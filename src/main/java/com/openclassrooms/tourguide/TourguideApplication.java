package com.openclassrooms.tourguide;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TourguideApplication {
    private static final Logger logger = LoggerFactory.getLogger(TourguideApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(TourguideApplication.class, args);
        logger.info("TourGuideApplication started ");
    }
}
