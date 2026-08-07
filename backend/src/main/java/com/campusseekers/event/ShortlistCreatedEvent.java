package com.campusseekers.event;

import java.time.Instant;
import java.util.UUID;

public record ShortlistCreatedEvent(
        UUID shortlistId,
        UUID studentProfileId,
        UUID collegeBranchId,
        Instant timestamp
) {}
