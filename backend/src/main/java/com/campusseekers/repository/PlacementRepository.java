package com.campusseekers.repository;

import com.campusseekers.entity.Placement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface PlacementRepository extends JpaRepository<Placement, UUID>, JpaSpecificationExecutor<Placement> {
    List<Placement> findByCollegeId(UUID collegeId);
    List<Placement> findByCollegeIdAndYear(UUID collegeId, Integer year);
    List<Placement> findByCollegeIdOrderByYearDesc(UUID collegeId);
    boolean existsByCollegeIdAndYear(UUID collegeId, Integer year);

    List<Placement> findByCollegeIdIn(Collection<UUID> collegeIds);
}

