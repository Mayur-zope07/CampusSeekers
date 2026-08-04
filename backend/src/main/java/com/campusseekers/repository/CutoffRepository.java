package com.campusseekers.repository;

import com.campusseekers.entity.Cutoff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CutoffRepository extends JpaRepository<Cutoff, UUID> {
    List<Cutoff> findByCollegeBranchId(UUID collegeBranchId);
    List<Cutoff> findByCollegeBranchIdAndYear(UUID collegeBranchId, Integer year);
}
