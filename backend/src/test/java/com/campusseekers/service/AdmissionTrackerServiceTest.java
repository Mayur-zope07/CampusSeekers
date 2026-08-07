package com.campusseekers.service;

import com.campusseekers.dto.AdmissionTrackerRequest;
import com.campusseekers.dto.AdmissionTrackerResponse;
import com.campusseekers.entity.*;
import com.campusseekers.repository.*;
import com.campusseekers.service.impl.AdmissionTrackerServiceImpl;
import com.campusseekers.util.SecurityUtils;
import com.campusseekers.validation.AdmissionStatusValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdmissionTrackerServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private StudentProfileRepository studentProfileRepository;
    @Mock private AdmissionTrackerRepository admissionTrackerRepository;
    @Mock private AdmissionTrackerHistoryRepository admissionTrackerHistoryRepository;
    @Mock private AdmissionStatusValidator statusValidator;
    @Mock private CacheManager cacheManager;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AdmissionTrackerServiceImpl admissionTrackerService;

    private MockedStatic<SecurityUtils> mockedSecurityUtils;
    private StudentProfile studentProfile;
    private User user;
    private AdmissionTracker tracker;
    private Shortlist shortlist;

    @BeforeEach
    void setUp() {
        mockedSecurityUtils = mockStatic(SecurityUtils.class);
        mockedSecurityUtils.when(SecurityUtils::getCurrentUserEmail).thenReturn(Optional.of("student@example.com"));

        user = new User();
        user.setEmail("student@example.com");

        studentProfile = new StudentProfile();
        studentProfile.setId(UUID.randomUUID());
        studentProfile.setUser(user);

        shortlist = new Shortlist();
        shortlist.setId(UUID.randomUUID());
        shortlist.setStudentProfile(studentProfile);

        tracker = new AdmissionTracker();
        tracker.setId(UUID.randomUUID());
        tracker.setShortlist(shortlist);
        tracker.setCurrentStatus(AdmissionStatus.INTERESTED);
        tracker.setHistory(new ArrayList<>());

        Cache mockCache = mock(Cache.class);
        lenient().when(cacheManager.getCache("dashboardStatistics")).thenReturn(mockCache);
    }

    @AfterEach
    void tearDown() {
        mockedSecurityUtils.close();
    }

    @Test
    void updateStatus_ShouldChangeStatusAndRecordHistory_WhenTransitionIsValid() {
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(user));
        when(studentProfileRepository.findByUser(user)).thenReturn(Optional.of(studentProfile));
        when(admissionTrackerRepository.findById(tracker.getId())).thenReturn(Optional.of(tracker));
        when(admissionTrackerRepository.save(any(AdmissionTracker.class))).thenReturn(tracker);

        AdmissionTrackerResponse response = admissionTrackerService.updateStatus(
                tracker.getId(), new AdmissionTrackerRequest(AdmissionStatus.APPLIED, "remarks")
        );

        assertNotNull(response);
        assertEquals(AdmissionStatus.APPLIED, response.currentStatus());
        verify(statusValidator, times(1)).validateTransition(AdmissionStatus.INTERESTED, AdmissionStatus.APPLIED);
        verify(admissionTrackerRepository, times(1)).save(tracker);
        verify(eventPublisher, times(1)).publishEvent(any(Object.class));
    }
}
