package com.campusseekers.controller;

import com.campusseekers.dto.ApiResponse;
import com.campusseekers.dto.CutoffRequest;
import com.campusseekers.dto.CutoffResponse;
import com.campusseekers.dto.CutoffSearchResponse;
import com.campusseekers.dto.PageResponse;
import com.campusseekers.entity.Category;
import com.campusseekers.entity.ExamName;
import com.campusseekers.service.CutoffSearchService;
import com.campusseekers.service.CutoffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Cutoffs", description = "Endpoints for managing entrance cutoff details")
public class CutoffController {

    private final CutoffService cutoffService;
    private final CutoffSearchService cutoffSearchService;

    private Pageable createPageable(int page, int size, String sort, String direction) {
        if (page < 0) {
            throw new IllegalArgumentException("Page index must not be less than zero");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be greater than zero");
        }

        List<Sort.Order> orders = new ArrayList<>();
        if (sort != null && !sort.isBlank()) {
            String[] sortProperties = sort.split(",");
            String[] sortDirections = direction != null ? direction.split(",") : new String[0];
            for (int i = 0; i < sortProperties.length; i++) {
                String prop = sortProperties[i].trim();
                Sort.Direction dir = Sort.Direction.ASC;
                if (i < sortDirections.length) {
                    String d = sortDirections[i].trim().toUpperCase();
                    if ("DESC".equals(d)) {
                        dir = Sort.Direction.DESC;
                    }
                } else if (sortDirections.length > 0) {
                    String d = sortDirections[0].trim().toUpperCase();
                    if ("DESC".equals(d)) {
                        dir = Sort.Direction.DESC;
                    }
                }
                orders.add(new Sort.Order(dir, prop));
            }
        }
        Sort finalSort = orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
        return PageRequest.of(page, size, finalSort);
    }

    @GetMapping("/api/cutoffs")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "Search cutoffs", description = "Search cutoffs matching dynamic criteria combinations")
    public ResponseEntity<ApiResponse<PageResponse<CutoffSearchResponse>>> getCutoffs(
            @RequestParam(required = false) ExamName exam,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer round,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) String rawSeatType,
            @RequestParam(required = false) String college,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) Integer minRank,
            @RequestParam(required = false) Integer maxRank,
            @RequestParam(required = false) BigDecimal minPercentile,
            @RequestParam(required = false) BigDecimal maxPercentile,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String direction) {

        Pageable pageable = createPageable(page, size, sort, direction);
        PageResponse<CutoffSearchResponse> response = cutoffSearchService.searchCutoffs(exam, year, round, category, rawSeatType, college, branch, minRank, maxRank, minPercentile, maxPercentile, pageable);
        return ResponseEntity.ok(ApiResponse.<PageResponse<CutoffSearchResponse>>builder()
                .success(true)
                .message("Cutoffs retrieved successfully.")
                .data(response)
                .build());
    }

    @GetMapping("/api/cutoffs/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "Get cutoff record by ID", description = "Retrieves a cutoff record by UUID. Accessible by students and admins.")
    public ResponseEntity<ApiResponse<CutoffResponse>> getCutoffById(
            @Parameter(description = "The UUID of the cutoff record") @PathVariable UUID id) {
        CutoffResponse response = cutoffService.getCutoffById(id);
        return ResponseEntity.ok(ApiResponse.<CutoffResponse>builder()
                .success(true)
                .message("Cutoff record retrieved successfully")
                .data(response)
                .build());
    }

    @PostMapping("/api/admin/cutoffs")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new cutoff record", description = "Registers entrance cutoff details. Restricted to ADMIN users only.")
    public ResponseEntity<ApiResponse<CutoffResponse>> createCutoff(
            @Valid @RequestBody CutoffRequest request) {
        CutoffResponse response = cutoffService.createCutoff(request);
        return new ResponseEntity<>(ApiResponse.<CutoffResponse>builder()
                .success(true)
                .message("Cutoff record created successfully")
                .data(response)
                .build(), HttpStatus.CREATED);
    }

    @PutMapping("/api/admin/cutoffs/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update cutoff details", description = "Modifies existing cutoff details. Restricted to ADMIN users only.")
    public ResponseEntity<ApiResponse<CutoffResponse>> updateCutoff(
            @Parameter(description = "The UUID of the cutoff record to update") @PathVariable UUID id,
            @Valid @RequestBody CutoffRequest request) {
        CutoffResponse response = cutoffService.updateCutoff(id, request);
        return ResponseEntity.ok(ApiResponse.<CutoffResponse>builder()
                .success(true)
                .message("Cutoff record updated successfully")
                .data(response)
                .build());
    }

    @DeleteMapping("/api/admin/cutoffs/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete cutoff record", description = "Deletes cutoff statistics record. Restricted to ADMIN users only.")
    public ResponseEntity<ApiResponse<Void>> deleteCutoff(
            @Parameter(description = "The UUID of the cutoff record to delete") @PathVariable UUID id) {
        cutoffService.deleteCutoff(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Cutoff record deleted successfully")
                .build());
    }
}
