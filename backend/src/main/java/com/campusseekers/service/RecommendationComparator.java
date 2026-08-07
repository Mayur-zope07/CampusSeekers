package com.campusseekers.service;

import com.campusseekers.entity.Placement;
import com.campusseekers.model.RecommendationCandidate;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;

public class RecommendationComparator implements Comparator<RecommendationCandidate> {

    private final Map<UUID, Placement> placementsMap;

    public RecommendationComparator(Map<UUID, Placement> placementsMap) {
        this.placementsMap = placementsMap;
    }

    @Override
    public int compare(RecommendationCandidate c1, RecommendationCandidate c2) {
        // 1. Category comparison (SAFE -> TARGET -> DREAM)
        int catCompare = c1.category().compareTo(c2.category());
        if (catCompare != 0) {
            return catCompare;
        }

        // 2. Smallest absolute percentile difference
        int diffCompare = c1.percentileDifference().abs().compareTo(c2.percentileDifference().abs());
        if (diffCompare != 0) {
            return diffCompare;
        }

        // 3. Highest placement ratio
        Placement p1 = placementsMap.get(c1.collegeBranch().getCollege().getId());
        Placement p2 = placementsMap.get(c2.collegeBranch().getCollege().getId());
        BigDecimal ratio1 = p1 != null && p1.getPlacementRatio() != null ? p1.getPlacementRatio() : BigDecimal.ZERO;
        BigDecimal ratio2 = p2 != null && p2.getPlacementRatio() != null ? p2.getPlacementRatio() : BigDecimal.ZERO;
        int ratioCompare = ratio2.compareTo(ratio1); // Descending
        if (ratioCompare != 0) {
            return ratioCompare;
        }

        // 4. Highest average package
        BigDecimal avg1 = p1 != null && p1.getAveragePackage() != null ? p1.getAveragePackage() : BigDecimal.ZERO;
        BigDecimal avg2 = p2 != null && p2.getAveragePackage() != null ? p2.getAveragePackage() : BigDecimal.ZERO;
        int avgCompare = avg2.compareTo(avg1); // Descending
        if (avgCompare != 0) {
            return avgCompare;
        }

        // 5. Lowest fees
        BigDecimal fees1 = c1.collegeBranch().getFeesPerYear() != null ? c1.collegeBranch().getFeesPerYear() : BigDecimal.ZERO;
        BigDecimal fees2 = c2.collegeBranch().getFeesPerYear() != null ? c2.collegeBranch().getFeesPerYear() : BigDecimal.ZERO;
        return fees1.compareTo(fees2); // Ascending
    }
}
