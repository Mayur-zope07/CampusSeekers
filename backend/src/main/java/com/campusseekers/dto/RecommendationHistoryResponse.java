package com.campusseekers.dto;

import com.campusseekers.entity.Category;
import com.campusseekers.entity.CollegeType;
import com.campusseekers.entity.ExamName;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RecommendationHistoryResponse(
        UUID id,
        ExamName examName,
        Integer admissionYear,
        BigDecimal percentile,
        Integer rank,
        Category category,
        List<String> preferredBranches,
        List<String> preferredCities,
        List<CollegeType> preferredCollegeTypes,
        String minimumNaac,
        BigDecimal maximumFees,
        Integer executionTimeMs,
        Integer returnedCount,
        Boolean cacheHit,
        Instant createdAt
) {}
