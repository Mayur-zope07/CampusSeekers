package com.campusseekers.service;

import com.campusseekers.dto.PageResponse;
import com.campusseekers.dto.SearchResultResponse;
import com.campusseekers.entity.Category;
import com.campusseekers.entity.Cutoff;
import com.campusseekers.entity.ExamName;
import com.campusseekers.repository.CutoffRepository;
import com.campusseekers.specification.SearchSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class PercentileSearchService {

    private final CutoffRepository cutoffRepository;

    @Value("${search.safety.safe-threshold:3.0}")
    private double safeThreshold;

    @Value("${search.safety.target-threshold:1.5}")
    private double targetThreshold;

    @Transactional(readOnly = true)
    public PageResponse<SearchResultResponse> searchByPercentile(
            ExamName exam, Integer year, BigDecimal percentile, Category category,
            String branch, String city, String state, Pageable pageable) {

        log.info("Percentile search: exam={}, year={}, percentile={}, category={}, branch={}", exam, year, percentile, category, branch);
        long startTime = System.currentTimeMillis();

        Specification<Cutoff> spec = Specification.where(SearchSpecifications.cutoffHasExam(exam))
                .and(SearchSpecifications.cutoffHasYear(year))
                .and(SearchSpecifications.cutoffHasCategory(category))
                .and(SearchSpecifications.cutoffHasBranch(branch))
                .and(SearchSpecifications.cutoffHasCollegeCity(city))
                .and(SearchSpecifications.cutoffHasCollegeState(state));

        Page<Cutoff> page = cutoffRepository.findAll(spec, pageable);

        Page<SearchResultResponse> responsePage = page.map(cutoff -> {
            BigDecimal cutoffPercentile = cutoff.getClosingPercentile();
            BigDecimal diff = percentile.subtract(cutoffPercentile).setScale(2, RoundingMode.HALF_UP);
            
            double diffDouble = diff.doubleValue();
            String classification;
            if (diffDouble >= safeThreshold) {
                classification = "SAFE";
            } else if (diffDouble >= targetThreshold) {
                classification = "TARGET";
            } else {
                classification = "DREAM";
            }

            return SearchResultResponse.builder()
                    .collegeId(cutoff.getCollegeBranch().getCollege().getId())
                    .collegeCode(cutoff.getCollegeBranch().getCollege().getCollegeCode())
                    .collegeName(cutoff.getCollegeBranch().getCollege().getName())
                    .branchId(cutoff.getCollegeBranch().getBranch().getId())
                    .branchName(cutoff.getCollegeBranch().getBranch().getName())
                    .branchCode(cutoff.getCollegeBranch().getBranch().getBranchCode())
                    .city(cutoff.getCollegeBranch().getCollege().getCity())
                    .state(cutoff.getCollegeBranch().getCollege().getState())
                    .studentPercentile(percentile)
                    .closingPercentile(cutoffPercentile)
                    .difference(diff)
                    .classification(classification)
                    .build();
        });

        log.info("Returned {} percentile search records in {} ms", responsePage.getNumberOfElements(), (System.currentTimeMillis() - startTime));
        return PageResponse.from(responsePage);
    }
}
