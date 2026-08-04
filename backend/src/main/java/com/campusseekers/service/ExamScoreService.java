package com.campusseekers.service;

import com.campusseekers.dto.ExamScoreRequest;
import com.campusseekers.dto.ExamScoreResponse;

import java.util.List;
import java.util.UUID;

public interface ExamScoreService {
    ExamScoreResponse addScore(ExamScoreRequest request);
    ExamScoreResponse updateScore(UUID id, ExamScoreRequest request);
    void deleteScore(UUID id);
    List<ExamScoreResponse> getScores();
    ExamScoreResponse getScore(UUID id);
}
