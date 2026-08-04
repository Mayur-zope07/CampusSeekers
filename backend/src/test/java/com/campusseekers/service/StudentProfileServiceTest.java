package com.campusseekers.service;

import com.campusseekers.dto.StudentProfileRequest;
import com.campusseekers.dto.StudentProfileResponse;
import com.campusseekers.entity.Category;
import com.campusseekers.entity.Gender;
import com.campusseekers.entity.StudentProfile;
import com.campusseekers.entity.User;
import com.campusseekers.exception.DuplicateResourceException;
import com.campusseekers.exception.ResourceNotFoundException;
import com.campusseekers.mapper.StudentProfileMapper;
import com.campusseekers.repository.StudentProfileRepository;
import com.campusseekers.repository.UserRepository;
import com.campusseekers.service.impl.StudentProfileServiceImpl;
import com.campusseekers.util.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentProfileServiceTest {

    @Mock
    private StudentProfileRepository studentProfileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentProfileMapper studentProfileMapper;

    @InjectMocks
    private StudentProfileServiceImpl studentProfileService;

    private User mockUser;
    private StudentProfileRequest mockRequest;
    private StudentProfile mockProfile;
    private StudentProfileResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .email("student@example.com")
                .build();
        mockUser.setId(UUID.randomUUID());

        mockRequest = StudentProfileRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .phone("9876543210")
                .gender(Gender.MALE)
                .category(Category.OPEN)
                .homeState("State")
                .homeDistrict("District")
                .build();

        mockProfile = StudentProfile.builder()
                .firstName("John")
                .lastName("Doe")
                .phone("9876543210")
                .gender(Gender.MALE)
                .category(Category.OPEN)
                .homeState("State")
                .homeDistrict("District")
                .build();
        mockProfile.setId(UUID.randomUUID());
        mockProfile.setUser(mockUser);

        mockResponse = StudentProfileResponse.builder()
                .id(mockProfile.getId())
                .userId(mockUser.getId())
                .firstName("John")
                .lastName("Doe")
                .phone("9876543210")
                .gender(Gender.MALE)
                .category(Category.OPEN)
                .homeState("State")
                .homeDistrict("District")
                .build();

        // Configure Spring Security Context
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(mockUser);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createProfile_ShouldSaveProfile_WhenNoneExists() {
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(mockUser));
        when(studentProfileRepository.existsByUser(mockUser)).thenReturn(false);
        when(studentProfileMapper.toEntity(mockRequest)).thenReturn(mockProfile);
        when(studentProfileRepository.save(mockProfile)).thenReturn(mockProfile);
        when(studentProfileMapper.toResponse(mockProfile)).thenReturn(mockResponse);

        StudentProfileResponse response = studentProfileService.createProfile(mockRequest);

        assertNotNull(response);
        assertEquals("John", response.getFirstName());
        verify(studentProfileRepository, times(1)).save(mockProfile);
    }

    @Test
    void createProfile_ShouldThrowException_WhenProfileExists() {
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(mockUser));
        when(studentProfileRepository.existsByUser(mockUser)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> studentProfileService.createProfile(mockRequest));
        verify(studentProfileRepository, never()).save(any());
    }

    @Test
    void updateProfile_ShouldUpdateAndSaveProfile() {
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(mockUser));
        when(studentProfileRepository.findByUser(mockUser)).thenReturn(Optional.of(mockProfile));
        when(studentProfileRepository.save(mockProfile)).thenReturn(mockProfile);
        when(studentProfileMapper.toResponse(mockProfile)).thenReturn(mockResponse);

        StudentProfileResponse response = studentProfileService.updateProfile(mockRequest);

        assertNotNull(response);
        verify(studentProfileMapper, times(1)).updateProfileFromRequest(mockRequest, mockProfile);
        verify(studentProfileRepository, times(1)).save(mockProfile);
    }

    @Test
    void getCurrentProfile_ShouldReturnProfile() {
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(mockUser));
        when(studentProfileRepository.findByUser(mockUser)).thenReturn(Optional.of(mockProfile));
        when(studentProfileMapper.toResponse(mockProfile)).thenReturn(mockResponse);

        StudentProfileResponse response = studentProfileService.getCurrentProfile();

        assertNotNull(response);
        assertEquals(mockProfile.getId(), response.getId());
    }

    @Test
    void getCurrentProfile_ShouldThrowNotFound_WhenProfileDoesNotExist() {
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(mockUser));
        when(studentProfileRepository.findByUser(mockUser)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studentProfileService.getCurrentProfile());
    }
}
