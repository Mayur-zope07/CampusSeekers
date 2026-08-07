package com.campusseekers.controller;

import com.campusseekers.dto.ApiResponse;
import com.campusseekers.dto.PageResponse;
import com.campusseekers.dto.ShortlistRequest;
import com.campusseekers.dto.ShortlistResponse;
import com.campusseekers.entity.AdmissionStatus;
import com.campusseekers.entity.RecommendationCategory;
import com.campusseekers.service.ShortlistService;
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

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Shortlist Management", description = "Endpoints for managing student shortlisted college branches")
public class ShortlistController {

    private final ShortlistService shortlistService;

    @PostMapping("/api/shortlists")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Add college branch to shortlist", description = "Shortlists a college branch for the student and auto-initializes the admission tracker.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Shortlisted successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid payload"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Branch already shortlisted")
    })
    public ResponseEntity<ApiResponse<ShortlistResponse>> addToShortlist(@Valid @RequestBody ShortlistRequest request) {
        ShortlistResponse response = shortlistService.addToShortlist(request);
        return new ResponseEntity<>(ApiResponse.<ShortlistResponse>builder()
                .success(true)
                .message("Shortlisted successfully")
                .data(response)
                .build(), HttpStatus.CREATED);
    }

    @GetMapping("/api/shortlists")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Search and list shortlist", description = "Retrieves a paginated list of student's active shortlists filtered by criteria.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Shortlist retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<PageResponse<ShortlistResponse>>> getShortlist(
            @RequestParam(required = false) String collegeKeyword,
            @RequestParam(required = false) String branchKeyword,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String naac,
            @RequestParam(required = false) BigDecimal maxFees,
            @RequestParam(required = false) Integer priority,
            @RequestParam(required = false) AdmissionStatus status,
            @RequestParam(required = false) RecommendationCategory recCategory,
            Pageable pageable) {
        Page<ShortlistResponse> page = shortlistService.searchShortlist(
                collegeKeyword, branchKeyword, city, state, naac, maxFees, priority, status, recCategory, pageable
        );
        return ResponseEntity.ok(ApiResponse.<PageResponse<ShortlistResponse>>builder()
                .success(true)
                .message("Shortlist retrieved successfully")
                .data(PageResponse.from(page))
                .build());
    }

    @PutMapping("/api/shortlists/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Update shortlist notes/priority", description = "Modifies priority ranking and personal notes for a shortlisted branch.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Shortlist updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Shortlist entry not found")
    })
    public ResponseEntity<ApiResponse<ShortlistResponse>> updateShortlist(
            @PathVariable UUID id,
            @RequestParam(required = false) Integer priority,
            @RequestParam(required = false) String notes) {
        ShortlistResponse response = shortlistService.updateShortlist(id, priority, notes);
        return ResponseEntity.ok(ApiResponse.<ShortlistResponse>builder()
                .success(true)
                .message("Shortlist updated successfully")
                .data(response)
                .build());
    }

    @DeleteMapping("/api/shortlists/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Soft-delete shortlist entry", description = "Soft-deletes a college branch from the student's shortlists.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Shortlist entry deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Shortlist entry not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteShortlist(@PathVariable UUID id) {
        shortlistService.removeFromShortlist(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Shortlist entry deleted successfully")
                .build());
    }

    @PostMapping("/api/recommendations/{recommendationItemId}/shortlist")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Shortlist recommended branch", description = "Directly adds a recommendation engine result branch to the student's shortlist.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Recommendation imported successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recommendation item not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Branch already shortlisted")
    })
    public ResponseEntity<ApiResponse<ShortlistResponse>> importRecommendation(@PathVariable UUID recommendationItemId) {
        ShortlistResponse response = shortlistService.importRecommendationToShortlist(recommendationItemId);
        return new ResponseEntity<>(ApiResponse.<ShortlistResponse>builder()
                .success(true)
                .message("Recommendation imported successfully")
                .data(response)
                .build(), HttpStatus.CREATED);
    }
}
