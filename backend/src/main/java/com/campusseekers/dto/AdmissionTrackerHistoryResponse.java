package com.campusseekers.dto;

import com.campusseekers.entity.AdmissionStatus;
import java.time.Instant;
import java.util.UUID;

public record AdmissionTrackerHistoryResponse(
        UUID id,
        UUID trackerId,
        AdmissionStatus previousStatus,
        AdmissionStatus newStatus,
        String remarks,
        Instant changedAt
) {}
