package com.campusseekers.dto;

import com.campusseekers.entity.Category;
import com.campusseekers.entity.CollegeType;
import com.campusseekers.entity.ExamName;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RecommendationResponse(
        UUID id,
        UUID studentProfileId,
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
        Integer evaluatedCount,
        Integer filteredCount,
        Integer returnedCount,
        Integer safeCount,
        Integer targetCount,
        Integer dreamCount,
        String engineVersion,
        String algorithmVersion,
        BigDecimal safeThreshold,
        BigDecimal targetThreshold,
        BigDecimal dreamThreshold,
        Boolean cacheHit,
        Instant createdAt,
        List<RecommendationItemResponse> items
) {}
