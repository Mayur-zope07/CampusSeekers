package com.campusseekers.repository;

import com.campusseekers.entity.ExamScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExamScoreRepository extends JpaRepository<ExamScore, UUID> {
    List<ExamScore> findByStudentProfileId(UUID studentProfileId);
}
