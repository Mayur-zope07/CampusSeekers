package com.campusseekers.service;

import com.campusseekers.dto.PageResponse;
import com.campusseekers.dto.SearchResultResponse;
import com.campusseekers.entity.*;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PercentileSearchServiceTest {

    @Mock
    private CutoffRepository cutoffRepository;

    @InjectMocks
    private PercentileSearchService percentileSearchService;

    @Test
    @SuppressWarnings("unchecked")
    void searchByPercentile_ShouldClassifySuccessfully() {
        ReflectionTestUtils.setField(percentileSearchService, "safeThreshold", 3.0);
        ReflectionTestUtils.setField(percentileSearchService, "targetThreshold", 1.5);

        College college = new College();
        college.setCollegeCode("1002");
        college.setName("GCOEA");
        college.setCity("Amravati");
        college.setState("Maharashtra");

        Branch branch = new Branch();
        branch.setBranchCode("0100219110");
        branch.setName("Civil");

        CollegeBranch cb = new CollegeBranch();
        cb.setCollege(college);
        cb.setBranch(branch);

        Cutoff cutoff = new Cutoff();
        cutoff.setCollegeBranch(cb);
        cutoff.setClosingPercentile(new BigDecimal("92.00"));

        Page<Cutoff> page = new PageImpl<>(Collections.singletonList(cutoff));
        when(cutoffRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        PageResponse<SearchResultResponse> response = percentileSearchService.searchByPercentile(
                ExamName.MHT_CET, 2025, new BigDecimal("95.50"), Category.OPEN,
                "Civil", "Amravati", "Maharashtra", PageRequest.of(0, 10)
        );

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        SearchResultResponse result = response.getContent().get(0);
        assertEquals("SAFE", result.getClassification()); // diff is 3.5 >= 3.0
    }
}
