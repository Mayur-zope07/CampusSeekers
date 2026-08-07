package com.campusseekers.service;

import com.campusseekers.config.RecommendationProperties;
import com.campusseekers.dto.RecommendationRequest;
import com.campusseekers.dto.RecommendationResponse;
import com.campusseekers.entity.*;
import com.campusseekers.repository.*;
import com.campusseekers.service.impl.RecommendationServiceImpl;
import com.campusseekers.util.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private StudentProfileRepository studentProfileRepository;
    @Mock
    private CutoffRepository cutoffRepository;
    @Mock
    private PlacementRepository placementRepository;
    @Mock
    private RecommendationRepository recommendationRepository;
    @Mock
    private RecommendationCacheService cacheService;
    @Mock
    private RecommendationEngine recommendationEngine;
    @Mock
    private RecommendationProperties properties;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private RecommendationServiceImpl recommendationService;

    private MockedStatic<SecurityUtils> mockedSecurityUtils;

    @BeforeEach
    void setUp() {
        mockedSecurityUtils = mockStatic(SecurityUtils.class);
    }

    @AfterEach
    void tearDown() {
        mockedSecurityUtils.close();
    }

    @Test
    void generateRecommendations_ShouldUseCache_WhenCacheHit() {
        mockedSecurityUtils.when(SecurityUtils::getCurrentUserEmail).thenReturn(Optional.of("student@example.com"));

        User user = new User();
        user.setEmail("student@example.com");
        user.setRole(Role.STUDENT);

        StudentProfile profile = new StudentProfile();
        profile.setId(UUID.randomUUID());
        profile.setUser(user);

        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(user));
        when(studentProfileRepository.findByUser(user)).thenReturn(Optional.of(profile));

        RecommendationRequest request = new RecommendationRequest(
                ExamName.MHT_CET, 2025, new BigDecimal("95.00"), null, Category.OPEN,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null, null
        );

        Recommendation cachedRecommendation = new Recommendation();
        cachedRecommendation.setId(UUID.randomUUID());
        cachedRecommendation.setStudentProfile(profile);
        cachedRecommendation.setExamName(ExamName.MHT_CET);
        cachedRecommendation.setAdmissionYear(2025);
        cachedRecommendation.setPercentile(new BigDecimal("95.00"));
        cachedRecommendation.setCategory(Category.OPEN);
        cachedRecommendation.setItems(Collections.emptyList());
        cachedRecommendation.setEvaluatedCount(0);
        cachedRecommendation.setFilteredCount(0);
        cachedRecommendation.setReturnedCount(0);
        cachedRecommendation.setSafeCount(0);
        cachedRecommendation.setTargetCount(0);
        cachedRecommendation.setDreamCount(0);
        cachedRecommendation.setExecutionTimeMs(0);
        cachedRecommendation.setEngineVersion("1.0");
        cachedRecommendation.setAlgorithmVersion("historical-cutoff-v1");
        cachedRecommendation.setSafeThreshold(new BigDecimal("3.0"));
        cachedRecommendation.setTargetThreshold(new BigDecimal("1.5"));
        cachedRecommendation.setDreamThreshold(new BigDecimal("0.0"));
        cachedRecommendation.setCacheHit(true);

        when(cacheService.findCachedRecommendation(profile.getId(), request)).thenReturn(Optional.of(cachedRecommendation));

        RecommendationResponse response = recommendationService.generateRecommendations(request);

        assertNotNull(response);
        verify(recommendationEngine, never()).generateRecommendations(any());
        verify(eventPublisher, times(1)).publishEvent(any(Object.class));
    }
}
