package com.campusseekers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlacementResponse {
    private UUID id;
    private UUID collegeId;
    private String collegeName;
    private Integer year;
    private BigDecimal averagePackage;
    private BigDecimal highestPackage;
    private BigDecimal placementRatio;
    private String topRecruiters;
    private Instant createdAt;
    private Instant updatedAt;
}
