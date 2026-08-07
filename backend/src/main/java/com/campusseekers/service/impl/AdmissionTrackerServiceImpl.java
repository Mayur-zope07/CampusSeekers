package com.campusseekers.service.impl;

import com.campusseekers.dto.AdmissionTrackerRequest;
import com.campusseekers.dto.AdmissionTrackerResponse;
import com.campusseekers.dto.AdmissionTrackerHistoryResponse;
import com.campusseekers.entity.*;
import com.campusseekers.event.AdmissionStatusChangedEvent;
import com.campusseekers.exception.ForbiddenException;
import com.campusseekers.exception.ResourceNotFoundException;
import com.campusseekers.exception.UnauthorizedException;
import com.campusseekers.repository.*;
import com.campusseekers.service.AdmissionTrackerService;
import com.campusseekers.util.SecurityUtils;
import com.campusseekers.validation.AdmissionStatusValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdmissionTrackerServiceImpl implements AdmissionTrackerService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final AdmissionTrackerRepository admissionTrackerRepository;
    private final AdmissionTrackerHistoryRepository admissionTrackerHistoryRepository;
    private final AdmissionStatusValidator statusValidator;
    private final CacheManager cacheManager;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public AdmissionTrackerResponse updateStatus(UUID trackerId, AdmissionTrackerRequest request) {
        StudentProfile studentProfile = getAuthenticatedStudentProfile();
        log.info("Student {} updating tracker {} status to {}", studentProfile.getId(), trackerId, request.status());

        AdmissionTracker tracker = admissionTrackerRepository.findById(trackerId)
                .orElseThrow(() -> new ResourceNotFoundException("Admission tracker not found with ID: " + trackerId));

        if (!tracker.getShortlist().getStudentProfile().getId().equals(studentProfile.getId())) {
            throw new ForbiddenException("You are not authorized to update this admission tracker");
        }

        AdmissionStatus previous = tracker.getCurrentStatus();
        AdmissionStatus next = request.status();

        statusValidator.validateTransition(previous, next);

        tracker.setCurrentStatus(next);
        tracker.setRemarks(request.remarks());

        AdmissionTrackerHistory history = AdmissionTrackerHistory.builder()
                .tracker(tracker)
                .previousStatus(previous)
                .newStatus(next)
                .remarks(request.remarks())
                .changedAt(Instant.now())
                .build();

        tracker.getHistory().add(history);
        AdmissionTracker saved = admissionTrackerRepository.save(tracker);

        evictCache(studentProfile.getId());
        eventPublisher.publishEvent(new AdmissionStatusChangedEvent(
                saved.getId(),
                studentProfile.getId(),
                previous,
                next,
                Instant.now()
        ));

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdmissionTrackerHistoryResponse> getHistory(UUID trackerId) {
        StudentProfile studentProfile = getAuthenticatedStudentProfile();

        AdmissionTracker tracker = admissionTrackerRepository.findById(trackerId)
                .orElseThrow(() -> new ResourceNotFoundException("Admission tracker not found with ID: " + trackerId));

        if (!tracker.getShortlist().getStudentProfile().getId().equals(studentProfile.getId())) {
            throw new ForbiddenException("You are not authorized to view this admission history");
        }

        List<AdmissionTrackerHistory> list = admissionTrackerHistoryRepository.findByTrackerShortlistStudentProfileIdAndTrackerIdOrderByChangedAtDesc(
                studentProfile.getId(), trackerId
        );

        return list.stream().map(this::mapToHistoryResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdmissionTrackerResponse> getAdmissionTrackers() {
        StudentProfile studentProfile = getAuthenticatedStudentProfile();
        List<AdmissionTracker> list = admissionTrackerRepository.findByShortlistStudentProfileIdAndShortlistIsDeletedFalse(studentProfile.getId());
        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
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

    private AdmissionTrackerResponse mapToResponse(AdmissionTracker t) {
        return new AdmissionTrackerResponse(
                t.getId(),
                t.getShortlist().getId(),
                t.getCurrentStatus(),
                t.getRemarks(),
                t.getCreatedAt(),
                t.getUpdatedAt()
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
