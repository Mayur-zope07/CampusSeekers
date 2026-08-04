package com.campusseekers.controller;

import com.campusseekers.dto.ApiResponse;
import com.campusseekers.dto.ExamScoreRequest;
import com.campusseekers.dto.ExamScoreResponse;
import com.campusseekers.service.ExamScoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile/scores")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Exam Scores", description = "Endpoints for managing student entrance exam scores")
public class ExamScoreController {

    private final ExamScoreService examScoreService;

    @GetMapping
    @Operation(summary = "Get list of all registered exam scores", description = "Returns all test scores belonging to the logged-in student.")
    public ResponseEntity<ApiResponse<List<ExamScoreResponse>>> getScores() {
        List<ExamScoreResponse> responses = examScoreService.getScores();
        return ResponseEntity.ok(ApiResponse.<List<ExamScoreResponse>>builder()
                .success(true)
                .message("Exam scores retrieved successfully")
                .data(responses)
                .build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get detailed information of a specific score", description = "Retrieves one exam score. Mismatched ownership requests return 404/403.")
    public ResponseEntity<ApiResponse<ExamScoreResponse>> getScore(@PathVariable UUID id) {
        ExamScoreResponse response = examScoreService.getScore(id);
        return ResponseEntity.ok(ApiResponse.<ExamScoreResponse>builder()
                .success(true)
                .message("Exam score retrieved successfully")
                .data(response)
                .build());
    }

    @PostMapping
    @Operation(summary = "Add a new exam score", description = "Registers a new score (MHT_CET, JEE_MAIN, etc.) ensuring no duplicate exam-year combinations.")
    public ResponseEntity<ApiResponse<ExamScoreResponse>> addScore(@Valid @RequestBody ExamScoreRequest request) {
        ExamScoreResponse response = examScoreService.addScore(request);
        return new ResponseEntity<>(ApiResponse.<ExamScoreResponse>builder()
                .success(true)
                .message("Exam score registered successfully")
                .data(response)
                .build(), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update specific details of an exam score", description = "Updates details of the exam score matching the ID parameter.")
    public ResponseEntity<ApiResponse<ExamScoreResponse>> updateScore(@PathVariable UUID id, @Valid @RequestBody ExamScoreRequest request) {
        ExamScoreResponse response = examScoreService.updateScore(id, request);
        return ResponseEntity.ok(ApiResponse.<ExamScoreResponse>builder()
                .success(true)
                .message("Exam score updated successfully")
                .data(response)
                .build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an exam score from database", description = "Deletes the exam score matching the ID parameter.")
    public ResponseEntity<ApiResponse<Void>> deleteScore(@PathVariable UUID id) {
        examScoreService.deleteScore(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Exam score deleted successfully")
                .build());
    }
}
