package com.campusseekers.service;

import com.campusseekers.model.RecommendationCandidate;
import com.campusseekers.model.RecommendationContext;

import java.util.List;

public interface RecommendationStrategy {
    List<RecommendationCandidate> recommend(RecommendationContext context);
}
