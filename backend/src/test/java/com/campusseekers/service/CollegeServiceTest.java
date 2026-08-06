package com.campusseekers.service;

import com.campusseekers.dto.CollegeRequest;
import com.campusseekers.dto.CollegeResponse;
import com.campusseekers.entity.College;
import com.campusseekers.entity.CollegeStatus;
import com.campusseekers.entity.CollegeType;
import com.campusseekers.exception.DuplicateResourceException;
import com.campusseekers.exception.ResourceNotFoundException;
import com.campusseekers.mapper.CollegeMapper;
import com.campusseekers.repository.CollegeRepository;
import com.campusseekers.service.impl.CollegeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollegeServiceTest {

    @Mock
    private CollegeRepository collegeRepository;

    @Mock
    private CollegeMapper collegeMapper;

    @InjectMocks
    private CollegeServiceImpl collegeService;

    private College mockCollege;
    private CollegeRequest mockRequest;
    private CollegeResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockCollege = College.builder()
                .name("COEP")
                .collegeCode("COEP001")
                .collegeType(CollegeType.GOVERNMENT)
                .establishmentYear(1854)
                .city("Pune")
                .state("Maharashtra")
                .status(CollegeStatus.ACTIVE)
                .build();
        mockCollege.setId(UUID.randomUUID());

        mockRequest = CollegeRequest.builder()
                .name("COEP")
                .collegeCode("COEP001")
                .collegeType(CollegeType.GOVERNMENT)
                .establishmentYear(1854)
                .city("Pune")
                .state("Maharashtra")
                .status(CollegeStatus.ACTIVE)
                .build();

        mockResponse = CollegeResponse.builder()
                .id(mockCollege.getId())
                .name("COEP")
                .collegeCode("COEP001")
                .collegeType(CollegeType.GOVERNMENT)
                .establishmentYear(1854)
                .city("Pune")
                .state("Maharashtra")
                .status(CollegeStatus.ACTIVE)
                .build();
    }

    @Test
    void createCollege_ShouldSaveCollege_WhenCodeIsUnique() {
        when(collegeRepository.existsByCollegeCode("COEP001")).thenReturn(false);
        when(collegeMapper.toEntity(mockRequest)).thenReturn(mockCollege);
        when(collegeRepository.save(mockCollege)).thenReturn(mockCollege);
        when(collegeMapper.toResponse(mockCollege)).thenReturn(mockResponse);

        CollegeResponse response = collegeService.createCollege(mockRequest);

        assertNotNull(response);
        assertEquals("COEP001", response.getCollegeCode());
        verify(collegeRepository, times(1)).save(mockCollege);
    }

    @Test
    void createCollege_ShouldThrowException_WhenCodeExists() {
        when(collegeRepository.existsByCollegeCode("COEP001")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> collegeService.createCollege(mockRequest));
        verify(collegeRepository, never()).save(any());
    }

    @Test
    void updateCollege_ShouldUpdateAndSave_WhenCollegeExists() {
        UUID collegeId = mockCollege.getId();
        when(collegeRepository.findById(collegeId)).thenReturn(Optional.of(mockCollege));
        when(collegeRepository.save(mockCollege)).thenReturn(mockCollege);
        when(collegeMapper.toResponse(mockCollege)).thenReturn(mockResponse);

        CollegeResponse response = collegeService.updateCollege(collegeId, mockRequest);

        assertNotNull(response);
        verify(collegeMapper, times(1)).updateCollegeFromRequest(mockRequest, mockCollege);
        verify(collegeRepository, times(1)).save(mockCollege);
    }

    @Test
    void updateCollege_ShouldThrowException_WhenCollegeDoesNotExist() {
        UUID collegeId = UUID.randomUUID();
        when(collegeRepository.findById(collegeId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> collegeService.updateCollege(collegeId, mockRequest));
        verify(collegeRepository, never()).save(any());
    }

    @Test
    void deleteCollege_ShouldDelete_WhenCollegeExists() {
        UUID collegeId = mockCollege.getId();
        when(collegeRepository.findById(collegeId)).thenReturn(Optional.of(mockCollege));

        collegeService.deleteCollege(collegeId);

        verify(collegeRepository, times(1)).delete(mockCollege);
    }

    @Test
    void deleteCollege_ShouldThrowException_WhenCollegeDoesNotExist() {
        UUID collegeId = UUID.randomUUID();
        when(collegeRepository.findById(collegeId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> collegeService.deleteCollege(collegeId));
        verify(collegeRepository, never()).delete(any(College.class));
    }

    @Test
    void getCollegeById_ShouldReturnCollege_WhenCollegeExists() {
        UUID collegeId = mockCollege.getId();
        when(collegeRepository.findById(collegeId)).thenReturn(Optional.of(mockCollege));
        when(collegeMapper.toResponse(mockCollege)).thenReturn(mockResponse);

        CollegeResponse response = collegeService.getCollegeById(collegeId);

        assertNotNull(response);
        assertEquals(collegeId, response.getId());
    }

    @Test
    void getCollegeById_ShouldThrowException_WhenCollegeDoesNotExist() {
        UUID collegeId = UUID.randomUUID();
        when(collegeRepository.findById(collegeId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> collegeService.getCollegeById(collegeId));
    }

    @Test
    void getAllColleges_ShouldReturnList() {
        when(collegeRepository.findAll()).thenReturn(List.of(mockCollege));
        when(collegeMapper.toResponseList(any())).thenReturn(List.of(mockResponse));

        List<CollegeResponse> response = collegeService.getAllColleges();

        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertEquals(1, response.size());
    }
}
