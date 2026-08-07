package com.campusseekers.model;

import com.campusseekers.entity.CollegeBranch;
import com.campusseekers.entity.RecommendationCategory;
import com.campusseekers.entity.RecommendationReasonCode;

import java.math.BigDecimal;

public record RecommendationCandidate(
        CollegeBranch collegeBranch,
        BigDecimal studentPercentile,
        BigDecimal closingPercentile,
        BigDecimal percentileDifference,
        RecommendationCategory category,
        RecommendationReasonCode reasonCode,
        String humanReadableReason
) {}
