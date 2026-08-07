package com.campusseekers.service;

import com.campusseekers.dto.DashboardResponse;
import com.campusseekers.dto.DashboardStatisticsResponse;
import com.campusseekers.entity.StudentProfile;
import com.campusseekers.entity.User;
import com.campusseekers.repository.*;
import com.campusseekers.service.impl.DashboardServiceImpl;
import com.campusseekers.util.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private StudentProfileRepository studentProfileRepository;
    @Mock private WishlistRepository wishlistRepository;
    @Mock private ShortlistRepository shortlistRepository;
    @Mock private RecommendationRepository recommendationRepository;
    @Mock private PlacementRepository placementRepository;
    @Mock private AdmissionTrackerHistoryRepository admissionTrackerHistoryRepository;
    @Mock private DashboardStatisticsService dashboardStatisticsService;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    private MockedStatic<SecurityUtils> mockedSecurityUtils;
    private StudentProfile studentProfile;
    private User user;

    @BeforeEach
    void setUp() {
        mockedSecurityUtils = mockStatic(SecurityUtils.class);
        mockedSecurityUtils.when(SecurityUtils::getCurrentUserEmail).thenReturn(Optional.of("student@example.com"));

        user = new User();
        user.setEmail("student@example.com");

        studentProfile = new StudentProfile();
        studentProfile.setId(UUID.randomUUID());
        studentProfile.setUser(user);
    }

    @AfterEach
    void tearDown() {
        mockedSecurityUtils.close();
    }

    @Test
    void getDashboard_ShouldAggregateAllMetrics() {
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(user));
        when(studentProfileRepository.findByUser(user)).thenReturn(Optional.of(studentProfile));

        // Mock stats
        DashboardStatisticsResponse mockStats = new DashboardStatisticsResponse(
                5, 2, 10, 4, 3, 3, 1, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN, new HashMap<>()
        );
        when(dashboardStatisticsService.getStatistics(studentProfile.getId())).thenReturn(mockStats);

        // Mock Wishlist
        when(wishlistRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        // Mock Shortlist
        when(shortlistRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        // Mock Recommendations
        when(recommendationRepository.findByStudentProfileId(eq(studentProfile.getId()), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        // Mock Timeline history
        when(admissionTrackerHistoryRepository.findByTrackerShortlistStudentProfileIdOrderByChangedAtDesc(eq(studentProfile.getId()), any(Pageable.class)))
                .thenReturn(Collections.emptyList());

        DashboardResponse response = dashboardService.getDashboard();

        assertNotNull(response);
        assertNotNull(response.statistics());
        assertNotNull(response.recentRecommendations());
        assertNotNull(response.recentWishlist());
        assertNotNull(response.recentShortlists());
        assertNotNull(response.recentAdmissionActivity());
    }
}
