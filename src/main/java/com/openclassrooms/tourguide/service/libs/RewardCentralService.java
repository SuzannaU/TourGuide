package com.openclassrooms.tourguide.service.libs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import rewardCentral.RewardCentral;

import java.util.UUID;

@Service
public class RewardCentralService {
    private static final Logger logger = LoggerFactory.getLogger(RewardCentralService.class);
    private final RewardCentral rewardCentral;

    public RewardCentralService(RewardCentral rewardCentral) {
        this.rewardCentral = rewardCentral;
    }

    public int getAttractionRewardPoints(UUID attractionId, UUID userId) {
        return rewardCentral.getAttractionRewardPoints(attractionId, userId);
    }
}
