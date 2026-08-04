package com.campusseekers.service.impl;

import com.campusseekers.dto.ExamScoreRequest;
import com.campusseekers.dto.ExamScoreResponse;
import com.campusseekers.entity.ExamScore;
import com.campusseekers.entity.StudentProfile;
import com.campusseekers.entity.User;
import com.campusseekers.exception.DuplicateResourceException;
import com.campusseekers.exception.ResourceNotFoundException;
import com.campusseekers.exception.UnauthorizedException;
import com.campusseekers.mapper.ExamScoreMapper;
import com.campusseekers.repository.ExamScoreRepository;
import com.campusseekers.repository.StudentProfileRepository;
import com.campusseekers.repository.UserRepository;
import com.campusseekers.service.ExamScoreService;
import com.campusseekers.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExamScoreServiceImpl implements ExamScoreService {

    private final ExamScoreRepository examScoreRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;
    private final ExamScoreMapper examScoreMapper;

    @Override
    @Transactional
    public ExamScoreResponse addScore(ExamScoreRequest request) {
        StudentProfile profile = getAuthenticatedStudentProfile();

        if (examScoreRepository.existsByStudentProfileAndExamNameAndExamYear(
                profile, request.getExamName(), request.getExamYear())) {
            throw new DuplicateResourceException("Exam score already exists for " + request.getExamName() + " in year " + request.getExamYear());
        }

        ExamScore score = examScoreMapper.toEntity(request);
        score.setStudentProfile(profile);

        ExamScore savedScore = examScoreRepository.save(score);
        return examScoreMapper.toResponse(savedScore);
    }

    @Override
    @Transactional
    public ExamScoreResponse updateScore(UUID id, ExamScoreRequest request) {
        StudentProfile profile = getAuthenticatedStudentProfile();

        // Enforce ownership check using findByIdAndStudentProfile
        ExamScore score = examScoreRepository.findByIdAndStudentProfile(id, profile)
                .orElseThrow(() -> new ResourceNotFoundException("Exam score not found with ID: " + id));

        // Check if updating to a duplicate (different record exists with same exam & year)
        if ((!score.getExamName().equals(request.getExamName()) || !score.getExamYear().equals(request.getExamYear()))
                && examScoreRepository.existsByStudentProfileAndExamNameAndExamYear(profile, request.getExamName(), request.getExamYear())) {
            throw new DuplicateResourceException("Another exam score already exists for " + request.getExamName() + " in year " + request.getExamYear());
        }

        examScoreMapper.updateScoreFromRequest(request, score);

        ExamScore updatedScore = examScoreRepository.save(score);
        return examScoreMapper.toResponse(updatedScore);
    }

    @Override
    @Transactional
    public void deleteScore(UUID id) {
        StudentProfile profile = getAuthenticatedStudentProfile();

        // Enforce ownership check
        ExamScore score = examScoreRepository.findByIdAndStudentProfile(id, profile)
                .orElseThrow(() -> new ResourceNotFoundException("Exam score not found with ID: " + id));

        examScoreRepository.delete(score);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExamScoreResponse> getScores() {
        StudentProfile profile = getAuthenticatedStudentProfile();
        List<ExamScore> scores = examScoreRepository.findByStudentProfile(profile);
        return examScoreMapper.toResponseList(scores);
    }

    @Override
    @Transactional(readOnly = true)
    public ExamScoreResponse getScore(UUID id) {
        StudentProfile profile = getAuthenticatedStudentProfile();

        // Enforce ownership check
        ExamScore score = examScoreRepository.findByIdAndStudentProfile(id, profile)
                .orElseThrow(() -> new ResourceNotFoundException("Exam score not found with ID: " + id));

        return examScoreMapper.toResponse(score);
    }

    private StudentProfile getAuthenticatedStudentProfile() {
        String email = SecurityUtils.getCurrentUserEmail()
                .orElseThrow(() -> new UnauthorizedException("User is not authenticated"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        return studentProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for this user"));
    }
}
