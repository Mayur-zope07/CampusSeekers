package com.campusseekers.service.impl;

import com.campusseekers.dto.ShortlistRequest;
import com.campusseekers.dto.ShortlistResponse;
import com.campusseekers.dto.AdmissionTrackerResponse;
import com.campusseekers.entity.*;
import com.campusseekers.event.ShortlistCreatedEvent;
import com.campusseekers.exception.DuplicateResourceException;
import com.campusseekers.exception.ForbiddenException;
import com.campusseekers.exception.ResourceNotFoundException;
import com.campusseekers.exception.UnauthorizedException;
import com.campusseekers.repository.*;
import com.campusseekers.service.ShortlistService;
import com.campusseekers.specification.StudentWorkflowSpecifications;
import com.campusseekers.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShortlistServiceImpl implements ShortlistService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final CollegeBranchRepository collegeBranchRepository;
    private final ShortlistRepository shortlistRepository;
    private final RecommendationItemRepository recommendationItemRepository;
    private final CacheManager cacheManager;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public ShortlistResponse addToShortlist(ShortlistRequest request) {
        StudentProfile studentProfile = getAuthenticatedStudentProfile();
        log.info("Student {} adding branch {} to shortlist", studentProfile.getId(), request.collegeBranchId());

        CollegeBranch cb = collegeBranchRepository.findById(request.collegeBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("College branch not found with ID: " + request.collegeBranchId()));

        Optional<Shortlist> existingOpt = shortlistRepository.findByStudentProfileIdAndCollegeBranchId(studentProfile.getId(), cb.getId());
        if (existingOpt.isPresent()) {
            Shortlist existing = existingOpt.get();
            if (!existing.getIsDeleted()) {
                throw new DuplicateResourceException("This college branch is already shortlisted");
            }
            // Restore soft deleted shortlist
            existing.setIsDeleted(false);
            existing.setPriority(request.priority());
            existing.setNotes(request.notes());
            
            // Re-initialize tracker status to INTERESTED
            if (existing.getAdmissionTracker() == null) {
                initTracker(existing);
            } else {
                AdmissionTracker tracker = existing.getAdmissionTracker();
                AdmissionStatus oldStatus = tracker.getCurrentStatus();
                tracker.setCurrentStatus(AdmissionStatus.INTERESTED);
                tracker.setRemarks("Restored shortlist");
                AdmissionTrackerHistory history = AdmissionTrackerHistory.builder()
                        .tracker(tracker)
                        .previousStatus(oldStatus)
                        .newStatus(AdmissionStatus.INTERESTED)
                        .remarks("Restored shortlist")
                        .changedAt(Instant.now())
                        .build();
                tracker.getHistory().add(history);
            }
            
            Shortlist saved = shortlistRepository.save(existing);
            evictCache(studentProfile.getId());
            eventPublisher.publishEvent(new ShortlistCreatedEvent(saved.getId(), studentProfile.getId(), cb.getId(), Instant.now()));
            return mapToResponse(saved);
        }

        Shortlist shortlist = Shortlist.builder()
                .studentProfile(studentProfile)
                .collegeBranch(cb)
                .priority(request.priority())
                .notes(request.notes())
                .addedAt(Instant.now())
                .isDeleted(false)
                .build();

        initTracker(shortlist);

        Shortlist saved = shortlistRepository.save(shortlist);
        evictCache(studentProfile.getId());
        eventPublisher.publishEvent(new ShortlistCreatedEvent(saved.getId(), studentProfile.getId(), cb.getId(), Instant.now()));

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public ShortlistResponse updateShortlist(UUID id, Integer priority, String notes) {
        StudentProfile studentProfile = getAuthenticatedStudentProfile();
        log.info("Student {} updating shortlist item {}", studentProfile.getId(), id);

        Shortlist shortlist = shortlistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shortlist item not found with ID: " + id));

        if (!shortlist.getStudentProfile().getId().equals(studentProfile.getId())) {
            throw new ForbiddenException("You are not authorized to update this shortlist item");
        }

        shortlist.setPriority(priority);
        shortlist.setNotes(notes);
        Shortlist saved = shortlistRepository.save(shortlist);

        evictCache(studentProfile.getId());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void removeFromShortlist(UUID id) {
        StudentProfile studentProfile = getAuthenticatedStudentProfile();
        log.info("Student {} removing shortlist item {}", studentProfile.getId(), id);

        Shortlist shortlist = shortlistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shortlist item not found with ID: " + id));

        if (!shortlist.getStudentProfile().getId().equals(studentProfile.getId())) {
            throw new ForbiddenException("You are not authorized to delete this shortlist item");
        }

        shortlist.setIsDeleted(true);
        shortlistRepository.save(shortlist);

        evictCache(studentProfile.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShortlistResponse> searchShortlist(
            String collegeKeyword,
            String branchKeyword,
            String city,
            String state,
            String naac,
            BigDecimal maxFees,
            Integer priority,
            AdmissionStatus status,
            RecommendationCategory recCategory,
            Pageable pageable
    ) {
        StudentProfile studentProfile = getAuthenticatedStudentProfile();

        Specification<Shortlist> spec = Specification.where(StudentWorkflowSpecifications.shortlistHasStudent(studentProfile.getId()))
                .and(StudentWorkflowSpecifications.shortlistNotDeleted());

        if (collegeKeyword != null && !collegeKeyword.isBlank()) {
            spec = spec.and(StudentWorkflowSpecifications.shortlistHasCollegeKeyword(collegeKeyword));
        }
        if (branchKeyword != null && !branchKeyword.isBlank()) {
            spec = spec.and(StudentWorkflowSpecifications.shortlistHasBranchKeyword(branchKeyword));
        }
        if (city != null && !city.isBlank()) {
            spec = spec.and(StudentWorkflowSpecifications.shortlistHasCity(city));
        }
        if (state != null && !state.isBlank()) {
            spec = spec.and(StudentWorkflowSpecifications.shortlistHasState(state));
        }
        if (naac != null && !naac.isBlank()) {
            spec = spec.and(StudentWorkflowSpecifications.shortlistHasNaac(naac));
        }
        if (maxFees != null) {
            spec = spec.and(StudentWorkflowSpecifications.shortlistHasMaxFees(maxFees));
        }
        if (priority != null) {
            spec = spec.and(StudentWorkflowSpecifications.shortlistHasPriority(priority));
        }
        if (status != null) {
            spec = spec.and(StudentWorkflowSpecifications.shortlistHasStatus(status));
        }
        if (recCategory != null) {
            spec = spec.and(StudentWorkflowSpecifications.shortlistHasRecommendationCategory(recCategory, studentProfile.getId()));
        }

        Page<Shortlist> page = shortlistRepository.findAll(spec, pageable);
        return page.map(this::mapToResponse);
    }

    @Override
    @Transactional
    public ShortlistResponse importRecommendationToShortlist(UUID recommendationItemId) {
        StudentProfile studentProfile = getAuthenticatedStudentProfile();
        log.info("Student {} importing recommendation item {} to shortlist", studentProfile.getId(), recommendationItemId);

        RecommendationItem recItem = recommendationItemRepository.findById(recommendationItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Recommendation item not found with ID: " + recommendationItemId));

        if (!recItem.getRecommendation().getStudentProfile().getId().equals(studentProfile.getId())) {
            throw new ForbiddenException("You are not authorized to import this recommendation item");
        }

        CollegeBranch cb = recItem.getCollegeBranch();

        Optional<Shortlist> existingOpt = shortlistRepository.findByStudentProfileIdAndCollegeBranchId(studentProfile.getId(), cb.getId());
        if (existingOpt.isPresent()) {
            Shortlist existing = existingOpt.get();
            if (!existing.getIsDeleted()) {
                throw new DuplicateResourceException("This recommended branch is already shortlisted");
            }
            existing.setIsDeleted(false);
            existing.setNotes("Imported from recommendation: " + recItem.getRecommendationCategory());
            
            if (existing.getAdmissionTracker() == null) {
                initTracker(existing);
            } else {
                AdmissionTracker tracker = existing.getAdmissionTracker();
                AdmissionStatus oldStatus = tracker.getCurrentStatus();
                tracker.setCurrentStatus(AdmissionStatus.INTERESTED);
                tracker.setRemarks("Imported from recommendation");
                AdmissionTrackerHistory history = AdmissionTrackerHistory.builder()
                        .tracker(tracker)
                        .previousStatus(oldStatus)
                        .newStatus(AdmissionStatus.INTERESTED)
                        .remarks("Imported from recommendation")
                        .changedAt(Instant.now())
                        .build();
                tracker.getHistory().add(history);
            }

            Shortlist saved = shortlistRepository.save(existing);
            evictCache(studentProfile.getId());
            eventPublisher.publishEvent(new ShortlistCreatedEvent(saved.getId(), studentProfile.getId(), cb.getId(), Instant.now()));
            return mapToResponse(saved);
        }

        Shortlist shortlist = Shortlist.builder()
                .studentProfile(studentProfile)
                .collegeBranch(cb)
                .notes("Imported from recommendation: " + recItem.getRecommendationCategory())
                .addedAt(Instant.now())
                .isDeleted(false)
                .build();

        initTracker(shortlist);

        Shortlist saved = shortlistRepository.save(shortlist);
        evictCache(studentProfile.getId());
        eventPublisher.publishEvent(new ShortlistCreatedEvent(saved.getId(), studentProfile.getId(), cb.getId(), Instant.now()));

        return mapToResponse(saved);
    }

    private void initTracker(Shortlist shortlist) {
        AdmissionTracker tracker = AdmissionTracker.builder()
                .shortlist(shortlist)
                .currentStatus(AdmissionStatus.INTERESTED)
                .remarks("Automatically created on shortlisting")
                .history(new ArrayList<>())
                .build();

        AdmissionTrackerHistory history = AdmissionTrackerHistory.builder()
                .tracker(tracker)
                .previousStatus(null)
                .newStatus(AdmissionStatus.INTERESTED)
                .remarks("Initialized shortlist status")
                .changedAt(Instant.now())
                .build();

        tracker.getHistory().add(history);
        shortlist.setAdmissionTracker(tracker);
    }

    private StudentProfile getAuthenticatedStudentProfile() {
        String email = SecurityUtils.getCurrentUserEmail()
                .orElseThrow(() -> new UnauthorizedException("User is not authenticated"));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return studentProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user: " + email));
    }

    private void evictCache(UUID studentProfileId) {
        if (cacheManager.getCache("dashboardStatistics") != null) {
            cacheManager.getCache("dashboardStatistics").evict(studentProfileId);
            log.info("Evicted dashboardStatistics cache for student profile {}", studentProfileId);
        }
    }

    private ShortlistResponse mapToResponse(Shortlist s) {
        CollegeBranch cb = s.getCollegeBranch();
        College college = cb.getCollege();
        Branch branch = cb.getBranch();

        AdmissionTrackerResponse trackerResponse = null;
        if (s.getAdmissionTracker() != null) {
            AdmissionTracker tracker = s.getAdmissionTracker();
            trackerResponse = new AdmissionTrackerResponse(
                    tracker.getId(),
                    s.getId(),
                    tracker.getCurrentStatus(),
                    tracker.getRemarks(),
                    tracker.getCreatedAt(),
                    tracker.getUpdatedAt()
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
}
