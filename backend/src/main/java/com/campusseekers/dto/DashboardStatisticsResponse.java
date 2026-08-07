package com.campusseekers.dto;

import com.campusseekers.entity.AdmissionStatus;
import java.math.BigDecimal;
import java.util.Map;

public record DashboardStatisticsResponse(
        long wishlistCount,
        long shortlistCount,
        long recommendationCount,
        long safeCount,
        long targetCount,
        long dreamCount,
        long applicationsCount,
        BigDecimal averageFees,
        BigDecimal highestPackage,
        BigDecimal lowestCutoff,
        Map<AdmissionStatus, Long> statusBreakdown
) {}
