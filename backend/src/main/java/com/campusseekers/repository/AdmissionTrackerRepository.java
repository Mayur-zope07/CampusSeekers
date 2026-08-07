package com.campusseekers.repository;

import com.campusseekers.entity.AdmissionTracker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdmissionTrackerRepository extends JpaRepository<AdmissionTracker, UUID>, JpaSpecificationExecutor<AdmissionTracker> {
    Optional<AdmissionTracker> findByShortlistId(UUID shortlistId);
    Optional<AdmissionTracker> findByShortlistStudentProfileIdAndShortlistId(UUID studentProfileId, UUID shortlistId);
    List<AdmissionTracker> findByShortlistStudentProfileIdAndShortlistIsDeletedFalse(UUID studentProfileId);
}
