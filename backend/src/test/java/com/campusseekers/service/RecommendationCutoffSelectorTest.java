package com.campusseekers.service;

import com.campusseekers.config.RecommendationProperties;
import com.campusseekers.entity.Category;
import com.campusseekers.entity.CollegeBranch;
import com.campusseekers.entity.Cutoff;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class RecommendationCutoffSelectorTest {

    private RecommendationProperties properties;
    private RecommendationCutoffSelector selector;
    private CollegeBranch mockBranch;

    @BeforeEach
    void setUp() {
        properties = Mockito.mock(RecommendationProperties.class);
        when(properties.getFallbackSelectionPolicy()).thenReturn("HIGHEST_PERCENTILE");
        selector = new RecommendationCutoffSelector(properties);

        mockBranch = new CollegeBranch();
        mockBranch.setId(UUID.randomUUID());
    }

    @Test
    void selectRepresentativeCutoffs_ShouldSelectMHOverStageI() {
        Cutoff c1 = Cutoff.builder()
                .collegeBranch(mockBranch)
                .round(4)
                .category(Category.OPEN)
                .rawSeatType("MI")
                .stage("I")
                .closingRank(193945)
                .closingPercentile(new BigDecimal("11.06"))
                .build();

        Cutoff c2 = Cutoff.builder()
                .collegeBranch(mockBranch)
                .round(4)
                .category(Category.OPEN)
                .rawSeatType("MI")
                .stage("MH")
                .closingRank(74614)
                .closingPercentile(new BigDecimal("76.92"))
                .build();

        List<Cutoff> result = selector.selectRepresentativeCutoffs(List.of(c1, c2));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("MH", result.get(0).getStage());
        assertEquals(74614, result.get(0).getClosingRank());
    }

    @Test
    void selectRepresentativeCutoffs_ShouldSelectVIIOverStageI() {
        Cutoff c1 = Cutoff.builder()
                .collegeBranch(mockBranch)
                .round(4)
                .category(Category.OPEN)
                .rawSeatType("GSEBCH")
                .stage("I")
                .closingRank(190171)
                .closingPercentile(new BigDecimal("14.31"))
                .build();

        Cutoff c2 = Cutoff.builder()
                .collegeBranch(mockBranch)
                .round(4)
                .category(Category.OPEN)
                .rawSeatType("GSEBCH")
                .stage("VII")
                .closingRank(93997)
                .closingPercentile(new BigDecimal("69.85"))
                .build();

        List<Cutoff> result = selector.selectRepresentativeCutoffs(List.of(c1, c2));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("VII", result.get(0).getStage());
        assertEquals(93997, result.get(0).getClosingRank());
    }

    @Test
    void selectRepresentativeCutoffs_ShouldFallbackToHighestPercentile_WhenStagesAreUnknown() {
        Cutoff c1 = Cutoff.builder()
                .collegeBranch(mockBranch)
                .round(4)
                .category(Category.OPEN)
                .rawSeatType("XYZ")
                .stage("UNKNOWN_STAGE_A")
                .closingRank(150000)
                .closingPercentile(new BigDecimal("30.00"))
                .build();

        Cutoff c2 = Cutoff.builder()
                .collegeBranch(mockBranch)
                .round(4)
                .category(Category.OPEN)
                .rawSeatType("XYZ")
                .stage("UNKNOWN_STAGE_B")
                .closingRank(100000)
                .closingPercentile(new BigDecimal("50.00"))
                .build();

        List<Cutoff> result = selector.selectRepresentativeCutoffs(List.of(c1, c2));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("UNKNOWN_STAGE_B", result.get(0).getStage());
        assertEquals(new BigDecimal("50.00"), result.get(0).getClosingPercentile());
    }

    @Test
    void selectRepresentativeCutoffs_ShouldFallbackToLowestPercentile_WhenConfigured() {
        when(properties.getFallbackSelectionPolicy()).thenReturn("LOWEST_PERCENTILE");

        Cutoff c1 = Cutoff.builder()
                .collegeBranch(mockBranch)
                .round(4)
                .category(Category.OPEN)
                .rawSeatType("XYZ")
                .stage("UNKNOWN_STAGE_A")
                .closingRank(150000)
                .closingPercentile(new BigDecimal("30.00"))
                .build();

        Cutoff c2 = Cutoff.builder()
                .collegeBranch(mockBranch)
                .round(4)
                .category(Category.OPEN)
                .rawSeatType("XYZ")
                .stage("UNKNOWN_STAGE_B")
                .closingRank(100000)
                .closingPercentile(new BigDecimal("50.00"))
                .build();

        List<Cutoff> result = selector.selectRepresentativeCutoffs(List.of(c1, c2));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("UNKNOWN_STAGE_A", result.get(0).getStage());
        assertEquals(new BigDecimal("30.00"), result.get(0).getClosingPercentile());
    }
}
