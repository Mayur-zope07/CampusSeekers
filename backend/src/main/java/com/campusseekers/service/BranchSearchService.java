package com.campusseekers.service;

import com.campusseekers.dto.BranchResponse;
import com.campusseekers.dto.CollegeBranchResponse;
import com.campusseekers.dto.PageResponse;
import com.campusseekers.entity.Branch;
import com.campusseekers.entity.CollegeBranch;
import com.campusseekers.mapper.BranchMapper;
import com.campusseekers.mapper.CollegeBranchMapper;
import com.campusseekers.repository.BranchRepository;
import com.campusseekers.repository.CollegeBranchRepository;
import com.campusseekers.specification.SearchSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class BranchSearchService {

    private final BranchRepository branchRepository;
    private final CollegeBranchRepository collegeBranchRepository;
    private final BranchMapper branchMapper;
    private final CollegeBranchMapper collegeBranchMapper;

    @Transactional(readOnly = true)
    @Cacheable(value = "branches", key = "{#name, #code, #keyword, #pageable}")
    public PageResponse<BranchResponse> searchBranches(String name, String code, String keyword, Pageable pageable) {
        log.info("Searching branches with name: {}, code: {}, keyword: {}", name, code, keyword);
        long startTime = System.currentTimeMillis();

        Specification<Branch> spec = Specification.where(SearchSpecifications.branchHasKeyword(keyword))
                .and(SearchSpecifications.branchHasName(name))
                .and(SearchSpecifications.branchHasCode(code));

        Page<Branch> page = branchRepository.findAll(spec, pageable);
        Page<BranchResponse> responsePage = page.map(branchMapper::toResponse);

        log.info("Returned {} branch records in {} ms", responsePage.getNumberOfElements(), (System.currentTimeMillis() - startTime));
        return PageResponse.from(responsePage);
    }

    @Transactional(readOnly = true)
    public PageResponse<CollegeBranchResponse> searchCollegeBranches(
            String college, String branch, BigDecimal minFees, BigDecimal maxFees,
            Integer minIntake, Integer maxIntake, Integer duration, Pageable pageable) {

        log.info("Searching college branches with college: {}, branch: {}, minFees: {}, maxFees: {}", 
                college, branch, minFees, maxFees);
        long startTime = System.currentTimeMillis();

        Specification<CollegeBranch> spec = Specification.where(SearchSpecifications.cbHasCollege(college))
                .and(SearchSpecifications.cbHasBranch(branch))
                .and(SearchSpecifications.cbHasFeesRange(minFees, maxFees))
                .and(SearchSpecifications.cbHasIntakeRange(minIntake, maxIntake))
                .and(SearchSpecifications.cbHasDuration(duration));

        Page<CollegeBranch> page = collegeBranchRepository.findAll(spec, pageable);
        Page<CollegeBranchResponse> responsePage = page.map(collegeBranchMapper::toResponse);

        log.info("Returned {} college branch records in {} ms", responsePage.getNumberOfElements(), (System.currentTimeMillis() - startTime));
        return PageResponse.from(responsePage);
    }
}
