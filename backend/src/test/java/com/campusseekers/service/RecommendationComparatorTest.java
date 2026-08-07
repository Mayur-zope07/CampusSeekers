package com.campusseekers.service;

import com.campusseekers.entity.*;
import com.campusseekers.model.RecommendationCandidate;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecommendationComparatorTest {

    @Test
    void compare_ShouldSortCorrectly() {
        UUID collegeId1 = UUID.randomUUID();
        UUID collegeId2 = UUID.randomUUID();

        Map<UUID, Placement> placementsMap = new HashMap<>();

        Placement p1 = new Placement();
        p1.setPlacementRatio(new BigDecimal("90.00"));
        p1.setAveragePackage(new BigDecimal("6.50"));
        placementsMap.put(collegeId1, p1);

        Placement p2 = new Placement();
        p2.setPlacementRatio(new BigDecimal("95.00"));
        p2.setAveragePackage(new BigDecimal("8.00"));
        placementsMap.put(collegeId2, p2);

        College college1 = new College();
        college1.setId(collegeId1);
        CollegeBranch cb1 = new CollegeBranch();
        cb1.setCollege(college1);
        cb1.setFeesPerYear(new BigDecimal("100000.00"));

        RecommendationCandidate cand1 = new RecommendationCandidate(
                cb1, new BigDecimal("95.00"), new BigDecimal("92.00"), new BigDecimal("3.00"),
                RecommendationCategory.SAFE, RecommendationReasonCode.PERCENTILE_ABOVE_CUTOFF, "reason"
        );

        College college2 = new College();
        college2.setId(collegeId2);
        CollegeBranch cb2 = new CollegeBranch();
        cb2.setCollege(college2);
        cb2.setFeesPerYear(new BigDecimal("120000.00"));

        RecommendationCandidate cand2 = new RecommendationCandidate(
                cb2, new BigDecimal("95.00"), new BigDecimal("91.00"), new BigDecimal("4.00"),
                RecommendationCategory.SAFE, RecommendationReasonCode.PERCENTILE_ABOVE_CUTOFF, "reason"
        );

        RecommendationCandidate cand3 = new RecommendationCandidate(
                cb2, new BigDecimal("95.00"), new BigDecimal("93.50"), new BigDecimal("1.50"),
                RecommendationCategory.TARGET, RecommendationReasonCode.PERCENTILE_NEAR_CUTOFF, "reason"
        );

        List<RecommendationCandidate> list = new ArrayList<>(List.of(cand3, cand2, cand1));
        list.sort(new RecommendationComparator(placementsMap));

        assertEquals(cand1, list.get(0));
        assertEquals(cand2, list.get(1));
        assertEquals(cand3, list.get(2));
    }
}
