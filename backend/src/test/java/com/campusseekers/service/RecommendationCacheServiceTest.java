package com.campusseekers.service;

import com.campusseekers.config.RecommendationProperties;
import com.campusseekers.dto.RecommendationRequest;
import com.campusseekers.entity.Category;
import com.campusseekers.entity.ExamName;
import com.campusseekers.entity.Recommendation;
import com.campusseekers.repository.RecommendationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationCacheServiceTest {

    @Mock
    private RecommendationRepository recommendationRepository;

    @Mock
    private RecommendationProperties properties;

    @InjectMocks
    private RecommendationCacheService cacheService;

    private UUID studentId;
    private RecommendationRequest request;

    @BeforeEach
    void setUp() {
        studentId = UUID.randomUUID();
        request = new RecommendationRequest(
                ExamName.MHT_CET, 2025, new BigDecimal("95.00"), null, Category.OPEN,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null, null
        );
    }

    @Test
    void findCachedRecommendation_ShouldReturnCached_WhenMatchExists() {
        when(properties.getCacheMinutes()).thenReturn(10);

        Recommendation cachedRecommendation = new Recommendation();
        cachedRecommendation.setExamName(ExamName.MHT_CET);
        cachedRecommendation.setAdmissionYear(2025);
        cachedRecommendation.setPercentile(new BigDecimal("95.00"));
        cachedRecommendation.setCategory(Category.OPEN);
        cachedRecommendation.setPreferredBranches(Collections.emptyList());
        cachedRecommendation.setPreferredCities(Collections.emptyList());
        cachedRecommendation.setPreferredCollegeTypes(Collections.emptyList());

        when(recommendationRepository.findByStudentProfileIdAndCreatedAtAfterOrderByCreatedAtDesc(eq(studentId), any(Instant.class)))
                .thenReturn(List.of(cachedRecommendation));

        Optional<Recommendation> result = cacheService.findCachedRecommendation(studentId, request);

        assertTrue(result.isPresent());
    }

    @Test
    void findCachedRecommendation_ShouldReturnEmpty_WhenNoMatchExists() {
        when(properties.getCacheMinutes()).thenReturn(10);

        Recommendation diffRecommendation = new Recommendation();
        diffRecommendation.setExamName(ExamName.MHT_CET);
        diffRecommendation.setAdmissionYear(2025);
        diffRecommendation.setPercentile(new BigDecimal("94.00"));
        diffRecommendation.setCategory(Category.OPEN);

        when(recommendationRepository.findByStudentProfileIdAndCreatedAtAfterOrderByCreatedAtDesc(eq(studentId), any(Instant.class)))
                .thenReturn(List.of(diffRecommendation));

        Optional<Recommendation> result = cacheService.findCachedRecommendation(studentId, request);

        assertFalse(result.isPresent());
    }
}
