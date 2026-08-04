package com.campusseekers.repository;

import com.campusseekers.entity.Shortlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShortlistRepository extends JpaRepository<Shortlist, UUID> {
    List<Shortlist> findByStudentProfileId(UUID studentProfileId);
    Optional<Shortlist> findByStudentProfileIdAndCollegeBranchId(UUID studentProfileId, UUID collegeBranchId);
}
