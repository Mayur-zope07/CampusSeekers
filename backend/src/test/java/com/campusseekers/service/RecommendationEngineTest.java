package com.campusseekers.service;

import com.campusseekers.config.RecommendationProperties;
import com.campusseekers.dto.RecommendationRequest;
import com.campusseekers.entity.Category;
import com.campusseekers.entity.ExamName;
import com.campusseekers.entity.StudentProfile;
import com.campusseekers.model.RecommendationCandidate;
import com.campusseekers.model.RecommendationContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationEngineTest {

    @Mock
    private RecommendationStrategyFactory strategyFactory;

    @Mock
    private RecommendationStrategy strategy;

    @InjectMocks
    private RecommendationEngine recommendationEngine;

    @Test
    void generateRecommendations_ShouldUseStrategyAndLimitResults() {
        RecommendationProperties props = new RecommendationProperties();
        props.setMaximumResults(1);
        props.setMaximumAllowedResults(10);
        props.setEngineVersion("1.0");
        props.setAlgorithmVersion("historical-cutoff-v1");

        RecommendationRequest request = new RecommendationRequest(
                ExamName.MHT_CET, 2025, new BigDecimal("95.00"), null, Category.OPEN,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null, null
        );

        RecommendationContext context = new RecommendationContext(
                new StudentProfile(), request, props, Collections.emptyList(), Collections.emptyList(), Collections.emptyMap()
        );

        RecommendationCandidate cand1 = new RecommendationCandidate(
                null, new BigDecimal("95.00"), new BigDecimal("92.00"), new BigDecimal("3.00"),
                null, null, "r1"
        );
        RecommendationCandidate cand2 = new RecommendationCandidate(
                null, new BigDecimal("95.00"), new BigDecimal("93.00"), new BigDecimal("2.00"),
                null, null, "r2"
        );

        when(strategyFactory.getStrategy(any(ExamName.class), anyString(), anyString())).thenReturn(strategy);
        when(strategy.recommend(context)).thenReturn(List.of(cand1, cand2));

        List<RecommendationCandidate> results = recommendationEngine.generateRecommendations(context);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(cand1, results.get(0));
    }
}
