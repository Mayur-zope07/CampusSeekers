package com.campusseekers.service;

import com.campusseekers.dto.CutoffSearchResponse;
import com.campusseekers.dto.PageResponse;
import com.campusseekers.entity.Category;
import com.campusseekers.entity.Cutoff;
import com.campusseekers.entity.ExamName;
import com.campusseekers.mapper.SearchMapper;
import com.campusseekers.repository.CutoffRepository;
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
public class CutoffSearchService {

    private final CutoffRepository cutoffRepository;
    private final SearchMapper searchMapper;

    @Transactional(readOnly = true)
    public PageResponse<CutoffSearchResponse> searchCutoffs(
            ExamName exam, Integer year, Integer round, Category category,
            String rawSeatType, String college, String branch,
            Integer minRank, Integer maxRank, BigDecimal minPercentile, BigDecimal maxPercentile,
            Pageable pageable) {

        log.info("Searching cutoffs with exam: {}, year: {}, round: {}, category: {}, rawSeatType: {}, college: {}, branch: {}", 
                exam, year, round, category, rawSeatType, college, branch);
        long startTime = System.currentTimeMillis();

        Specification<Cutoff> spec = Specification.where(SearchSpecifications.cutoffHasExam(exam))
                .and(SearchSpecifications.cutoffHasYear(year))
                .and(SearchSpecifications.cutoffHasRound(round))
                .and(SearchSpecifications.cutoffHasCategory(category))
                .and(SearchSpecifications.cutoffHasRawSeatType(rawSeatType))
                .and(SearchSpecifications.cutoffHasCollege(college))
                .and(SearchSpecifications.cutoffHasBranch(branch))
                .and(SearchSpecifications.cutoffHasRankRange(minRank, maxRank))
                .and(SearchSpecifications.cutoffHasPercentileRange(minPercentile, maxPercentile));

        Page<Cutoff> page = cutoffRepository.findAll(spec, pageable);
        Page<CutoffSearchResponse> responsePage = page.map(searchMapper::toCutoffSearchResponse);

        log.info("Returned {} cutoff records in {} ms", responsePage.getNumberOfElements(), (System.currentTimeMillis() - startTime));
        return PageResponse.from(responsePage);
    }
}
