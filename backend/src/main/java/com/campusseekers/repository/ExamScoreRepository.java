package com.campusseekers.repository;

import com.campusseekers.entity.ExamScore;
import com.campusseekers.entity.ExamName;
import com.campusseekers.entity.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExamScoreRepository extends JpaRepository<ExamScore, UUID> {
    List<ExamScore> findByStudentProfileId(UUID studentProfileId);
    List<ExamScore> findByStudentProfile(StudentProfile studentProfile);
    Optional<ExamScore> findByIdAndStudentProfile(UUID id, StudentProfile studentProfile);
    boolean existsByStudentProfileAndExamNameAndExamYear(
            StudentProfile studentProfile,
            ExamName examName,
            Integer examYear
    );
}
