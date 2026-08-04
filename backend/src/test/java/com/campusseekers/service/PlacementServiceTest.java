package com.campusseekers.service;

import com.campusseekers.dto.PlacementRequest;
import com.campusseekers.dto.PlacementResponse;
import com.campusseekers.entity.College;
import com.campusseekers.entity.Placement;
import com.campusseekers.exception.DuplicateResourceException;
import com.campusseekers.exception.ResourceNotFoundException;
import com.campusseekers.mapper.PlacementMapper;
import com.campusseekers.repository.CollegeRepository;
import com.campusseekers.repository.PlacementRepository;
import com.campusseekers.service.impl.PlacementServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlacementServiceTest {

    @Mock
    private PlacementRepository placementRepository;

    @Mock
    private CollegeRepository collegeRepository;

    @Mock
    private PlacementMapper placementMapper;

    @InjectMocks
    private PlacementServiceImpl placementService;

    private College mockCollege;
    private Placement mockPlacement;
    private PlacementRequest mockRequest;
    private PlacementResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockCollege = College.builder()
                .name("COEP")
                .build();
        mockCollege.setId(UUID.randomUUID());

        mockPlacement = Placement.builder()
                .college(mockCollege)
                .year(2026)
                .averagePackage(new BigDecimal("12.50"))
                .highestPackage(new BigDecimal("42.00"))
                .placementRatio(new BigDecimal("94.50"))
                .build();
        mockPlacement.setId(UUID.randomUUID());

        mockRequest = PlacementRequest.builder()
                .collegeId(mockCollege.getId())
                .year(2026)
                .averagePackage(new BigDecimal("12.50"))
                .highestPackage(new BigDecimal("42.00"))
                .placementRatio(new BigDecimal("94.50"))
                .build();

        mockResponse = PlacementResponse.builder()
                .id(mockPlacement.getId())
                .collegeId(mockCollege.getId())
                .collegeName("COEP")
                .year(2026)
                .averagePackage(new BigDecimal("12.50"))
                .highestPackage(new BigDecimal("42.00"))
                .placementRatio(new BigDecimal("94.50"))
                .build();
    }

    @Test
    void createPlacement_ShouldSavePlacement_WhenUnique() {
        when(collegeRepository.findById(mockCollege.getId())).thenReturn(Optional.of(mockCollege));
        when(placementRepository.existsByCollegeIdAndYear(mockCollege.getId(), 2026)).thenReturn(false);
        when(placementMapper.toEntity(mockRequest)).thenReturn(mockPlacement);
        when(placementRepository.save(mockPlacement)).thenReturn(mockPlacement);
        when(placementMapper.toResponse(mockPlacement)).thenReturn(mockResponse);

        PlacementResponse response = placementService.createPlacement(mockRequest);

        assertNotNull(response);
        assertEquals(2026, response.getYear());
        verify(placementRepository, times(1)).save(mockPlacement);
    }

    @Test
    void createPlacement_ShouldThrowException_WhenDuplicate() {
        when(collegeRepository.findById(mockCollege.getId())).thenReturn(Optional.of(mockCollege));
        when(placementRepository.existsByCollegeIdAndYear(mockCollege.getId(), 2026)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> placementService.createPlacement(mockRequest));
        verify(placementRepository, never()).save(any());
    }

    @Test
    void updatePlacement_ShouldUpdateAndSave() {
        UUID placementId = mockPlacement.getId();
        when(placementRepository.findById(placementId)).thenReturn(Optional.of(mockPlacement));
        when(collegeRepository.findById(mockCollege.getId())).thenReturn(Optional.of(mockCollege));
        when(placementRepository.save(mockPlacement)).thenReturn(mockPlacement);
        when(placementMapper.toResponse(mockPlacement)).thenReturn(mockResponse);

        PlacementResponse response = placementService.updatePlacement(placementId, mockRequest);

        assertNotNull(response);
        verify(placementMapper, times(1)).updatePlacementFromRequest(mockRequest, mockPlacement);
        verify(placementRepository, times(1)).save(mockPlacement);
    }

    @Test
    void deletePlacement_ShouldDelete_WhenExists() {
        UUID placementId = mockPlacement.getId();
        when(placementRepository.findById(placementId)).thenReturn(Optional.of(mockPlacement));

        placementService.deletePlacement(placementId);

        verify(placementRepository, times(1)).delete(mockPlacement);
    }

    @Test
    void getPlacementById_ShouldReturnPlacement() {
        UUID placementId = mockPlacement.getId();
        when(placementRepository.findById(placementId)).thenReturn(Optional.of(mockPlacement));
        when(placementMapper.toResponse(mockPlacement)).thenReturn(mockResponse);

        PlacementResponse response = placementService.getPlacementById(placementId);

        assertNotNull(response);
        assertEquals(placementId, response.getId());
    }
}
