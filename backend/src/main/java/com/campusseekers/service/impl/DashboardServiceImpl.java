package com.campusseekers.service.impl;

import com.campusseekers.dto.*;
import com.campusseekers.entity.*;
import com.campusseekers.exception.ResourceNotFoundException;
import com.campusseekers.exception.UnauthorizedException;
import com.campusseekers.repository.*;
import com.campusseekers.service.DashboardService;
import com.campusseekers.service.DashboardStatisticsService;
import com.campusseekers.specification.StudentWorkflowSpecifications;
import com.campusseekers.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final WishlistRepository wishlistRepository;
    private final ShortlistRepository shortlistRepository;
    private final RecommendationRepository recommendationRepository;
    private final PlacementRepository placementRepository;
    private final AdmissionTrackerHistoryRepository admissionTrackerHistoryRepository;
    private final DashboardStatisticsService dashboardStatisticsService;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        StudentProfile studentProfile = getAuthenticatedStudentProfile();
        log.info("Generating dashboard payload for student profile: {}", studentProfile.getId());

        // 1. Get statistics
        DashboardStatisticsResponse stats = dashboardStatisticsService.getStatistics(studentProfile.getId());

        // 2. Recent Recommendations (up to 5)
        List<RecommendationItemResponse> recentRecs = new ArrayList<>();
        Page<Recommendation> recPage = recommendationRepository.findByStudentProfileId(
                studentProfile.getId(),
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        if (recPage.hasContent()) {
            Recommendation latestRec = recPage.getContent().get(0);
            List<RecommendationItem> items = latestRec.getItems().stream()
                    .limit(5)
                    .collect(Collectors.toList());

            List<UUID> collegeIds = items.stream()
                    .map(item -> item.getCollegeBranch().getCollege().getId())
                    .collect(Collectors.toList());
            Map<UUID, Placement> placementsMap = buildPlacementsMap(collegeIds);

            recentRecs = items.stream().map(item -> {
                Placement p = placementsMap.get(item.getCollegeBranch().getCollege().getId());
                return new RecommendationItemResponse(
                        item.getCollegeBranch().getCollege().getId(),
                        item.getCollegeBranch().getCollege().getCollegeCode(),
                        item.getCollegeBranch().getCollege().getName(),
                        item.getCollegeBranch().getBranch().getId(),
                        item.getCollegeBranch().getBranch().getBranchCode(),
                        item.getCollegeBranch().getBranch().getName(),
                        item.getCollegeBranch().getCollege().getCity(),
                        item.getCollegeBranch().getCollege().getState(),
                        item.getCollegeBranch().getCollege().getCollegeType(),
                        item.getCollegeBranch().getCollege().getNaacGrade(),
                        item.getCollegeBranch().getCollege().getNbaAccredited(),
                        item.getCollegeBranch().getDurationYears(),
                        item.getCollegeBranch().getIntakeCapacity(),
                        item.getCollegeBranch().getFeesPerYear(),
                        item.getClosingPercentile(),
                        item.getStudentPercentile(),
                        item.getPercentileDifference(),
                        item.getRecommendationCategory(),
                        item.getRecommendationReasonCode(),
                        item.getHumanReadableReason(),
                        p != null && p.getPlacementRatio() != null ? p.getPlacementRatio() : BigDecimal.ZERO,
                        p != null && p.getAveragePackage() != null ? p.getAveragePackage() : BigDecimal.ZERO,
                        p != null && p.getHighestPackage() != null ? p.getHighestPackage() : BigDecimal.ZERO
                );
            }).collect(Collectors.toList());
        }

        // 3. Recent Wishlist (up to 5)
        Specification<Wishlist> wishlistSpec = Specification.where(StudentWorkflowSpecifications.wishlistHasStudent(studentProfile.getId()))
                .and(StudentWorkflowSpecifications.wishlistNotDeleted());
        Page<Wishlist> wishlistPage = wishlistRepository.findAll(
                wishlistSpec,
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        List<WishlistResponse> recentWishlist = wishlistPage.getContent().stream()
                .map(this::mapToWishlistResponse)
                .collect(Collectors.toList());

        // 4. Recent Shortlists (up to 5, sorted by priority)
        Specification<Shortlist> shortlistSpec = Specification.where(StudentWorkflowSpecifications.shortlistHasStudent(studentProfile.getId()))
                .and(StudentWorkflowSpecifications.shortlistNotDeleted());
        Page<Shortlist> shortlistPage = shortlistRepository.findAll(
                shortlistSpec,
                PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "priority"))
        );
        List<ShortlistResponse> recentShortlist = shortlistPage.getContent().stream()
                .map(this::mapToShortlistResponse)
                .collect(Collectors.toList());

        // 5. Recent Admission Activity (up to 5)
        List<AdmissionTrackerHistory> historyLogs = admissionTrackerHistoryRepository.findByTrackerShortlistStudentProfileIdOrderByChangedAtDesc(
                studentProfile.getId(), PageRequest.of(0, 5)
        );
        List<AdmissionTrackerHistoryResponse> recentActivity = historyLogs.stream()
                .map(this::mapToHistoryResponse)
                .collect(Collectors.toList());

        return new DashboardResponse(
                stats,
                recentRecs,
                recentWishlist,
                recentShortlist,
                recentActivity
        );
    }

    private StudentProfile getAuthenticatedStudentProfile() {
        String email = SecurityUtils.getCurrentUserEmail()
                .orElseThrow(() -> new UnauthorizedException("User is not authenticated"));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return studentProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user: " + email));
    }

    private Map<UUID, Placement> buildPlacementsMap(List<UUID> collegeIds) {
        if (collegeIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Placement> placements = placementRepository.findByCollegeIdIn(collegeIds);
        Map<UUID, Placement> placementsMap = new HashMap<>();
        for (Placement p : placements) {
            UUID id = p.getCollege().getId();
            Placement existing = placementsMap.get(id);
            if (existing == null || p.getYear() > existing.getYear()) {
                placementsMap.put(id, p);
            }
        }
        return placementsMap;
    }

    private WishlistResponse mapToWishlistResponse(Wishlist w) {
        return new WishlistResponse(
                w.getId(),
                w.getStudentProfile().getId(),
                w.getCollege().getId(),
                w.getCollege().getCollegeCode(),
                w.getCollege().getName(),
                w.getCollege().getCity(),
                w.getCollege().getState(),
                w.getCollege().getNaacGrade(),
                w.getCreatedAt(),
                w.getIsDeleted()
        );
    }

    private ShortlistResponse mapToShortlistResponse(Shortlist s) {
        CollegeBranch cb = s.getCollegeBranch();
        College college = cb.getCollege();
        Branch branch = cb.getBranch();

        AdmissionTrackerResponse trackerResponse = null;
        if (s.getAdmissionTracker() != null) {
            AdmissionTracker t = s.getAdmissionTracker();
            trackerResponse = new AdmissionTrackerResponse(
                    t.getId(),
                    s.getId(),
                    t.getCurrentStatus(),
                    t.getRemarks(),
                    t.getCreatedAt(),
                    t.getUpdatedAt()
            );
        }

        return new ShortlistResponse(
                s.getId(),
                s.getStudentProfile().getId(),
                cb.getId(),
                college.getId(),
                college.getCollegeCode(),
                college.getName(),
                branch.getId(),
                branch.getBranchCode(),
                branch.getName(),
                college.getCity(),
                college.getState(),
                college.getNaacGrade(),
                cb.getFeesPerYear(),
                s.getPriority(),
                s.getNotes(),
                s.getIsDeleted(),
                s.getAddedAt(),
                trackerResponse
        );
    }

    private AdmissionTrackerHistoryResponse mapToHistoryResponse(AdmissionTrackerHistory h) {
        return new AdmissionTrackerHistoryResponse(
                h.getId(),
                h.getTracker().getId(),
                h.getPreviousStatus(),
                h.getNewStatus(),
                h.getRemarks(),
                h.getChangedAt()
        );
    }
}
