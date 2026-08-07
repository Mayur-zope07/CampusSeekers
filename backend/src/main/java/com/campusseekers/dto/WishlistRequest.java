package com.campusseekers.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record WishlistRequest(
        @NotNull(message = "College ID is required")
        UUID collegeId,

        String notes
) {}
