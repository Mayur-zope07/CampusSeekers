package com.campusseekers.service.impl;

import com.campusseekers.dto.DashboardStatisticsResponse;
import com.campusseekers.entity.*;
import com.campusseekers.repository.*;
import com.campusseekers.service.DashboardStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardStatisticsServiceImpl implements DashboardStatisticsService {

    private final WishlistRepository wishlistRepository;
    private final ShortlistRepository shortlistRepository;
    private final RecommendationRepository recommendationRepository;
    private final PlacementRepository placementRepository;
    private final CutoffRepository cutoffRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "dashboardStatistics", key = "#studentProfileId")
    public DashboardStatisticsResponse getStatistics(UUID studentProfileId) {
        log.info("Computing dashboard statistics for student profile: {}", studentProfileId);

        // 1. Wishlist Count
        long wishlistCount = wishlistRepository.count(
                org.springframework.data.jpa.domain.Specification.where(
                        com.campusseekers.specification.StudentWorkflowSpecifications.wishlistHasStudent(studentProfileId)
                ).and(com.campusseekers.specification.StudentWorkflowSpecifications.wishlistNotDeleted())
        );

        // 2. Shortlists
        List<Shortlist> shortlists = shortlistRepository.findByStudentProfileIdAndIsDeletedFalse(studentProfileId);
        long shortlistCount = shortlists.size();

        // 3. Recommendation stats
        Page<Recommendation> recPage = recommendationRepository.findByStudentProfileId(
                studentProfileId,
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        long recCount = 0;
        long safeCount = 0;
        long targetCount = 0;
        long dreamCount = 0;

        if (recPage.hasContent()) {
            Recommendation latestRec = recPage.getContent().get(0);
            recCount = latestRec.getReturnedCount() != null ? latestRec.getReturnedCount() : 0;
            safeCount = latestRec.getSafeCount() != null ? latestRec.getSafeCount() : 0;
            targetCount = latestRec.getTargetCount() != null ? latestRec.getTargetCount() : 0;
            dreamCount = latestRec.getDreamCount() != null ? latestRec.getDreamCount() : 0;
        }

        // 4. Applications Count (statuses that count as applications)
        Set<AdmissionStatus> applicationStatuses = Set.of(
                AdmissionStatus.APPLIED,
                AdmissionStatus.DOCUMENTS_UPLOADED,
                AdmissionStatus.DOCUMENTS_VERIFIED,
                AdmissionStatus.SEAT_ALLOTTED,
                AdmissionStatus.CONFIRMED
        );

        long applicationsCount = shortlists.stream()
                .map(Shortlist::getAdmissionTracker)
                .filter(Objects::nonNull)
                .filter(t -> applicationStatuses.contains(t.getCurrentStatus()))
                .count();

        // 5. Average Fees
        BigDecimal averageFees = BigDecimal.ZERO;
        if (!shortlists.isEmpty()) {
            BigDecimal totalFees = BigDecimal.ZERO;
            int countWithFees = 0;
            for (Shortlist s : shortlists) {
                if (s.getCollegeBranch() != null && s.getCollegeBranch().getFeesPerYear() != null) {
                    totalFees = totalFees.add(s.getCollegeBranch().getFeesPerYear());
                    countWithFees++;
                }
            }
            if (countWithFees > 0) {
                averageFees = totalFees.divide(BigDecimal.valueOf(countWithFees), 2, RoundingMode.HALF_UP);
            }
        }

        // 6. Highest Package
        BigDecimal highestPackage = BigDecimal.ZERO;
        List<UUID> collegeIds = shortlists.stream()
                .map(s -> s.getCollegeBranch().getCollege().getId())
                .distinct()
                .collect(Collectors.toList());

        if (!collegeIds.isEmpty()) {
            List<Placement> placements = placementRepository.findByCollegeIdIn(collegeIds);
            highestPackage = placements.stream()
                    .map(p -> {
                        if (p.getHighestPackage() != null) return p.getHighestPackage();
                        if (p.getAveragePackage() != null) return p.getAveragePackage();
                        return BigDecimal.ZERO;
                    })
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
        }

        // 7. Lowest Cutoff Percentile among shortlisted branches
        BigDecimal lowestCutoff = BigDecimal.ZERO;
        List<UUID> cbIds = shortlists.stream()
                .map(s -> s.getCollegeBranch().getId())
                .collect(Collectors.toList());

        if (!cbIds.isEmpty()) {
            List<Cutoff> cutoffs = cutoffRepository.findByCollegeBranchIdIn(cbIds);
            lowestCutoff = cutoffs.stream()
                    .map(Cutoff::getClosingPercentile)
                    .filter(Objects::nonNull)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
        }

        // 8. Status breakdown
        Map<AdmissionStatus, Long> statusBreakdown = new EnumMap<>(AdmissionStatus.class);
        for (AdmissionStatus status : AdmissionStatus.values()) {
            statusBreakdown.put(status, 0L);
        }

        for (Shortlist s : shortlists) {
            if (s.getAdmissionTracker() != null) {
                AdmissionStatus status = s.getAdmissionTracker().getCurrentStatus();
                statusBreakdown.put(status, statusBreakdown.getOrDefault(status, 0L) + 1);
            }
        }

        return new DashboardStatisticsResponse(
                wishlistCount,
                shortlistCount,
                recCount,
                safeCount,
                targetCount,
                dreamCount,
                applicationsCount,
                averageFees,
                highestPackage,
                lowestCutoff,
                statusBreakdown
        );
    }
}
