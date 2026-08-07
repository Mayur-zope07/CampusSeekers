package com.campusseekers.event;

import java.time.Instant;
import java.util.UUID;

public record WishlistCreatedEvent(
        UUID wishlistId,
        UUID studentProfileId,
        UUID collegeId,
        Instant timestamp
) {}
