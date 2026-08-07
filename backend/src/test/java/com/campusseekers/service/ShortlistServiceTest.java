package com.campusseekers.service;

import com.campusseekers.dto.ShortlistRequest;
import com.campusseekers.dto.ShortlistResponse;
import com.campusseekers.entity.*;
import com.campusseekers.exception.DuplicateResourceException;
import com.campusseekers.repository.*;
import com.campusseekers.service.impl.ShortlistServiceImpl;
import com.campusseekers.util.SecurityUtils;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShortlistServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private StudentProfileRepository studentProfileRepository;
    @Mock private CollegeBranchRepository collegeBranchRepository;
    @Mock private ShortlistRepository shortlistRepository;
    @Mock private RecommendationItemRepository recommendationItemRepository;
    @Mock private CacheManager cacheManager;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ShortlistServiceImpl shortlistService;

    private MockedStatic<SecurityUtils> mockedSecurityUtils;
    private StudentProfile studentProfile;
    private User user;
    private CollegeBranch collegeBranch;

    @BeforeEach
    void setUp() {
        mockedSecurityUtils = mockStatic(SecurityUtils.class);
        mockedSecurityUtils.when(SecurityUtils::getCurrentUserEmail).thenReturn(Optional.of("student@example.com"));

        user = new User();
        user.setEmail("student@example.com");

        studentProfile = new StudentProfile();
        studentProfile.setId(UUID.randomUUID());
        studentProfile.setUser(user);

        College college = new College();
        college.setId(UUID.randomUUID());
        college.setName("COEP");
        college.setCollegeCode("1002");

        Branch branch = new Branch();
        branch.setId(UUID.randomUUID());
        branch.setName("Computers");
        branch.setBranchCode("CO");

        collegeBranch = new CollegeBranch();
        collegeBranch.setId(UUID.randomUUID());
        collegeBranch.setCollege(college);
        collegeBranch.setBranch(branch);
        collegeBranch.setFeesPerYear(new BigDecimal("120000.00"));

        Cache mockCache = mock(Cache.class);
        lenient().when(cacheManager.getCache("dashboardStatistics")).thenReturn(mockCache);
    }

    @AfterEach
    void tearDown() {
        mockedSecurityUtils.close();
    }

    @Test
    void addToShortlist_ShouldSaveNew_WhenNotExisting() {
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(user));
        when(studentProfileRepository.findByUser(user)).thenReturn(Optional.of(studentProfile));
        when(collegeBranchRepository.findById(collegeBranch.getId())).thenReturn(Optional.of(collegeBranch));
        when(shortlistRepository.findByStudentProfileIdAndCollegeBranchId(studentProfile.getId(), collegeBranch.getId())).thenReturn(Optional.empty());

        Shortlist savedShortlist = Shortlist.builder()
                .studentProfile(studentProfile)
                .collegeBranch(collegeBranch)
                .priority(1)
                .notes("notes")
                .addedAt(Instant.now())
                .isDeleted(false)
                .build();
        savedShortlist.setId(UUID.randomUUID());

        when(shortlistRepository.save(any(Shortlist.class))).thenReturn(savedShortlist);

        ShortlistResponse response = shortlistService.addToShortlist(new ShortlistRequest(collegeBranch.getId(), 1, "notes"));

        assertNotNull(response);
        assertEquals(collegeBranch.getId(), response.collegeBranchId());
        verify(shortlistRepository, times(1)).save(any(Shortlist.class));
        verify(eventPublisher, times(1)).publishEvent(any(Object.class));
    }

    @Test
    void addToShortlist_ShouldThrowDuplicateException_WhenAlreadyExistsActive() {
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(user));
        when(studentProfileRepository.findByUser(user)).thenReturn(Optional.of(studentProfile));
        when(collegeBranchRepository.findById(collegeBranch.getId())).thenReturn(Optional.of(collegeBranch));

        Shortlist existing = Shortlist.builder()
                .studentProfile(studentProfile)
                .collegeBranch(collegeBranch)
                .isDeleted(false)
                .build();

        when(shortlistRepository.findByStudentProfileIdAndCollegeBranchId(studentProfile.getId(), collegeBranch.getId())).thenReturn(Optional.of(existing));

        assertThrows(DuplicateResourceException.class, () -> shortlistService.addToShortlist(new ShortlistRequest(collegeBranch.getId(), 1, "notes")));
    }
}
