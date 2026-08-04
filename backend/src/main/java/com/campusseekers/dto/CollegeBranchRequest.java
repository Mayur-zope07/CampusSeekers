package com.campusseekers.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollegeBranchRequest {

    @NotNull(message = "College ID is required")
    private UUID collegeId;

    @NotNull(message = "Branch ID is required")
    private UUID branchId;

    @NotNull(message = "Fees per year is required")
    @Positive(message = "Fees per year must be positive")
    private BigDecimal feesPerYear;

    @NotNull(message = "Intake capacity is required")
    @Positive(message = "Intake capacity must be positive")
    private Integer intakeCapacity;

    @NotNull(message = "Duration in years is required")
    @Positive(message = "Duration in years must be positive")
    private Integer durationYears;
}
