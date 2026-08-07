package com.campusseekers.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ShortlistRequest(
        @NotNull(message = "College Branch ID is required")
        UUID collegeBranchId,

        @Min(value = 1, message = "Priority must be at least 1")
        Integer priority,

        String notes
) {}
