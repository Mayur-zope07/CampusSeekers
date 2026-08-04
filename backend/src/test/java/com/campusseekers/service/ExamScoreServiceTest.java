package com.campusseekers.service;

import com.campusseekers.dto.ExamScoreRequest;
import com.campusseekers.dto.ExamScoreResponse;
import com.campusseekers.entity.Category;
import com.campusseekers.entity.ExamName;
import com.campusseekers.entity.ExamScore;
import com.campusseekers.entity.Gender;
import com.campusseekers.entity.StudentProfile;
import com.campusseekers.entity.User;
import com.campusseekers.exception.DuplicateResourceException;
import com.campusseekers.exception.ResourceNotFoundException;
import com.campusseekers.mapper.ExamScoreMapper;
import com.campusseekers.repository.ExamScoreRepository;
import com.campusseekers.repository.StudentProfileRepository;
import com.campusseekers.repository.UserRepository;
import com.campusseekers.service.impl.ExamScoreServiceImpl;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExamScoreServiceTest {

    @Mock
    private ExamScoreRepository examScoreRepository;

    @Mock
    private StudentProfileRepository studentProfileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExamScoreMapper examScoreMapper;

    @InjectMocks
    private ExamScoreServiceImpl examScoreService;

    private User mockUser;
    private StudentProfile mockProfile;
    private ExamScoreRequest mockRequest;
    private ExamScore mockScore;
    private ExamScoreResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .email("student@example.com")
                .build();
        mockUser.setId(UUID.randomUUID());

        mockProfile = StudentProfile.builder()
                .firstName("John")
                .lastName("Doe")
                .phone("9876543210")
                .gender(Gender.MALE)
                .category(Category.OPEN)
                .build();
        mockProfile.setId(UUID.randomUUID());
        mockProfile.setUser(mockUser);

        mockRequest = ExamScoreRequest.builder()
                .examName(ExamName.MHT_CET)
                .examYear(2026)
                .rank(120)
                .percentile(new BigDecimal("99.45"))
                .marks(180)
                .build();

        mockScore = ExamScore.builder()
                .examName(ExamName.MHT_CET)
                .examYear(2026)
                .scoreRank(120)
                .scorePercentile(new BigDecimal("99.45"))
                .marks(180)
                .build();
        mockScore.setId(UUID.randomUUID());
        mockScore.setStudentProfile(mockProfile);

        mockResponse = ExamScoreResponse.builder()
                .id(mockScore.getId())
                .examName(ExamName.MHT_CET)
                .examYear(2026)
                .rank(120)
                .percentile(new BigDecimal("99.45"))
                .marks(180)
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
    void addScore_ShouldSaveScore_WhenNoDuplicateExists() {
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(mockUser));
        when(studentProfileRepository.findByUser(mockUser)).thenReturn(Optional.of(mockProfile));
        when(examScoreRepository.existsByStudentProfileAndExamNameAndExamYear(mockProfile, ExamName.MHT_CET, 2026)).thenReturn(false);
        when(examScoreMapper.toEntity(mockRequest)).thenReturn(mockScore);
        when(examScoreRepository.save(mockScore)).thenReturn(mockScore);
        when(examScoreMapper.toResponse(mockScore)).thenReturn(mockResponse);

        ExamScoreResponse response = examScoreService.addScore(mockRequest);

        assertNotNull(response);
        assertEquals(2026, response.getExamYear());
        verify(examScoreRepository, times(1)).save(mockScore);
    }

    @Test
    void addScore_ShouldThrowException_WhenDuplicateRecordExists() {
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(mockUser));
        when(studentProfileRepository.findByUser(mockUser)).thenReturn(Optional.of(mockProfile));
        when(examScoreRepository.existsByStudentProfileAndExamNameAndExamYear(mockProfile, ExamName.MHT_CET, 2026)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> examScoreService.addScore(mockRequest));
        verify(examScoreRepository, never()).save(any());
    }

    @Test
    void updateScore_ShouldUpdateAndSave_WhenOwnershipIsConfirmed() {
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(mockUser));
        when(studentProfileRepository.findByUser(mockUser)).thenReturn(Optional.of(mockProfile));
        when(examScoreRepository.findByIdAndStudentProfile(mockScore.getId(), mockProfile)).thenReturn(Optional.of(mockScore));
        when(examScoreRepository.save(mockScore)).thenReturn(mockScore);
        when(examScoreMapper.toResponse(mockScore)).thenReturn(mockResponse);

        ExamScoreResponse response = examScoreService.updateScore(mockScore.getId(), mockRequest);

        assertNotNull(response);
        verify(examScoreMapper, times(1)).updateScoreFromRequest(mockRequest, mockScore);
        verify(examScoreRepository, times(1)).save(mockScore);
    }

    @Test
    void updateScore_ShouldThrowNotFound_WhenNotOwner() {
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(mockUser));
        when(studentProfileRepository.findByUser(mockUser)).thenReturn(Optional.of(mockProfile));
        when(examScoreRepository.findByIdAndStudentProfile(mockScore.getId(), mockProfile)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> examScoreService.updateScore(mockScore.getId(), mockRequest));
        verify(examScoreRepository, never()).save(any());
    }

    @Test
    void deleteScore_ShouldDelete_WhenOwnershipIsConfirmed() {
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(mockUser));
        when(studentProfileRepository.findByUser(mockUser)).thenReturn(Optional.of(mockProfile));
        when(examScoreRepository.findByIdAndStudentProfile(mockScore.getId(), mockProfile)).thenReturn(Optional.of(mockScore));

        examScoreService.deleteScore(mockScore.getId());

        verify(examScoreRepository, times(1)).delete(mockScore);
    }

    @Test
    void getScore_ShouldReturnScore_WhenOwnershipIsConfirmed() {
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(mockUser));
        when(studentProfileRepository.findByUser(mockUser)).thenReturn(Optional.of(mockProfile));
        when(examScoreRepository.findByIdAndStudentProfile(mockScore.getId(), mockProfile)).thenReturn(Optional.of(mockScore));
        when(examScoreMapper.toResponse(mockScore)).thenReturn(mockResponse);

        ExamScoreResponse response = examScoreService.getScore(mockScore.getId());

        assertNotNull(response);
        assertEquals(mockScore.getId(), response.getId());
    }
}
