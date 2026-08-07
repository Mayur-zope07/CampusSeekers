package com.campusseekers.dto;

import com.campusseekers.entity.Category;
import com.campusseekers.entity.ExamName;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RecommendationSummaryResponse(
        UUID id,
        UUID studentProfileId,
        ExamName examName,
        Integer admissionYear,
        BigDecimal percentile,
        Category category,
        Integer evaluatedCount,
        Integer returnedCount,
        Instant createdAt
) {}
