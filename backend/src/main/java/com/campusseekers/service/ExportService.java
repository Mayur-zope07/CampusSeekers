package com.campusseekers.service;

import com.campusseekers.dto.ExportResponse;
import com.campusseekers.dto.ShortlistResponse;
import com.campusseekers.dto.WishlistResponse;
import com.campusseekers.dto.RecommendationItemResponse;
import com.campusseekers.dto.DashboardResponse;

import java.util.List;

public interface ExportService {
    String getFormat();
    ExportResponse exportWishlist(List<WishlistResponse> wishlist);
    ExportResponse exportShortlist(List<ShortlistResponse> shortlist);
    ExportResponse exportRecommendations(List<RecommendationItemResponse> recommendations);
    ExportResponse exportDashboard(DashboardResponse dashboard);
}
