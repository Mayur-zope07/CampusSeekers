package com.campusseekers.event;

import java.time.Instant;
import java.util.UUID;

public record WishlistDeletedEvent(
        UUID wishlistId,
        UUID studentProfileId,
        Instant timestamp
) {}
