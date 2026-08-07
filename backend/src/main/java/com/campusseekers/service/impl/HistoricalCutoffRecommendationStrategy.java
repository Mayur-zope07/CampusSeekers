package com.campusseekers.service.impl;

import com.campusseekers.entity.Cutoff;
import com.campusseekers.model.RecommendationCandidate;
import com.campusseekers.model.RecommendationContext;
import com.campusseekers.service.RecommendationComparator;
import com.campusseekers.service.RecommendationFilterService;
import com.campusseekers.service.RecommendationRuleEvaluator;
import com.campusseekers.service.RecommendationStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoricalCutoffRecommendationStrategy implements RecommendationStrategy {

    private final RecommendationFilterService filterService;
    private final RecommendationRuleEvaluator ruleEvaluator;

    @Override
    public List<RecommendationCandidate> recommend(RecommendationContext context) {
        // Filter candidates based on preferences
        List<Cutoff> filteredCutoffs = filterService.filter(context.cutoffs(), context.request());

        // Evaluate remaining options against classification rules
        List<RecommendationCandidate> candidates = filteredCutoffs.stream()
                .map(cutoff -> ruleEvaluator.evaluate(
                        cutoff.getCollegeBranch(),
                        context.request().percentile(),
                        cutoff.getClosingPercentile(),
                        context.configuration()
                ))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Rank results using comparison logic
        candidates.sort(new RecommendationComparator(context.placementsMap()));

        return candidates;
    }
}
