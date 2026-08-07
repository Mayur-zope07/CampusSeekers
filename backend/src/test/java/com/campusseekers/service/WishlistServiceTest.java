package com.campusseekers.service;

import com.campusseekers.dto.WishlistRequest;
import com.campusseekers.dto.WishlistResponse;
import com.campusseekers.entity.College;
import com.campusseekers.entity.StudentProfile;
import com.campusseekers.entity.User;
import com.campusseekers.entity.Wishlist;
import com.campusseekers.exception.DuplicateResourceException;
import com.campusseekers.repository.CollegeRepository;
import com.campusseekers.repository.StudentProfileRepository;
import com.campusseekers.repository.UserRepository;
import com.campusseekers.repository.WishlistRepository;
import com.campusseekers.service.impl.WishlistServiceImpl;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private StudentProfileRepository studentProfileRepository;
    @Mock private CollegeRepository collegeRepository;
    @Mock private WishlistRepository wishlistRepository;
    @Mock private CacheManager cacheManager;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private WishlistServiceImpl wishlistService;

    private MockedStatic<SecurityUtils> mockedSecurityUtils;
    private StudentProfile studentProfile;
    private User user;
    private College college;

    @BeforeEach
    void setUp() {
        mockedSecurityUtils = mockStatic(SecurityUtils.class);
        mockedSecurityUtils.when(SecurityUtils::getCurrentUserEmail).thenReturn(Optional.of("student@example.com"));

        user = new User();
        user.setEmail("student@example.com");

        studentProfile = new StudentProfile();
        studentProfile.setId(UUID.randomUUID());
        studentProfile.setUser(user);

        college = new College();
        college.setId(UUID.randomUUID());
        college.setCollegeCode("1002");
        college.setName("COEP");

        Cache mockCache = mock(Cache.class);
        lenient().when(cacheManager.getCache("dashboardStatistics")).thenReturn(mockCache);
    }

    @AfterEach
    void tearDown() {
        mockedSecurityUtils.close();
    }

    @Test
    void addToWishlist_ShouldSaveNew_WhenNotExisting() {
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(user));
        when(studentProfileRepository.findByUser(user)).thenReturn(Optional.of(studentProfile));
        when(collegeRepository.findById(college.getId())).thenReturn(Optional.of(college));
        when(wishlistRepository.findByStudentProfileIdAndCollegeId(studentProfile.getId(), college.getId())).thenReturn(Optional.empty());

        Wishlist savedWishlist = Wishlist.builder()
                .studentProfile(studentProfile)
                .college(college)
                .isDeleted(false)
                .build();
        savedWishlist.setId(UUID.randomUUID());

        when(wishlistRepository.save(any(Wishlist.class))).thenReturn(savedWishlist);

        WishlistResponse response = wishlistService.addToWishlist(new WishlistRequest(college.getId(), "notes"));

        assertNotNull(response);
        assertEquals(college.getId(), response.collegeId());
        verify(wishlistRepository, times(1)).save(any(Wishlist.class));
        verify(eventPublisher, times(1)).publishEvent(any(Object.class));
    }

    @Test
    void addToWishlist_ShouldThrowDuplicateException_WhenAlreadyExistsActive() {
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(user));
        when(studentProfileRepository.findByUser(user)).thenReturn(Optional.of(studentProfile));
        when(collegeRepository.findById(college.getId())).thenReturn(Optional.of(college));

        Wishlist existing = Wishlist.builder()
                .studentProfile(studentProfile)
                .college(college)
                .isDeleted(false)
                .build();

        when(wishlistRepository.findByStudentProfileIdAndCollegeId(studentProfile.getId(), college.getId())).thenReturn(Optional.of(existing));

        assertThrows(DuplicateResourceException.class, () -> wishlistService.addToWishlist(new WishlistRequest(college.getId(), "notes")));
    }

    @Test
    void removeFromWishlist_ShouldSoftDelete() {
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(user));
        when(studentProfileRepository.findByUser(user)).thenReturn(Optional.of(studentProfile));

        Wishlist existing = Wishlist.builder()
                .studentProfile(studentProfile)
                .college(college)
                .isDeleted(false)
                .build();
        existing.setId(UUID.randomUUID());

        when(wishlistRepository.findById(existing.getId())).thenReturn(Optional.of(existing));

        wishlistService.removeFromWishlist(existing.getId());

        assertTrue(existing.getIsDeleted());
        verify(wishlistRepository, times(1)).save(existing);
        verify(eventPublisher, times(1)).publishEvent(any(Object.class));
    }
}
