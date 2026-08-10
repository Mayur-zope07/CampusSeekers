package com.campusseekers.dto;

import com.campusseekers.entity.Category;
import com.campusseekers.entity.ExamName;
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
public class CutoffResponse {
    private UUID id;
    private UUID collegeBranchId;
    private String collegeName;
    private String branchName;
    private ExamName examName;
    private Integer year;
    private Integer round;
    private Category category;
    private String rawSeatType;
    private String stage;
    private Integer closingRank;
    private BigDecimal closingPercentile;
    private Instant createdAt;
    private Instant updatedAt;
}
