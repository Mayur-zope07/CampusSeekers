package com.campusseekers.service;

import com.campusseekers.config.RecommendationProperties;
import com.campusseekers.model.RecommendationCandidate;
import com.campusseekers.model.RecommendationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationEngine {

    private final RecommendationStrategyFactory strategyFactory;

    public List<RecommendationCandidate> generateRecommendations(RecommendationContext context) {
        log.info("Generating recommendations in RecommendationEngine");

        RecommendationProperties properties = context.configuration();

        // Resolve execution strategy
        RecommendationStrategy strategy = strategyFactory.getStrategy(
                context.request().exam(),
                properties.getEngineVersion(),
                properties.getAlgorithmVersion()
        );

        // Execute recommendation logic
        List<RecommendationCandidate> candidates = strategy.recommend(context);

        // Apply limit logic
        int limit = properties.getMaximumResults();
        if (limit > properties.getMaximumAllowedResults()) {
            limit = properties.getMaximumAllowedResults();
        }

        List<RecommendationCandidate> limited = candidates.stream()
                .limit(limit)
                .collect(Collectors.toList());

        log.info("Engine evaluated {} cutoffs, returning {} recommendations (limit={})",
                context.cutoffs().size(), limited.size(), limit);

        return limited;
    }
}
