package com.campusseekers.service;

import com.campusseekers.dto.ShortlistRequest;
import com.campusseekers.dto.ShortlistResponse;
import com.campusseekers.entity.AdmissionStatus;
import com.campusseekers.entity.RecommendationCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

public interface ShortlistService {
    ShortlistResponse addToShortlist(ShortlistRequest request);
    ShortlistResponse updateShortlist(UUID id, Integer priority, String notes);
    void removeFromShortlist(UUID id);
    Page<ShortlistResponse> searchShortlist(
            String collegeKeyword,
            String branchKeyword,
            String city,
            String state,
            String naac,
            BigDecimal maxFees,
            Integer priority,
            AdmissionStatus status,
            RecommendationCategory recCategory,
            Pageable pageable
    );
    ShortlistResponse importRecommendationToShortlist(UUID recommendationItemId);
}
