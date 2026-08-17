package com.campusseekers.service.impl;

import com.campusseekers.dto.WishlistRequest;
import com.campusseekers.dto.WishlistResponse;
import com.campusseekers.entity.College;
import com.campusseekers.entity.StudentProfile;
import com.campusseekers.entity.User;
import com.campusseekers.entity.Wishlist;
import com.campusseekers.event.WishlistCreatedEvent;
import com.campusseekers.event.WishlistDeletedEvent;
import com.campusseekers.exception.DuplicateResourceException;
import com.campusseekers.exception.ForbiddenException;
import com.campusseekers.exception.ResourceNotFoundException;
import com.campusseekers.exception.UnauthorizedException;
import com.campusseekers.repository.CollegeRepository;
import com.campusseekers.repository.StudentProfileRepository;
import com.campusseekers.repository.UserRepository;
import com.campusseekers.repository.WishlistRepository;
import com.campusseekers.service.WishlistService;
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

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final CollegeRepository collegeRepository;
    private final WishlistRepository wishlistRepository;
    private final CacheManager cacheManager;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public WishlistResponse addToWishlist(WishlistRequest request) {
        StudentProfile studentProfile = getAuthenticatedStudentProfile();
        log.info("Student {} adding college {} to wishlist", studentProfile.getId(), request.collegeId());

        College college = collegeRepository.findById(request.collegeId())
                .orElseThrow(() -> new ResourceNotFoundException("College not found with ID: " + request.collegeId()));

        Optional<Wishlist> existingOpt = wishlistRepository.findByStudentProfileIdAndCollegeId(studentProfile.getId(), college.getId());
        if (existingOpt.isPresent()) {
            Wishlist existing = existingOpt.get();
            if (!existing.getIsDeleted()) {
                throw new DuplicateResourceException("College is already in your wishlist");
            }
            // Restore soft-deleted item
            existing.setIsDeleted(false);
            Wishlist saved = wishlistRepository.save(existing);
            evictCache(studentProfile.getId());
            eventPublisher.publishEvent(new WishlistCreatedEvent(saved.getId(), studentProfile.getId(), college.getId(), Instant.now()));
            return mapToResponse(saved);
        }

        Wishlist wishlist = Wishlist.builder()
                .studentProfile(studentProfile)
                .college(college)
                .isDeleted(false)
                .build();

        Wishlist saved = wishlistRepository.save(wishlist);
        evictCache(studentProfile.getId());
        eventPublisher.publishEvent(new WishlistCreatedEvent(saved.getId(), studentProfile.getId(), college.getId(), Instant.now()));

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void removeFromWishlist(UUID id) {
        StudentProfile studentProfile = getAuthenticatedStudentProfile();
        log.info("Student {} removing wishlist item {}", studentProfile.getId(), id);

        Wishlist wishlist = wishlistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist item not found with ID: " + id));

        if (!wishlist.getStudentProfile().getId().equals(studentProfile.getId())) {
            throw new ForbiddenException("You are not authorized to delete this wishlist item");
        }

        wishlist.setIsDeleted(true);
        wishlistRepository.save(wishlist);

        evictCache(studentProfile.getId());
        eventPublisher.publishEvent(new WishlistDeletedEvent(id, studentProfile.getId(), Instant.now()));
    }

    @Override
    @Transactional
    public WishlistResponse restoreWishlist(UUID id) {
        StudentProfile studentProfile = getAuthenticatedStudentProfile();
        log.info("Student {} restoring wishlist item {}", studentProfile.getId(), id);

        Wishlist wishlist = wishlistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist item not found with ID: " + id));

        if (!wishlist.getStudentProfile().getId().equals(studentProfile.getId())) {
            throw new ForbiddenException("You are not authorized to restore this wishlist item");
        }

        if (!wishlist.getIsDeleted()) {
            return mapToResponse(wishlist);
        }

        wishlist.setIsDeleted(false);
        Wishlist saved = wishlistRepository.save(wishlist);

        evictCache(studentProfile.getId());
        eventPublisher.publishEvent(new WishlistCreatedEvent(saved.getId(), studentProfile.getId(), saved.getCollege().getId(), Instant.now()));

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WishlistResponse> searchWishlist(String keyword, String naac, Pageable pageable) {
        StudentProfile studentProfile = getAuthenticatedStudentProfile();

        Specification<Wishlist> spec = Specification.where(StudentWorkflowSpecifications.wishlistHasStudent(studentProfile.getId()));

        if (keyword != null && !keyword.isBlank()) {
            spec = spec.and(StudentWorkflowSpecifications.wishlistHasCollegeKeyword(keyword));
        }
        if (naac != null && !naac.isBlank()) {
            spec = spec.and(StudentWorkflowSpecifications.wishlistHasNaac(naac));
        }

        Page<Wishlist> page = wishlistRepository.findAll(spec, pageable);
        return page.map(this::mapToResponse);
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

    private WishlistResponse mapToResponse(Wishlist w) {
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
}
