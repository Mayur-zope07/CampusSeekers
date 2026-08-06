package com.campusseekers.service;

import com.campusseekers.dto.CollegeDetailsResponse;
import com.campusseekers.dto.CollegeListResponse;
import com.campusseekers.dto.CutoffResponse;
import com.campusseekers.dto.PageResponse;
import com.campusseekers.entity.College;
import com.campusseekers.entity.CollegeBranch;
import com.campusseekers.entity.CollegeStatus;
import com.campusseekers.entity.CollegeType;
import com.campusseekers.entity.Cutoff;
import com.campusseekers.mapper.CutoffMapper;
import com.campusseekers.mapper.SearchMapper;
import com.campusseekers.repository.CollegeRepository;
import com.campusseekers.repository.CutoffRepository;
import com.campusseekers.specification.SearchSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollegeSearchService {

    private final CollegeRepository collegeRepository;
    private final CutoffRepository cutoffRepository;
    private final SearchMapper searchMapper;
    private final CutoffMapper cutoffMapper;

    @Transactional(readOnly = true)
    @Cacheable(value = "colleges", key = "{#name, #code, #city, #state, #type, #naacGrade, #nba, #status, #keyword, #pageable}")
    public PageResponse<CollegeListResponse> searchColleges(
            String name, String code, String city, String state,
            CollegeType type, String naacGrade, Boolean nba,
            CollegeStatus status, String keyword, Pageable pageable) {

        log.info("Searching colleges with name: {}, code: {}, city: {}, state: {}, keyword: {}", name, code, city, state, keyword);
        long startTime = System.currentTimeMillis();

        Specification<College> spec = Specification.where(SearchSpecifications.collegeHasKeyword(keyword))
                .and(SearchSpecifications.collegeHasName(name))
                .and(SearchSpecifications.collegeHasCode(code))
                .and(SearchSpecifications.collegeHasCity(city))
                .and(SearchSpecifications.collegeHasState(state))
                .and(SearchSpecifications.collegeHasType(type))
                .and(SearchSpecifications.collegeHasNaacGrade(naacGrade))
                .and(SearchSpecifications.collegeHasNba(nba))
                .and(SearchSpecifications.collegeHasStatus(status));

        Page<College> page = collegeRepository.findAll(spec, pageable);
        Page<CollegeListResponse> responsePage = page.map(searchMapper::toListResponse);

        log.info("Returned {} records in {} ms", responsePage.getNumberOfElements(), (System.currentTimeMillis() - startTime));
        return PageResponse.from(responsePage);
    }

    @Transactional(readOnly = true)
    public CollegeDetailsResponse getCollegeDetails(UUID id) {
        log.info("Retrieving details for college ID: {}", id);
        College college = collegeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("College not found with ID: " + id));

        CollegeDetailsResponse details = searchMapper.toDetailsResponse(college);

        // Fetch latest cutoffs dynamically for the latest year in database
        Integer maxYear = cutoffRepository.findMaxYear().orElse(2025);
        List<CutoffResponse> cutoffResponses = new ArrayList<>();
        for (CollegeBranch cb : college.getCollegeBranches()) {
            List<Cutoff> cutoffs = cutoffRepository.findByCollegeBranchIdAndYear(cb.getId(), maxYear);
            cutoffResponses.addAll(cutoffMapper.toResponseList(cutoffs));
        }
        details.setLatestCutoffs(cutoffResponses);

        return details;
    }
}
