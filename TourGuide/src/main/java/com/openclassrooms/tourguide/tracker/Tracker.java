package com.openclassrooms.tourguide.tracker;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.openclassrooms.tourguide.manager.TrackerManager;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openclassrooms.tourguide.service.model.TourGuideService;
import com.openclassrooms.tourguide.model.user.User;
import org.springframework.stereotype.Component;

@Component
public class Tracker extends Thread {
	private final Logger logger = LoggerFactory.getLogger(Tracker.class);
	private static final long trackingPollingInterval = TimeUnit.MINUTES.toSeconds(5);
	private final TourGuideService tourGuideService;

	public Tracker(TourGuideService tourGuideService) {
		this.tourGuideService = tourGuideService;
	}

	@Override
	public void run() {
		StopWatch stopWatch = new StopWatch();
		while (true) {
			if (Thread.currentThread().isInterrupted() || TrackerManager.isTrackerStopped()) {
				logger.debug("Tracker stopping");
				break;
			}

			List<User> users = tourGuideService.getAllUsers();
			logger.debug("Begin Tracker. Tracking " + users.size() + " users.");
			stopWatch.start();
			users.forEach(u -> tourGuideService.trackUserLocation(u));
			stopWatch.stop();
			logger.debug("Tracker Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
			stopWatch.reset();
			try {
				logger.debug("Tracker sleeping");
				TimeUnit.SECONDS.sleep(trackingPollingInterval);
			} catch (InterruptedException e) {
				break;
			}
		}

	}
}
