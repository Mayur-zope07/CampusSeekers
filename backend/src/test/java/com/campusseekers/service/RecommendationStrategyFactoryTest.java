package com.campusseekers.service;

import com.campusseekers.entity.ExamName;
import com.campusseekers.service.impl.HistoricalCutoffRecommendationStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class RecommendationStrategyFactoryTest {

    @Mock
    private HistoricalCutoffRecommendationStrategy historicalCutoffRecommendationStrategy;

    @InjectMocks
    private RecommendationStrategyFactory factory;

    @Test
    void getStrategy_ShouldReturnHistoricalCutoffStrategy() {
        RecommendationStrategy strategy = factory.getStrategy(ExamName.MHT_CET, "1.0", "historical-cutoff-v1");
        assertNotNull(strategy);
        assertEquals(historicalCutoffRecommendationStrategy, strategy);
    }
}
