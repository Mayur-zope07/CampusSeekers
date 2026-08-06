package com.campusseekers.service;

import com.campusseekers.dto.CutoffSearchResponse;
import com.campusseekers.dto.PageResponse;
import com.campusseekers.entity.Category;
import com.campusseekers.entity.Cutoff;
import com.campusseekers.entity.ExamName;
import com.campusseekers.mapper.SearchMapper;
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

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CutoffSearchServiceTest {

    @Mock
    private CutoffRepository cutoffRepository;
    @Mock
    private SearchMapper searchMapper;

    @InjectMocks
    private CutoffSearchService cutoffSearchService;

    @Test
    @SuppressWarnings("unchecked")
    void searchCutoffs_ShouldReturnResults_WhenValid() {
        Cutoff cutoff = new Cutoff();
        Page<Cutoff> page = new PageImpl<>(Collections.singletonList(cutoff));

        when(cutoffRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(searchMapper.toCutoffSearchResponse(any())).thenReturn(new CutoffSearchResponse());

        PageResponse<CutoffSearchResponse> response = cutoffSearchService.searchCutoffs(
                ExamName.MHT_CET, 2025, 1, Category.OPEN, "GOPENS", "GCOEA", "Civil",
                1, 2000, new BigDecimal("95.00"), new BigDecimal("99.99"),
                PageRequest.of(0, 10)
        );

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        verify(cutoffRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }
}
