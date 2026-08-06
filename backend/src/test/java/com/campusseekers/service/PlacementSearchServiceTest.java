package com.campusseekers.service;

import com.campusseekers.dto.PageResponse;
import com.campusseekers.dto.PlacementResponse;
import com.campusseekers.entity.Placement;
import com.campusseekers.mapper.PlacementMapper;
import com.campusseekers.repository.PlacementRepository;
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
class PlacementSearchServiceTest {

    @Mock
    private PlacementRepository placementRepository;
    @Mock
    private PlacementMapper placementMapper;

    @InjectMocks
    private PlacementSearchService placementSearchService;

    @Test
    @SuppressWarnings("unchecked")
    void searchPlacements_ShouldReturnResults_WhenValid() {
        Placement placement = new Placement();
        Page<Placement> page = new PageImpl<>(Collections.singletonList(placement));

        when(placementRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(placementMapper.toResponse(any())).thenReturn(new PlacementResponse());

        PageResponse<PlacementResponse> response = placementSearchService.searchPlacements(
                "GCOEA", 2025, new BigDecimal("6.00"), new BigDecimal("12.00"), new BigDecimal("80.00"),
                PageRequest.of(0, 10)
        );

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        verify(placementRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }
}
