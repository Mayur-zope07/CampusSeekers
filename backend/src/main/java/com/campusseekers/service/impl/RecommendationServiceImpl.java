package com.campusseekers.service.impl;

import com.campusseekers.config.RecommendationProperties;
import com.campusseekers.dto.*;
import com.campusseekers.entity.*;
import com.campusseekers.event.RecommendationGeneratedEvent;
import com.campusseekers.event.RecommendationViewedEvent;
import com.campusseekers.exception.ForbiddenException;
import com.campusseekers.exception.ResourceNotFoundException;
import com.campusseekers.exception.UnauthorizedException;
import com.campusseekers.model.RecommendationCandidate;
import com.campusseekers.model.RecommendationContext;
import com.campusseekers.repository.*;
import com.campusseekers.service.RecommendationCacheService;
import com.campusseekers.service.RecommendationEngine;
import com.campusseekers.service.RecommendationService;
import com.campusseekers.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final CutoffRepository cutoffRepository;
    private final PlacementRepository placementRepository;
    private final RecommendationRepository recommendationRepository;
    private final RecommendationCacheService cacheService;
    private final RecommendationEngine recommendationEngine;
    private final RecommendationProperties properties;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public RecommendationResponse generateRecommendations(RecommendationRequest request) {
        log.info("Request received to generate recommendations for exam: {}", request.exam());
        long startTime = System.currentTimeMillis();

        StudentProfile studentProfile = getAuthenticatedStudentProfile();
        User user = studentProfile.getUser();

        // 1. Cache lookup
        Optional<Recommendation> cached = cacheService.findCachedRecommendation(studentProfile.getId(), request);
        if (cached.isPresent()) {
            Recommendation rec = cached.get();
            // Publish viewed event
            eventPublisher.publishEvent(new RecommendationViewedEvent(
                    rec.getId(),
                    user.getId(),
                    Instant.now()
            ));
            
            // Map items
            List<UUID> collegeIds = rec.getItems().stream()
                    .map(item -> item.getCollegeBranch().getCollege().getId())
                    .collect(Collectors.toList());
            Map<UUID, Placement> placementsMap = buildPlacementsMap(collegeIds);
            
            return mapToResponse(rec, placementsMap);
        }

        // 2. Load candidate cutoffs
        List<Cutoff> cutoffs = cutoffRepository.findCutoffsForRecommendation(
                request.exam(),
                request.year(),
                request.category()
        );
        int evaluatedCount = cutoffs.size();

        // 3. Load placements bulk
        List<UUID> collegeIds = cutoffs.stream()
                .map(c -> c.getCollegeBranch().getCollege().getId())
                .distinct()
                .collect(Collectors.toList());
        Map<UUID, Placement> placementsMap = buildPlacementsMap(collegeIds);

        // 4. Build Context
        RecommendationContext context = new RecommendationContext(
                studentProfile,
                request,
                properties,
                cutoffs,
                new ArrayList<>(placementsMap.values()),
                placementsMap
        );

        // 5. Execute Engine
        List<RecommendationCandidate> candidates = recommendationEngine.generateRecommendations(context);

        // Classify counts
        int safeCount = 0;
        int targetCount = 0;
        int dreamCount = 0;
        for (RecommendationCandidate cand : candidates) {
            switch (cand.category()) {
                case SAFE -> safeCount++;
                case TARGET -> targetCount++;
                case DREAM -> dreamCount++;
            }
        }

        long executionTimeMs = System.currentTimeMillis() - startTime;

        // 6. Persist Recommendation request
        Recommendation recommendation = Recommendation.builder()
                .studentProfile(studentProfile)
                .examName(request.exam())
                .admissionYear(request.year())
                .percentile(request.percentile())
                .rank(request.rank())
                .category(request.category())
                .preferredBranches(request.preferredBranches())
                .preferredCities(request.preferredCities())
                .preferredCollegeTypes(request.preferredCollegeTypes())
                .minimumNaac(request.minimumNAAC())
                .maximumFees(request.maximumFees())
                .executionTimeMs((int) executionTimeMs)
                .evaluatedCount(evaluatedCount)
                .filteredCount(evaluatedCount - candidates.size()) // Approximate filtered count
                .returnedCount(candidates.size())
                .safeCount(safeCount)
                .targetCount(targetCount)
                .dreamCount(dreamCount)
                .engineVersion(properties.getEngineVersion())
                .algorithmVersion(properties.getAlgorithmVersion())
                .safeThreshold(properties.getSafeThreshold())
                .targetThreshold(properties.getTargetThreshold())
                .dreamThreshold(properties.getDreamThreshold())
                .cacheHit(false)
                .build();

        List<RecommendationItem> items = candidates.stream().map(cand -> RecommendationItem.builder()
                .recommendation(recommendation)
                .collegeBranch(cand.collegeBranch())
                .closingPercentile(cand.closingPercentile())
                .studentPercentile(cand.studentPercentile())
                .percentileDifference(cand.percentileDifference())
                .recommendationCategory(cand.category())
                .recommendationReasonCode(cand.reasonCode())
                .humanReadableReason(cand.humanReadableReason())
                .build()
        ).collect(Collectors.toList());

        recommendation.setItems(items);
        Recommendation saved = recommendationRepository.save(recommendation);

        log.info("Recommendations generated and persisted. ID: {}, time: {} ms", saved.getId(), executionTimeMs);

        // 7. Publish Events
        eventPublisher.publishEvent(new RecommendationGeneratedEvent(
                saved.getId(),
                studentProfile.getId(),
                request,
                Instant.now()
        ));
        eventPublisher.publishEvent(new RecommendationViewedEvent(
                saved.getId(),
                user.getId(),
                Instant.now()
        ));

        return mapToResponse(saved, placementsMap);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RecommendationHistoryResponse> getRecommendationHistory(Pageable pageable) {
        StudentProfile studentProfile = getAuthenticatedStudentProfile();
        Page<Recommendation> page = recommendationRepository.findByStudentProfileId(studentProfile.getId(), pageable);
        return page.map(this::mapToHistoryResponse);
    }

    @Override
    @Transactional
    public RecommendationResponse getRecommendationDetails(UUID id) {
        Recommendation recommendation = recommendationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recommendation not found with ID: " + id));

        // Security check
        String email = SecurityUtils.getCurrentUserEmail()
                .orElseThrow(() -> new UnauthorizedException("User is not authenticated"));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() != Role.ADMIN) {
            // Must be owner
            StudentProfile studentProfile = studentProfileRepository.findByUser(user)
                    .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));
            if (!recommendation.getStudentProfile().getId().equals(studentProfile.getId())) {
                throw new ForbiddenException("You are not authorized to view this recommendation");
            }
        }

        // Fetch placements
        List<UUID> collegeIds = recommendation.getItems().stream()
                .map(item -> item.getCollegeBranch().getCollege().getId())
                .collect(Collectors.toList());
        Map<UUID, Placement> placementsMap = buildPlacementsMap(collegeIds);

        // Publish Viewed Event
        eventPublisher.publishEvent(new RecommendationViewedEvent(
                recommendation.getId(),
                user.getId(),
                Instant.now()
        ));

        return mapToResponse(recommendation, placementsMap);
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

    private RecommendationResponse mapToResponse(Recommendation r, Map<UUID, Placement> placementsMap) {
        List<RecommendationItemResponse> items = r.getItems().stream().map(item -> {
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

        return new RecommendationResponse(
                r.getId(),
                r.getStudentProfile().getId(),
                r.getExamName(),
                r.getAdmissionYear(),
                r.getPercentile(),
                r.getRank(),
                r.getCategory(),
                r.getPreferredBranches() != null ? r.getPreferredBranches() : Collections.emptyList(),
                r.getPreferredCities() != null ? r.getPreferredCities() : Collections.emptyList(),
                r.getPreferredCollegeTypes() != null ? r.getPreferredCollegeTypes() : Collections.emptyList(),
                r.getMinimumNaac(),
                r.getMaximumFees(),
                r.getExecutionTimeMs(),
                r.getEvaluatedCount(),
                r.getFilteredCount(),
                r.getReturnedCount(),
                r.getSafeCount(),
                r.getTargetCount(),
                r.getDreamCount(),
                r.getEngineVersion(),
                r.getAlgorithmVersion(),
                r.getSafeThreshold(),
                r.getTargetThreshold(),
                r.getDreamThreshold(),
                r.getCacheHit(),
                r.getCreatedAt(),
                items
        );
    }

    private RecommendationHistoryResponse mapToHistoryResponse(Recommendation r) {
        return new RecommendationHistoryResponse(
                r.getId(),
                r.getExamName(),
                r.getAdmissionYear(),
                r.getPercentile(),
                r.getRank(),
                r.getCategory(),
                r.getPreferredBranches() != null ? r.getPreferredBranches() : Collections.emptyList(),
                r.getPreferredCities() != null ? r.getPreferredCities() : Collections.emptyList(),
                r.getPreferredCollegeTypes() != null ? r.getPreferredCollegeTypes() : Collections.emptyList(),
                r.getMinimumNaac(),
                r.getMaximumFees(),
                r.getExecutionTimeMs(),
                r.getReturnedCount(),
                r.getCacheHit(),
                r.getCreatedAt()
        );
    }
}
