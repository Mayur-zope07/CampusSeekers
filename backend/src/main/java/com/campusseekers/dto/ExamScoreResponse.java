package com.campusseekers.dto;

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
public class ExamScoreResponse {
    private UUID id;
    private ExamName examName;
    private Integer examYear;
    private Integer marks;
    private Integer rank;
    private BigDecimal percentile;
    private Instant createdAt;
    private Instant updatedAt;
}
