package com.campusseekers.event;

import java.time.Instant;
import java.util.UUID;

public record RecommendationViewedEvent(
        UUID recommendationId,
        UUID userId,
        Instant timestamp
) {}
