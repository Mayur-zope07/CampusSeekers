package com.campusseekers.service;

import com.campusseekers.dto.PageResponse;
import com.campusseekers.dto.PlacementResponse;
import com.campusseekers.entity.Placement;
import com.campusseekers.mapper.PlacementMapper;
import com.campusseekers.repository.PlacementRepository;
import com.campusseekers.specification.SearchSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlacementSearchService {

    private final PlacementRepository placementRepository;
    private final PlacementMapper placementMapper;

    @Transactional(readOnly = true)
    public PageResponse<PlacementResponse> searchPlacements(
            String college, Integer year, BigDecimal minAveragePackage,
            BigDecimal minHighestPackage, BigDecimal minRatio, Pageable pageable) {

        log.info("Searching placements with college: {}, year: {}, minAveragePackage: {}", college, year, minAveragePackage);
        long startTime = System.currentTimeMillis();

        Specification<Placement> spec = Specification.where(SearchSpecifications.placementHasCollege(college))
                .and(SearchSpecifications.placementHasYear(year))
                .and(SearchSpecifications.placementHasMinAveragePackage(minAveragePackage))
                .and(SearchSpecifications.placementHasMinHighestPackage(minHighestPackage))
                .and(SearchSpecifications.placementHasMinRatio(minRatio));

        Page<Placement> page = placementRepository.findAll(spec, pageable);
        Page<PlacementResponse> responsePage = page.map(placementMapper::toResponse);

        log.info("Returned {} placement records in {} ms", responsePage.getNumberOfElements(), (System.currentTimeMillis() - startTime));
        return PageResponse.from(responsePage);
    }
}
