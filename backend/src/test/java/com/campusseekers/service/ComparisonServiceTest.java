package com.campusseekers.service;

import com.campusseekers.dto.ComparisonResponse;
import com.campusseekers.entity.College;
import com.campusseekers.mapper.CollegeBranchMapper;
import com.campusseekers.mapper.CutoffMapper;
import com.campusseekers.mapper.PlacementMapper;
import com.campusseekers.repository.CollegeRepository;
import com.campusseekers.repository.CutoffRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComparisonServiceTest {

    @Mock
    private CollegeRepository collegeRepository;
    @Mock
    private CutoffRepository cutoffRepository;
    @Mock
    private CollegeBranchMapper collegeBranchMapper;
    @Mock
    private PlacementMapper placementMapper;
    @Mock
    private CutoffMapper cutoffMapper;

    @InjectMocks
    private CollegeComparisonService collegeComparisonService;

    @Test
    void compareColleges_ShouldReturnComparisonResponse_WhenValid() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        List<UUID> ids = Arrays.asList(id1, id2);

        College college1 = new College();
        college1.setId(id1);
        college1.setCollegeBranches(Collections.emptyList());

        College college2 = new College();
        college2.setId(id2);
        college2.setCollegeBranches(Collections.emptyList());

        when(collegeRepository.findAllById(ids)).thenReturn(Arrays.asList(college1, college2));
        when(cutoffRepository.findMaxYear()).thenReturn(Optional.of(2025));

        ComparisonResponse response = collegeComparisonService.compareColleges(ids);

        assertNotNull(response);
        assertEquals(2, response.getColleges().size());
        verify(collegeRepository, times(1)).findAllById(ids);
    }

    @Test
    void compareColleges_ShouldThrowException_WhenInvalidSize() {
        assertThrows(IllegalArgumentException.class, () ->
                collegeComparisonService.compareColleges(Collections.singletonList(UUID.randomUUID()))
        );
    }
}
