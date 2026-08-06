package com.campusseekers.repository;

import com.campusseekers.entity.CollegeBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CollegeBranchRepository extends JpaRepository<CollegeBranch, UUID>, JpaSpecificationExecutor<CollegeBranch> {
    List<CollegeBranch> findByCollegeId(UUID collegeId);
    List<CollegeBranch> findByBranchId(UUID branchId);
    Optional<CollegeBranch> findByCollegeIdAndBranchId(UUID collegeId, UUID branchId);
    boolean existsByCollegeIdAndBranchId(UUID collegeId, UUID branchId);
}
