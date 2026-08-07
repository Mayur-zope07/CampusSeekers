package com.campusseekers.service;

import com.campusseekers.config.RecommendationProperties;
import com.campusseekers.entity.CollegeBranch;
import com.campusseekers.entity.RecommendationCategory;
import com.campusseekers.entity.RecommendationReasonCode;
import com.campusseekers.model.RecommendationCandidate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class RecommendationRuleEvaluator {

    public RecommendationCandidate evaluate(
            CollegeBranch collegeBranch,
            BigDecimal studentPercentile,
            BigDecimal closingPercentile,
            RecommendationProperties properties) {

        BigDecimal difference = studentPercentile.subtract(closingPercentile).setScale(2, RoundingMode.HALF_UP);
        double diffDouble = difference.doubleValue();

        RecommendationCategory category;
        RecommendationReasonCode reasonCode;
        String reason;

        if (diffDouble >= properties.getSafeThreshold().doubleValue()) {
            category = RecommendationCategory.SAFE;
            reasonCode = RecommendationReasonCode.PERCENTILE_ABOVE_CUTOFF;
        } else if (diffDouble >= properties.getTargetThreshold().doubleValue()) {
            category = RecommendationCategory.TARGET;
            reasonCode = RecommendationReasonCode.PERCENTILE_NEAR_CUTOFF;
        } else if (diffDouble >= properties.getDreamThreshold().doubleValue()) {
            category = RecommendationCategory.DREAM;
            reasonCode = diffDouble >= 0 ? RecommendationReasonCode.PERCENTILE_NEAR_CUTOFF : RecommendationReasonCode.PERCENTILE_BELOW_CUTOFF;
        } else {
            return null; // Ineligible
        }

        BigDecimal absDiff = difference.abs().setScale(2, RoundingMode.HALF_UP);
        if (difference.compareTo(BigDecimal.ZERO) >= 0) {
            reason = String.format("Your percentile is %s higher than the previous year's cutoff.", absDiff);
        } else {
            reason = String.format("Your percentile is %s lower than the previous year's cutoff.", absDiff);
        }

        return new RecommendationCandidate(
                collegeBranch,
                studentPercentile,
                closingPercentile,
                difference,
                category,
                reasonCode,
                reason
        );
    }
}
