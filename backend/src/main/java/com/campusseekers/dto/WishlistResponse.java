package com.campusseekers.dto;

import java.time.Instant;
import java.util.UUID;

public record WishlistResponse(
        UUID id,
        UUID studentProfileId,
        UUID collegeId,
        String collegeCode,
        String collegeName,
        String city,
        String state,
        String naacGrade,
        Instant createdAt,
        Boolean isDeleted
) {}
