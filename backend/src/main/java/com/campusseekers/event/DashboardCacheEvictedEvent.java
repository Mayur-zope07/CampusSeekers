package com.campusseekers.event;

import java.time.Instant;
import java.util.UUID;

public record DashboardCacheEvictedEvent(
        UUID studentProfileId,
        Instant timestamp
) {}
