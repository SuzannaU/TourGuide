package com.openclassrooms.tourguide.manager;

import com.openclassrooms.tourguide.model.user.User;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.IntStream;

@Component
public class InternalUsersManager {
    private static final Logger logger = LoggerFactory.getLogger(InternalUsersManager.class);
    // Set this default up to 100,000 for testing
    private static int internalUserNumber = 1;
    private static final String tripPricerServiceApiKey = "test-server-api-key";
    private static final Map<String, User> internalUserMap = new HashMap<>();
    private static boolean usersInitializationDone = false;

    public static void setInternalUserNumber(int internalUserNumber) {
        InternalUsersManager.internalUserNumber = internalUserNumber;
    }

    public static int getInternalUserNumber() {
        return internalUserNumber;
    }

    public static boolean getUsersInitializationDone() {
        return usersInitializationDone;
    }
    /**********************************************************************************
     *
     * Methods Below: For Internal Testing
     *
     **********************************************************************************/
    // Database connection will be used for external users, but for testing purposes
    // internal users are provided and stored in memory

    public static Map<String, User> getInternalUserMap(){
        return internalUserMap;
    }

    public static String getTripPricerServiceApiKey() {
        return tripPricerServiceApiKey;
    }

    void initializeInternalUsers() {
        logger.info("Initializing users");
        IntStream.range(0, internalUserNumber).forEach(i -> {
            String userName = "internalUser" + i;
            String phone = "000";
            String email = userName + "@tourGuide.com";
            User user = new User(UUID.randomUUID(), userName, phone, email);
            generateUserLocationHistory(user);

            internalUserMap.put(userName, user);
        });
        usersInitializationDone = true;
        logger.debug("Created {} internal test users.", internalUserNumber);
    }

    private void generateUserLocationHistory(User user) {
        IntStream.range(0, 3).forEach(i -> {
            user.addToVisitedLocations(new VisitedLocation(user.getUserId(),
                    new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
        });
    }

    private double generateRandomLongitude() {
        double leftLimit = -180;
        double rightLimit = 180;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    private double generateRandomLatitude() {
        double leftLimit = -85.05112878;
        double rightLimit = 85.05112878;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    private Date getRandomTime() {
        LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
        return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
    }
}
