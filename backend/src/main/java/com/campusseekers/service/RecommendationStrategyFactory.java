package com.campusseekers.service;

import com.campusseekers.entity.ExamName;
import com.campusseekers.service.impl.HistoricalCutoffRecommendationStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecommendationStrategyFactory {

    private final HistoricalCutoffRecommendationStrategy historicalCutoffRecommendationStrategy;

    public RecommendationStrategy getStrategy(ExamName exam, String engineVersion, String algorithmVersion) {
        // Return default historical strategy (extensible for JEE, NEET, etc.)
        return historicalCutoffRecommendationStrategy;
    }
}
