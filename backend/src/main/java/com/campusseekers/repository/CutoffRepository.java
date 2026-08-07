package com.campusseekers.repository;

import com.campusseekers.entity.Cutoff;
import com.campusseekers.entity.Category;
import com.campusseekers.entity.ExamName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CutoffRepository extends JpaRepository<Cutoff, UUID>, JpaSpecificationExecutor<Cutoff> {
    @Query("SELECT MAX(c.year) FROM Cutoff c")
    Optional<Integer> findMaxYear();

    List<Cutoff> findByCollegeBranchId(UUID collegeBranchId);
    List<Cutoff> findByCollegeBranchIdAndYear(UUID collegeBranchId, Integer year);
    List<Cutoff> findByCollegeBranchIdOrderByYearDesc(UUID collegeBranchId);
    boolean existsByCollegeBranchIdAndExamNameAndYearAndRoundAndCategoryAndRawSeatType(
            UUID collegeBranchId,
            ExamName examName,
            Integer year,
            Integer round,
            Category category,
            String rawSeatType
    );

    @Query("SELECT DISTINCT c FROM Cutoff c " +
           "JOIN FETCH c.collegeBranch cb " +
           "JOIN FETCH cb.college col " +
           "JOIN FETCH cb.branch b " +
           "WHERE c.examName = :examName " +
           "AND c.year = :year " +
           "AND c.category = :category")
    List<Cutoff> findCutoffsForRecommendation(
            @Param("examName") ExamName examName,
            @Param("year") Integer year,
            @Param("category") Category category
    );
}

