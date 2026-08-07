package com.campusseekers.dto;

import com.campusseekers.entity.AdmissionStatus;
import java.time.Instant;
import java.util.UUID;

public record AdmissionTrackerResponse(
        UUID id,
        UUID shortlistId,
        AdmissionStatus currentStatus,
        String remarks,
        Instant createdAt,
        Instant updatedAt
) {}
