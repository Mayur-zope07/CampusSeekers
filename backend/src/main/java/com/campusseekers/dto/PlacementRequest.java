package com.campusseekers.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
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
public class PlacementRequest {

    @NotNull(message = "College ID is required")
    private UUID collegeId;

    @NotNull(message = "Year is required")
    @Positive(message = "Year must be positive")
    private Integer year;

    @NotNull(message = "Average package is required")
    @Positive(message = "Average package must be positive")
    private BigDecimal averagePackage;

    @NotNull(message = "Highest package is required")
    @Positive(message = "Highest package must be positive")
    private BigDecimal highestPackage;

    @NotNull(message = "Placement ratio is required")
    @DecimalMin(value = "0.00", message = "Placement ratio cannot be less than 0.00")
    @DecimalMax(value = "100.00", message = "Placement ratio cannot exceed 100.00")
    private BigDecimal placementRatio;

    private String topRecruiters;
}
