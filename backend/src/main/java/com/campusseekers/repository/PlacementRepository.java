package com.campusseekers.repository;

import com.campusseekers.entity.Placement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlacementRepository extends JpaRepository<Placement, UUID> {
    List<Placement> findByCollegeId(UUID collegeId);
    List<Placement> findByCollegeIdAndYear(UUID collegeId, Integer year);
}
