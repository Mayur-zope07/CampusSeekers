package com.campusseekers.repository;

import com.campusseekers.entity.AdmissionTrackerHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AdmissionTrackerHistoryRepository extends JpaRepository<AdmissionTrackerHistory, UUID> {
    List<AdmissionTrackerHistory> findByTrackerIdOrderByChangedAtDesc(UUID trackerId);
    List<AdmissionTrackerHistory> findByTrackerShortlistStudentProfileIdAndTrackerIdOrderByChangedAtDesc(UUID studentProfileId, UUID trackerId);
    List<AdmissionTrackerHistory> findByTrackerShortlistStudentProfileIdOrderByChangedAtDesc(UUID studentProfileId, org.springframework.data.domain.Pageable pageable);
}
