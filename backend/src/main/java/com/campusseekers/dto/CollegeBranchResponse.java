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
public class CollegeBranchResponse {
    private UUID id;
    private UUID collegeId;
    private String collegeName;
    private UUID branchId;
    private String branchName;
    private String branchCode;
    private BigDecimal feesPerYear;
    private Integer intakeCapacity;
    private Integer durationYears;
    private Instant createdAt;
    private Instant updatedAt;
}
