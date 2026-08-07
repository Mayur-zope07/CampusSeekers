package com.campusseekers.dto;

import com.campusseekers.entity.CollegeType;
import com.campusseekers.entity.RecommendationCategory;
import com.campusseekers.entity.RecommendationReasonCode;

import java.math.BigDecimal;
import java.util.UUID;

public record RecommendationItemResponse(
        UUID collegeId,
        String collegeCode,
        String collegeName,
        UUID branchId,
        String branchCode,
        String branchName,
        String city,
        String state,
        CollegeType collegeType,
        String naacGrade,
        Boolean nbaAccredited,
        Integer durationYears,
        Integer intakeCapacity,
        BigDecimal feesPerYear,
        BigDecimal closingPercentile,
        BigDecimal studentPercentile,
        BigDecimal percentileDifference,
        RecommendationCategory recommendationCategory,
        RecommendationReasonCode recommendationReasonCode,
        String humanReadableReason,
        BigDecimal placementRatio,
        BigDecimal averagePackage,
        BigDecimal highestPackage
) {}
