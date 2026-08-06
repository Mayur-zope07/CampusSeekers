package com.campusseekers.service;

import com.campusseekers.dto.CollegeDetailsResponse;
import com.campusseekers.dto.CollegeListResponse;
import com.campusseekers.dto.PageResponse;
import com.campusseekers.entity.College;
import com.campusseekers.entity.CollegeStatus;
import com.campusseekers.entity.CollegeType;
import com.campusseekers.mapper.CutoffMapper;
import com.campusseekers.mapper.SearchMapper;
import com.campusseekers.repository.CollegeRepository;
import com.campusseekers.repository.CutoffRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollegeSearchServiceTest {

    @Mock
    private CollegeRepository collegeRepository;
    @Mock
    private CutoffRepository cutoffRepository;
    @Mock
    private SearchMapper searchMapper;
    @Mock
    private CutoffMapper cutoffMapper;

    @InjectMocks
    private CollegeSearchService collegeSearchService;

    @Test
    @SuppressWarnings("unchecked")
    void searchColleges_ShouldReturnRetrievedPage() {
        College college = new College();
        Page<College> page = new PageImpl<>(Collections.singletonList(college));

        when(collegeRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(searchMapper.toListResponse(any())).thenReturn(new CollegeListResponse());

        PageResponse<CollegeListResponse> response = collegeSearchService.searchColleges(
                "GCOEA", null, "Amravati", "Maharashtra",
                CollegeType.GOVERNMENT, "A", true, CollegeStatus.ACTIVE, null,
                PageRequest.of(0, 10)
        );

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        verify(collegeRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getCollegeDetails_ShouldReturnDetails_WhenExists() {
        UUID id = UUID.randomUUID();
        College college = new College();
        college.setId(id);
        college.setCollegeBranches(Collections.emptyList());

        when(collegeRepository.findById(id)).thenReturn(Optional.of(college));
        when(searchMapper.toDetailsResponse(college)).thenReturn(new CollegeDetailsResponse());
        when(cutoffRepository.findMaxYear()).thenReturn(Optional.of(2025));

        CollegeDetailsResponse response = collegeSearchService.getCollegeDetails(id);

        assertNotNull(response);
        verify(collegeRepository, times(1)).findById(id);
    }
}
