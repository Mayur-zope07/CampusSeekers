package com.campusseekers.service;

import com.campusseekers.config.RecommendationProperties;
import com.campusseekers.entity.CollegeBranch;
import com.campusseekers.entity.RecommendationCategory;
import com.campusseekers.entity.RecommendationReasonCode;
import com.campusseekers.model.RecommendationCandidate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class RecommendationRuleEvaluatorTest {

    private RecommendationRuleEvaluator evaluator;
    private RecommendationProperties properties;
    private CollegeBranch collegeBranch;

    @BeforeEach
    void setUp() {
        evaluator = new RecommendationRuleEvaluator();
        properties = new RecommendationProperties();
        properties.setSafeThreshold(new BigDecimal("3.0"));
        properties.setTargetThreshold(new BigDecimal("1.5"));
        properties.setDreamThreshold(new BigDecimal("0.0"));
        collegeBranch = new CollegeBranch();
    }

    @Test
    void evaluate_ShouldClassifyAsSafe_WhenDifferenceIsAtOrAboveSafeThreshold() {
        RecommendationCandidate candidate = evaluator.evaluate(
                collegeBranch, new BigDecimal("95.00"), new BigDecimal("92.00"), properties
        );

        assertNotNull(candidate);
        assertEquals(RecommendationCategory.SAFE, candidate.category());
        assertEquals(RecommendationReasonCode.PERCENTILE_ABOVE_CUTOFF, candidate.reasonCode());
        assertEquals(new BigDecimal("3.00"), candidate.percentileDifference());
        assertTrue(candidate.humanReadableReason().contains("higher"));
    }

    @Test
    void evaluate_ShouldClassifyAsTarget_WhenDifferenceIsAtOrAboveTargetThreshold() {
        RecommendationCandidate candidate = evaluator.evaluate(
                collegeBranch, new BigDecimal("95.00"), new BigDecimal("93.50"), properties
        );

        assertNotNull(candidate);
        assertEquals(RecommendationCategory.TARGET, candidate.category());
        assertEquals(RecommendationReasonCode.PERCENTILE_NEAR_CUTOFF, candidate.reasonCode());
        assertEquals(new BigDecimal("1.50"), candidate.percentileDifference());
    }

    @Test
    void evaluate_ShouldClassifyAsDream_WhenDifferenceIsAtOrAboveDreamThreshold() {
        RecommendationCandidate candidate = evaluator.evaluate(
                collegeBranch, new BigDecimal("95.00"), new BigDecimal("94.50"), properties
        );

        assertNotNull(candidate);
        assertEquals(RecommendationCategory.DREAM, candidate.category());
        assertEquals(RecommendationReasonCode.PERCENTILE_NEAR_CUTOFF, candidate.reasonCode());
        assertEquals(new BigDecimal("0.50"), candidate.percentileDifference());
    }

    @Test
    void evaluate_ShouldClassifyAsDream_WhenDifferenceIsNegativeButAtOrAboveDreamThreshold() {
        properties.setDreamThreshold(new BigDecimal("-2.0"));
        RecommendationCandidate candidate = evaluator.evaluate(
                collegeBranch, new BigDecimal("95.00"), new BigDecimal("96.00"), properties
        );

        assertNotNull(candidate);
        assertEquals(RecommendationCategory.DREAM, candidate.category());
        assertEquals(RecommendationReasonCode.PERCENTILE_BELOW_CUTOFF, candidate.reasonCode());
        assertEquals(new BigDecimal("-1.00"), candidate.percentileDifference());
        assertTrue(candidate.humanReadableReason().contains("lower"));
    }

    @Test
    void evaluate_ShouldReturnNull_WhenDifferenceIsBelowDreamThreshold() {
        RecommendationCandidate candidate = evaluator.evaluate(
                collegeBranch, new BigDecimal("95.00"), new BigDecimal("95.10"), properties
        );

        assertNull(candidate);
    }
}
