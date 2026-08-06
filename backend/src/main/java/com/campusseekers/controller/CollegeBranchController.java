package com.campusseekers.controller;

import com.campusseekers.dto.ApiResponse;
import com.campusseekers.dto.CollegeBranchRequest;
import com.campusseekers.dto.CollegeBranchResponse;
import com.campusseekers.dto.PageResponse;
import com.campusseekers.service.BranchSearchService;
import com.campusseekers.service.CollegeBranchService;
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
@Tag(name = "College Branches Mappings", description = "Endpoints for managing college-branch mappings")
public class CollegeBranchController {

    private final CollegeBranchService collegeBranchService;
    private final BranchSearchService branchSearchService;

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

    @GetMapping("/api/college-branches")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "Search college branches", description = "Search college branch associations with fees and capacity parameters")
    public ResponseEntity<ApiResponse<PageResponse<CollegeBranchResponse>>> getCollegeBranches(
            @RequestParam(required = false) String college,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) BigDecimal minFees,
            @RequestParam(required = false) BigDecimal maxFees,
            @RequestParam(required = false) Integer minIntake,
            @RequestParam(required = false) Integer maxIntake,
            @RequestParam(required = false) Integer duration,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String direction) {

        Pageable pageable = createPageable(page, size, sort, direction);
        PageResponse<CollegeBranchResponse> response = branchSearchService.searchCollegeBranches(college, branch, minFees, maxFees, minIntake, maxIntake, duration, pageable);
        return ResponseEntity.ok(ApiResponse.<PageResponse<CollegeBranchResponse>>builder()
                .success(true)
                .message("College branches retrieved successfully.")
                .data(response)
                .build());
    }

    @GetMapping("/api/college-branches/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "Get mapping details by ID", description = "Retrieves a mapping record by UUID. Accessible by students and admins.")
    public ResponseEntity<ApiResponse<CollegeBranchResponse>> getCollegeBranchById(
            @Parameter(description = "The UUID of the mapping record") @PathVariable UUID id) {
        CollegeBranchResponse response = collegeBranchService.getCollegeBranchById(id);
        return ResponseEntity.ok(ApiResponse.<CollegeBranchResponse>builder()
                .success(true)
                .message("College branch mapping retrieved successfully")
                .data(response)
                .build());
    }

    @PostMapping("/api/admin/college-branches")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create college-branch mapping", description = "Associates an academic branch with a college. Restricted to ADMIN users only.")
    public ResponseEntity<ApiResponse<CollegeBranchResponse>> createCollegeBranch(
            @Valid @RequestBody CollegeBranchRequest request) {
        CollegeBranchResponse response = collegeBranchService.createCollegeBranch(request);
        return new ResponseEntity<>(ApiResponse.<CollegeBranchResponse>builder()
                .success(true)
                .message("College branch mapping created successfully")
                .data(response)
                .build(), HttpStatus.CREATED);
    }

    @PutMapping("/api/admin/college-branches/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update mapping details", description = "Modifies mapping attributes (fees, intake, duration). Restricted to ADMIN users only.")
    public ResponseEntity<ApiResponse<CollegeBranchResponse>> updateCollegeBranch(
            @Parameter(description = "The UUID of the mapping record to update") @PathVariable UUID id,
            @Valid @RequestBody CollegeBranchRequest request) {
        CollegeBranchResponse response = collegeBranchService.updateCollegeBranch(id, request);
        return ResponseEntity.ok(ApiResponse.<CollegeBranchResponse>builder()
                .success(true)
                .message("College branch mapping updated successfully")
                .data(response)
                .build());
    }

    @DeleteMapping("/api/admin/college-branches/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete college-branch mapping", description = "Deletes mapping record. Restricted to ADMIN users only.")
    public ResponseEntity<ApiResponse<Void>> deleteCollegeBranch(
            @Parameter(description = "The UUID of the mapping record to delete") @PathVariable UUID id) {
        collegeBranchService.deleteCollegeBranch(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("College branch mapping deleted successfully")
                .build());
    }
}
