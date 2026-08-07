package com.campusseekers.dto;

import java.util.List;

public record DashboardResponse(
        DashboardStatisticsResponse statistics,
        List<RecommendationItemResponse> recentRecommendations,
        List<WishlistResponse> recentWishlist,
        List<ShortlistResponse> recentShortlists,
        List<AdmissionTrackerHistoryResponse> recentAdmissionActivity
) {}
