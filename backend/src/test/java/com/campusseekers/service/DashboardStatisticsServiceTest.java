package com.campusseekers.service;

import com.campusseekers.dto.DashboardStatisticsResponse;
import com.campusseekers.entity.*;
import com.campusseekers.repository.*;
import com.campusseekers.service.impl.DashboardStatisticsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardStatisticsServiceTest {

    @Mock private WishlistRepository wishlistRepository;
    @Mock private ShortlistRepository shortlistRepository;
    @Mock private RecommendationRepository recommendationRepository;
    @Mock private PlacementRepository placementRepository;
    @Mock private CutoffRepository cutoffRepository;

    @InjectMocks
    private DashboardStatisticsServiceImpl dashboardStatisticsService;

    private UUID studentProfileId;

    @BeforeEach
    void setUp() {
        studentProfileId = UUID.randomUUID();
    }

    @Test
    void getStatistics_ShouldCalculateCorrectMetrics() {
        // Mocks for Wishlist and Shortlist counts
        when(wishlistRepository.count(any(Specification.class))).thenReturn(5L);

        College college = new College();
        college.setId(UUID.randomUUID());

        CollegeBranch cb = new CollegeBranch();
        cb.setCollege(college);
        cb.setFeesPerYear(new BigDecimal("100000.00"));

        AdmissionTracker tracker = new AdmissionTracker();
        tracker.setCurrentStatus(AdmissionStatus.APPLIED);

        Shortlist s1 = Shortlist.builder()
                .collegeBranch(cb)
                .admissionTracker(tracker)
                .isDeleted(false)
                .build();

        when(shortlistRepository.findByStudentProfileIdAndIsDeletedFalse(studentProfileId)).thenReturn(List.of(s1));

        // Recommendations mock
        Recommendation rec = new Recommendation();
        rec.setReturnedCount(10);
        rec.setSafeCount(4);
        rec.setTargetCount(3);
        rec.setDreamCount(3);

        when(recommendationRepository.findByStudentProfileId(eq(studentProfileId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(rec)));

        // Placements mock
        Placement placement = new Placement();
        placement.setAveragePackage(new BigDecimal("6.50"));
        placement.setHighestPackage(new BigDecimal("12.00"));

        when(placementRepository.findByCollegeIdIn(anyList())).thenReturn(List.of(placement));

        // Cutoff mock
        Cutoff cutoff = new Cutoff();
        cutoff.setClosingPercentile(new BigDecimal("92.50"));

        when(cutoffRepository.findByCollegeBranchIdIn(anyList())).thenReturn(List.of(cutoff));

        // Execute service
        DashboardStatisticsResponse stats = dashboardStatisticsService.getStatistics(studentProfileId);

        assertNotNull(stats);
        assertEquals(5L, stats.wishlistCount());
        assertEquals(1L, stats.shortlistCount());
        assertEquals(10L, stats.recommendationCount());
        assertEquals(4L, stats.safeCount());
        assertEquals(new BigDecimal("100000.00"), stats.averageFees());
        assertEquals(new BigDecimal("12.00"), stats.highestPackage());
        assertEquals(new BigDecimal("92.50"), stats.lowestCutoff());
    }
}
