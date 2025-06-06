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

/**
 * Handles initialization of internal users needed for the developing phase of the application.
 * Database connection will be used for external users.
 * Internal Users initialization only happens in dev profile, or if called from testing
 *
 * @see AppManager
 */
@Component
public class InternalUsersManager {
    private static final Logger logger = LoggerFactory.getLogger(InternalUsersManager.class);
    private static final Map<String, User> INTERNAL_USERS_MAP = new HashMap<>();

    /**
     * Creates internal users and put them in a Map with their userName as Key.
     *
     * @param userNumber the number of users to be created
     */
    public static void initializeInternalUsers(int userNumber) {
        logger.info("Initializing users");
        IntStream.range(0, userNumber).forEach(i -> {
            String userName = "internalUser" + i;
            String phone = "000";
            String email = userName + "@tourGuide.com";
            User user = new User(UUID.randomUUID(), userName, phone, email);
            generateUserLocationHistory(user);

            INTERNAL_USERS_MAP.put(userName, user);
        });
        logger.debug("Created {} internal test users.", userNumber);
    }

    private static void generateUserLocationHistory(User user) {
        IntStream.range(0, 3).forEach(i ->
            user.addToVisitedLocations(new VisitedLocation(user.getUserId(),
                    new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()))
        );
    }

    private static double generateRandomLongitude() {
        double leftLimit = -180;
        double rightLimit = 180;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    private static double generateRandomLatitude() {
        double leftLimit = -85.05112878;
        double rightLimit = 85.05112878;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    private static Date getRandomTime() {
        LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
        return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
    }

    public static Map<String, User> getInternalUsersMap(){
        return INTERNAL_USERS_MAP;
    }
}
