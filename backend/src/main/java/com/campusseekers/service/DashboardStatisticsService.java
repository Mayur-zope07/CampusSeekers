package com.campusseekers.service;

import com.campusseekers.dto.DashboardStatisticsResponse;
import java.util.UUID;

public interface DashboardStatisticsService {
    DashboardStatisticsResponse getStatistics(UUID studentProfileId);
}
