package com.campusseekers.dto;

import com.campusseekers.entity.AdmissionStatus;
import jakarta.validation.constraints.NotNull;

public record AdmissionTrackerRequest(
        @NotNull(message = "Status is required")
        AdmissionStatus status,

        String remarks
) {}
