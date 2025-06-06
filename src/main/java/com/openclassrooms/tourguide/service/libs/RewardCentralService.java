package com.openclassrooms.tourguide.service.libs;

import org.springframework.stereotype.Service;
import rewardCentral.RewardCentral;

import java.util.UUID;

/**
 * Intermediary layer that communicates with RewardCentral library
 */
@Service
public class RewardCentralService {
    private final RewardCentral rewardCentral;

    public RewardCentralService(RewardCentral rewardCentral) {
        this.rewardCentral = rewardCentral;
    }

    public int getAttractionRewardPoints(UUID attractionId, UUID userId) {
        return rewardCentral.getAttractionRewardPoints(attractionId, userId);
    }
}
