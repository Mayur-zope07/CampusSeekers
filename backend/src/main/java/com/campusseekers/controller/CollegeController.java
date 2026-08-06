package com.campusseekers.controller;

import com.campusseekers.dto.*;
import com.campusseekers.entity.CollegeStatus;
import com.campusseekers.entity.CollegeType;
import com.campusseekers.service.CollegeSearchService;
import com.campusseekers.service.CollegeService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Colleges", description = "Endpoints for managing colleges details")
public class CollegeController {

    private final CollegeService collegeService;
    private final CollegeSearchService collegeSearchService;

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

    @GetMapping("/api/colleges")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "Search colleges", description = "Search and filter colleges with pagination and sorting")
    public ResponseEntity<ApiResponse<PageResponse<CollegeListResponse>>> getColleges(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) CollegeType type,
            @RequestParam(required = false) String naacGrade,
            @RequestParam(required = false) Boolean nba,
            @RequestParam(required = false) CollegeStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String direction) {

        Pageable pageable = createPageable(page, size, sort, direction);
        PageResponse<CollegeListResponse> response = collegeSearchService.searchColleges(name, code, city, state, type, naacGrade, nba, status, keyword, pageable);
        return ResponseEntity.ok(ApiResponse.<PageResponse<CollegeListResponse>>builder()
                .success(true)
                .message("Colleges retrieved successfully.")
                .data(response)
                .build());
    }

    @GetMapping("/api/colleges/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "Get college details by ID", description = "Retrieves complete details of a specific college. Accessible by students and admins.")
    public ResponseEntity<ApiResponse<CollegeDetailsResponse>> getCollegeById(
            @Parameter(description = "The UUID of the college to fetch") @PathVariable UUID id) {
        CollegeDetailsResponse response = collegeSearchService.getCollegeDetails(id);
        return ResponseEntity.ok(ApiResponse.<CollegeDetailsResponse>builder()
                .success(true)
                .message("College details retrieved successfully")
                .data(response)
                .build());
    }

    @PostMapping("/api/admin/colleges")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new college record", description = "Registers a new college. Restricted to ADMIN users only.")
    public ResponseEntity<com.campusseekers.dto.ApiResponse<CollegeResponse>> createCollege(
            @Valid @RequestBody CollegeRequest request) {
        CollegeResponse response = collegeService.createCollege(request);
        return new ResponseEntity<>(com.campusseekers.dto.ApiResponse.<CollegeResponse>builder()
                .success(true)
                .message("College created successfully")
                .data(response)
                .build(), HttpStatus.CREATED);
    }

    @PutMapping("/api/admin/colleges/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update college details", description = "Modifies existing details of a college. Restricted to ADMIN users only.")
    public ResponseEntity<com.campusseekers.dto.ApiResponse<CollegeResponse>> updateCollege(
            @Parameter(description = "The UUID of the college to update") @PathVariable UUID id,
            @Valid @RequestBody CollegeRequest request) {
        CollegeResponse response = collegeService.updateCollege(id, request);
        return ResponseEntity.ok(com.campusseekers.dto.ApiResponse.<CollegeResponse>builder()
                .success(true)
                .message("College updated successfully")
                .data(response)
                .build());
    }

    @DeleteMapping("/api/admin/colleges/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete college record", description = "Deletes a college record from the database. Restricted to ADMIN users only.")
    public ResponseEntity<com.campusseekers.dto.ApiResponse<Void>> deleteCollege(
            @Parameter(description = "The UUID of the college to delete") @PathVariable UUID id) {
        collegeService.deleteCollege(id);
        return ResponseEntity.ok(com.campusseekers.dto.ApiResponse.<Void>builder()
                .success(true)
                .message("College deleted successfully")
                .build());
    }
}
