package com.campusseekers.service;

import com.campusseekers.dto.CutoffRequest;
import com.campusseekers.dto.CutoffResponse;
import com.campusseekers.entity.Category;
import com.campusseekers.entity.CollegeBranch;
import com.campusseekers.entity.Cutoff;
import com.campusseekers.entity.ExamName;
import com.campusseekers.exception.DuplicateResourceException;
import com.campusseekers.exception.ResourceNotFoundException;
import com.campusseekers.mapper.CutoffMapper;
import com.campusseekers.repository.CollegeBranchRepository;
import com.campusseekers.repository.CutoffRepository;
import com.campusseekers.service.impl.CutoffServiceImpl;
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
class CutoffServiceTest {

    @Mock
    private CutoffRepository cutoffRepository;

    @Mock
    private CollegeBranchRepository collegeBranchRepository;

    @Mock
    private CutoffMapper cutoffMapper;

    @InjectMocks
    private CutoffServiceImpl cutoffService;

    private CollegeBranch mockMapping;
    private Cutoff mockCutoff;
    private CutoffRequest mockRequest;
    private CutoffResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockMapping = CollegeBranch.builder().build();
        mockMapping.setId(UUID.randomUUID());

        mockCutoff = Cutoff.builder()
                .collegeBranch(mockMapping)
                .examName(ExamName.MHT_CET)
                .year(2026)
                .round(1)
                .category(Category.OPEN)
                .rawSeatType("GOPENS")
                .stage("I")
                .closingRank(1250)
                .closingPercentile(new BigDecimal("98.45"))
                .build();
        mockCutoff.setId(UUID.randomUUID());

        mockRequest = CutoffRequest.builder()
                .collegeBranchId(mockMapping.getId())
                .examName(ExamName.MHT_CET)
                .year(2026)
                .round(1)
                .category(Category.OPEN)
                .rawSeatType("GOPENS")
                .stage("I")
                .closingRank(1250)
                .closingPercentile(new BigDecimal("98.45"))
                .build();

        mockResponse = CutoffResponse.builder()
                .id(mockCutoff.getId())
                .collegeBranchId(mockMapping.getId())
                .collegeName("COEP")
                .branchName("CS")
                .examName(ExamName.MHT_CET)
                .year(2026)
                .round(1)
                .category(Category.OPEN)
                .rawSeatType("GOPENS")
                .stage("I")
                .closingRank(1250)
                .closingPercentile(new BigDecimal("98.45"))
                .build();
    }

    @Test
    void createCutoff_ShouldSaveCutoff_WhenUnique() {
        when(collegeBranchRepository.findById(mockMapping.getId())).thenReturn(Optional.of(mockMapping));
        when(cutoffRepository.existsByCollegeBranchIdAndExamNameAndYearAndRoundAndCategoryAndRawSeatTypeAndStage(
                mockMapping.getId(), ExamName.MHT_CET, 2026, 1, Category.OPEN, "GOPENS", "I")).thenReturn(false);
        when(cutoffMapper.toEntity(mockRequest)).thenReturn(mockCutoff);
        when(cutoffRepository.save(mockCutoff)).thenReturn(mockCutoff);
        when(cutoffMapper.toResponse(mockCutoff)).thenReturn(mockResponse);

        CutoffResponse response = cutoffService.createCutoff(mockRequest);

        assertNotNull(response);
        assertEquals(1250, response.getClosingRank());
        verify(cutoffRepository, times(1)).save(mockCutoff);
    }

    @Test
    void createCutoff_ShouldThrowException_WhenDuplicate() {
        when(collegeBranchRepository.findById(mockMapping.getId())).thenReturn(Optional.of(mockMapping));
        when(cutoffRepository.existsByCollegeBranchIdAndExamNameAndYearAndRoundAndCategoryAndRawSeatTypeAndStage(
                mockMapping.getId(), ExamName.MHT_CET, 2026, 1, Category.OPEN, "GOPENS", "I")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> cutoffService.createCutoff(mockRequest));
        verify(cutoffRepository, never()).save(any());
    }

    @Test
    void updateCutoff_ShouldUpdateAndSave() {
        UUID cutoffId = mockCutoff.getId();
        when(cutoffRepository.findById(cutoffId)).thenReturn(Optional.of(mockCutoff));
        when(collegeBranchRepository.findById(mockMapping.getId())).thenReturn(Optional.of(mockMapping));
        when(cutoffRepository.save(mockCutoff)).thenReturn(mockCutoff);
        when(cutoffMapper.toResponse(mockCutoff)).thenReturn(mockResponse);

        CutoffResponse response = cutoffService.updateCutoff(cutoffId, mockRequest);

        assertNotNull(response);
        verify(cutoffMapper, times(1)).updateCutoffFromRequest(mockRequest, mockCutoff);
        verify(cutoffRepository, times(1)).save(mockCutoff);
    }

    @Test
    void deleteCutoff_ShouldDelete_WhenExists() {
        UUID cutoffId = mockCutoff.getId();
        when(cutoffRepository.findById(cutoffId)).thenReturn(Optional.of(mockCutoff));

        cutoffService.deleteCutoff(cutoffId);

        verify(cutoffRepository, times(1)).delete(mockCutoff);
    }

    @Test
    void getCutoffById_ShouldReturnCutoff() {
        UUID cutoffId = mockCutoff.getId();
        when(cutoffRepository.findById(cutoffId)).thenReturn(Optional.of(mockCutoff));
        when(cutoffMapper.toResponse(mockCutoff)).thenReturn(mockResponse);

        CutoffResponse response = cutoffService.getCutoffById(cutoffId);

        assertNotNull(response);
        assertEquals(cutoffId, response.getId());
    }
}
