package com.campusseekers.service;

import com.campusseekers.dto.RecommendationRequest;
import com.campusseekers.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecommendationFilterServiceTest {

    private RecommendationFilterService filterService;
    private College college;
    private Branch branch;
    private CollegeBranch cb;
    private Cutoff cutoff;

    @BeforeEach
    void setUp() {
        filterService = new RecommendationFilterService();

        college = new College();
        college.setName("COEP");
        college.setCity("Pune");
        college.setCollegeType(CollegeType.GOVERNMENT);
        college.setNaacGrade("A++");

        branch = new Branch();
        branch.setBranchCode("CO");
        branch.setName("Computer Engineering");

        cb = new CollegeBranch();
        cb.setCollege(college);
        cb.setBranch(branch);
        cb.setFeesPerYear(new BigDecimal("100000.00"));

        cutoff = new Cutoff();
        cutoff.setCollegeBranch(cb);
        cutoff.setExamName(ExamName.MHT_CET);
        cutoff.setYear(2025);
        cutoff.setCategory(Category.OPEN);
    }

    @Test
    void filter_ShouldInclude_WhenMatchAllFilters() {
        RecommendationRequest req = new RecommendationRequest(
                ExamName.MHT_CET, 2025, new BigDecimal("95.00"), null, Category.OPEN,
                List.of("CO"), List.of("Pune"), List.of(CollegeType.GOVERNMENT), "A", new BigDecimal("120000.00")
        );

        List<Cutoff> filtered = filterService.filter(List.of(cutoff), req);
        assertEquals(1, filtered.size());
    }

    @Test
    void filter_ShouldExclude_WhenCityMismatch() {
        RecommendationRequest req = new RecommendationRequest(
                ExamName.MHT_CET, 2025, new BigDecimal("95.00"), null, Category.OPEN,
                Collections.emptyList(), List.of("Mumbai"), Collections.emptyList(), null, null
        );

        List<Cutoff> filtered = filterService.filter(List.of(cutoff), req);
        assertEquals(0, filtered.size());
    }

    @Test
    void filter_ShouldExclude_WhenFeesExceeded() {
        RecommendationRequest req = new RecommendationRequest(
                ExamName.MHT_CET, 2025, new BigDecimal("95.00"), null, Category.OPEN,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null, new BigDecimal("80000.00")
        );

        List<Cutoff> filtered = filterService.filter(List.of(cutoff), req);
        assertEquals(0, filtered.size());
    }

    @Test
    void filter_ShouldExclude_WhenNaacBelowMinimum() {
        RecommendationRequest req = new RecommendationRequest(
                ExamName.MHT_CET, 2025, new BigDecimal("95.00"), null, Category.OPEN,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), "A++", null
        );
        assertEquals(1, filterService.filter(List.of(cutoff), req).size());

        college.setNaacGrade("B");
        RecommendationRequest req2 = new RecommendationRequest(
                ExamName.MHT_CET, 2025, new BigDecimal("95.00"), null, Category.OPEN,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), "A", null
        );
        assertEquals(0, filterService.filter(List.of(cutoff), req2).size());
    }
}
