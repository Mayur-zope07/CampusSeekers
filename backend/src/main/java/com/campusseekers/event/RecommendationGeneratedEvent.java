package com.campusseekers.event;

import com.campusseekers.dto.RecommendationRequest;

import java.time.Instant;
import java.util.UUID;

public record RecommendationGeneratedEvent(
        UUID recommendationId,
        UUID studentProfileId,
        RecommendationRequest request,
        Instant timestamp
) {}
