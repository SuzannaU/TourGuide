package com.openclassrooms.tourguide;

import com.openclassrooms.tourguide.manager.InternalUsersManager;
import com.openclassrooms.tourguide.model.user.User;
import com.openclassrooms.tourguide.service.model.UserService;
import com.openclassrooms.tourguide.tracker.Tracker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class TrackerTest {

    @Autowired
    private Tracker tracker;
    @Autowired
    private UserService userService;
    private Thread trackerThread;

    @BeforeEach
    public void beforeEach(){
        InternalUsersManager.getInternalUsersMap().clear();
        trackerThread = new Thread(tracker);
    }

    @AfterEach
    public void afterEach(){
        trackerThread.interrupt();
    }

    @Test
    public void run_addsLocationToUser(){
        InternalUsersManager.initializeInternalUsers(1);
        List<User> users;

        // starts tracker and waits for sleeping stage to recover users
        trackerThread.start();
        while(true) {
            if(trackerThread.getState() == Thread.State.TIMED_WAITING) {
                users = userService.getAllUsers();
                break;
            }
        }

        User user = users.get(0);
        assertEquals(4, user.getVisitedLocations().size());
    }
}
