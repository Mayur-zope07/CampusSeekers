package com.campusseekers.service;

import com.campusseekers.dto.RecommendationHistoryResponse;
import com.campusseekers.dto.RecommendationRequest;
import com.campusseekers.dto.RecommendationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface RecommendationService {
    RecommendationResponse generateRecommendations(RecommendationRequest request);
    Page<RecommendationHistoryResponse> getRecommendationHistory(Pageable pageable);
    RecommendationResponse getRecommendationDetails(UUID id);
}
