package com.campusseekers.controller;

import com.campusseekers.dto.ApiResponse;
import com.campusseekers.dto.PageResponse;
import com.campusseekers.dto.RecommendationHistoryResponse;
import com.campusseekers.dto.RecommendationRequest;
import com.campusseekers.dto.RecommendationResponse;
import com.campusseekers.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Recommendation Engine", description = "Endpoints for generating, fetching history, and viewing smart college recommendations based on cutoffs")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Generate smart college recommendations", description = "Evaluates historical cutoff data to generate ranked DREAM, TARGET, and SAFE recommendations.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Recommendations generated successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid payload details or constraint validation failure"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Access token missing or invalid"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Caller is not a student")
    })
    public ResponseEntity<ApiResponse<RecommendationResponse>> generateRecommendations(
            @Valid @RequestBody RecommendationRequest request) {
        RecommendationResponse response = recommendationService.generateRecommendations(request);
        return new ResponseEntity<>(ApiResponse.<RecommendationResponse>builder()
                .success(true)
                .message("Recommendations generated successfully")
                .data(response)
                .build(), HttpStatus.CREATED);
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Retrieve paginated recommendation history", description = "Retrieves previous recommendation requests of the authenticated student.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "History retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Access token missing or invalid"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Caller is not a student")
    })
    public ResponseEntity<ApiResponse<PageResponse<RecommendationHistoryResponse>>> getRecommendationHistory(
            Pageable pageable) {
        Page<RecommendationHistoryResponse> page = recommendationService.getRecommendationHistory(pageable);
        return ResponseEntity.ok(ApiResponse.<PageResponse<RecommendationHistoryResponse>>builder()
                .success(true)
                .message("Recommendation history retrieved successfully")
                .data(PageResponse.from(page))
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "Retrieve specific recommendation details", description = "Returns details of a previously generated recommendation. Students can only view their own; admins can view all.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Recommendation details retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Access token missing or invalid"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Caller does not own the requested details"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recommendation not found")
    })
    public ResponseEntity<ApiResponse<RecommendationResponse>> getRecommendationDetails(
            @PathVariable UUID id) {
        RecommendationResponse response = recommendationService.getRecommendationDetails(id);
        return ResponseEntity.ok(ApiResponse.<RecommendationResponse>builder()
                .success(true)
                .message("Recommendation details retrieved successfully")
                .data(response)
                .build());
    }
}
