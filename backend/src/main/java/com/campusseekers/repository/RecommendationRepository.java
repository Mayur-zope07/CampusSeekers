package com.campusseekers.repository;

import com.campusseekers.entity.Recommendation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, UUID> {
    Page<Recommendation> findByStudentProfileId(UUID studentProfileId, Pageable pageable);
    List<Recommendation> findByStudentProfileIdAndCreatedAtAfterOrderByCreatedAtDesc(UUID studentProfileId, Instant since);
}
