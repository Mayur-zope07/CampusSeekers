package com.campusseekers.service;

import com.campusseekers.dto.BranchRequest;
import com.campusseekers.dto.BranchResponse;
import com.campusseekers.entity.Branch;
import com.campusseekers.exception.DuplicateResourceException;
import com.campusseekers.exception.ResourceNotFoundException;
import com.campusseekers.mapper.BranchMapper;
import com.campusseekers.repository.BranchRepository;
import com.campusseekers.service.impl.BranchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BranchServiceTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private BranchMapper branchMapper;

    @InjectMocks
    private BranchServiceImpl branchService;

    private Branch mockBranch;
    private BranchRequest mockRequest;
    private BranchResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockBranch = Branch.builder()
                .name("Computer Science")
                .branchCode("CS")
                .build();
        mockBranch.setId(UUID.randomUUID());

        mockRequest = BranchRequest.builder()
                .name("Computer Science")
                .branchCode("CS")
                .build();

        mockResponse = BranchResponse.builder()
                .id(mockBranch.getId())
                .name("Computer Science")
                .branchCode("CS")
                .build();
    }

    @Test
    void createBranch_ShouldSaveBranch_WhenCodeIsUnique() {
        when(branchRepository.existsByBranchCode("CS")).thenReturn(false);
        when(branchMapper.toEntity(mockRequest)).thenReturn(mockBranch);
        when(branchRepository.save(mockBranch)).thenReturn(mockBranch);
        when(branchMapper.toResponse(mockBranch)).thenReturn(mockResponse);

        BranchResponse response = branchService.createBranch(mockRequest);

        assertNotNull(response);
        assertEquals("CS", response.getBranchCode());
        verify(branchRepository, times(1)).save(mockBranch);
    }

    @Test
    void createBranch_ShouldThrowException_WhenCodeExists() {
        when(branchRepository.existsByBranchCode("CS")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> branchService.createBranch(mockRequest));
        verify(branchRepository, never()).save(any());
    }

    @Test
    void updateBranch_ShouldUpdateAndSave_WhenBranchExists() {
        UUID branchId = mockBranch.getId();
        when(branchRepository.findById(branchId)).thenReturn(Optional.of(mockBranch));
        when(branchRepository.save(mockBranch)).thenReturn(mockBranch);
        when(branchMapper.toResponse(mockBranch)).thenReturn(mockResponse);

        BranchResponse response = branchService.updateBranch(branchId, mockRequest);

        assertNotNull(response);
        verify(branchMapper, times(1)).updateBranchFromRequest(mockRequest, mockBranch);
        verify(branchRepository, times(1)).save(mockBranch);
    }

    @Test
    void deleteBranch_ShouldDelete_WhenBranchExists() {
        UUID branchId = mockBranch.getId();
        when(branchRepository.findById(branchId)).thenReturn(Optional.of(mockBranch));

        branchService.deleteBranch(branchId);

        verify(branchRepository, times(1)).delete(mockBranch);
    }

    @Test
    void getBranchById_ShouldReturnBranch_WhenBranchExists() {
        UUID branchId = mockBranch.getId();
        when(branchRepository.findById(branchId)).thenReturn(Optional.of(mockBranch));
        when(branchMapper.toResponse(mockBranch)).thenReturn(mockResponse);

        BranchResponse response = branchService.getBranchById(branchId);

        assertNotNull(response);
        assertEquals(branchId, response.getId());
    }

    @Test
    void getAllBranches_ShouldReturnList() {
        when(branchRepository.findAll()).thenReturn(List.of(mockBranch));
        when(branchMapper.toResponseList(any())).thenReturn(List.of(mockResponse));

        List<BranchResponse> response = branchService.getAllBranches();

        assertNotNull(response);
        assertEquals(1, response.size());
    }
}
