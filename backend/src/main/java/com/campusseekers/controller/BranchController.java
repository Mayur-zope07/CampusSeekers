package com.campusseekers.controller;

import com.campusseekers.dto.ApiResponse;
import com.campusseekers.dto.BranchRequest;
import com.campusseekers.dto.BranchResponse;
import com.campusseekers.dto.PageResponse;
import com.campusseekers.service.BranchSearchService;
import com.campusseekers.service.BranchService;
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
@Tag(name = "Branches", description = "Endpoints for managing college branches")
public class BranchController {

    private final BranchService branchService;
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

    @GetMapping("/api/branches")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "Search branches", description = "Search branches with pagination and sorting")
    public ResponseEntity<ApiResponse<PageResponse<BranchResponse>>> getBranches(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String direction) {

        Pageable pageable = createPageable(page, size, sort, direction);
        PageResponse<BranchResponse> response = branchSearchService.searchBranches(name, code, keyword, pageable);
        return ResponseEntity.ok(ApiResponse.<PageResponse<BranchResponse>>builder()
                .success(true)
                .message("Branches retrieved successfully.")
                .data(response)
                .build());
    }

    @GetMapping("/api/branches/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "Get branch by ID", description = "Retrieves details of a specific branch. Accessible by students and admins.")
    public ResponseEntity<ApiResponse<BranchResponse>> getBranchById(
            @Parameter(description = "The UUID of the branch to fetch") @PathVariable UUID id) {
        BranchResponse response = branchService.getBranchById(id);
        return ResponseEntity.ok(ApiResponse.<BranchResponse>builder()
                .success(true)
                .message("Branch retrieved successfully")
                .data(response)
                .build());
    }

    @PostMapping("/api/admin/branches")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new branch record", description = "Registers a new academic branch. Restricted to ADMIN users only.")
    public ResponseEntity<ApiResponse<BranchResponse>> createBranch(
            @Valid @RequestBody BranchRequest request) {
        BranchResponse response = branchService.createBranch(request);
        return new ResponseEntity<>(ApiResponse.<BranchResponse>builder()
                .success(true)
                .message("Branch created successfully")
                .data(response)
                .build(), HttpStatus.CREATED);
    }

    @PutMapping("/api/admin/branches/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update branch details", description = "Modifies branch name or code. Restricted to ADMIN users only.")
    public ResponseEntity<ApiResponse<BranchResponse>> updateBranch(
            @Parameter(description = "The UUID of the branch to update") @PathVariable UUID id,
            @Valid @RequestBody BranchRequest request) {
        BranchResponse response = branchService.updateBranch(id, request);
        return ResponseEntity.ok(ApiResponse.<BranchResponse>builder()
                .success(true)
                .message("Branch updated successfully")
                .data(response)
                .build());
    }

    @DeleteMapping("/api/admin/branches/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete branch record", description = "Deletes a branch record. Restricted to ADMIN users only.")
    public ResponseEntity<ApiResponse<Void>> deleteBranch(
            @Parameter(description = "The UUID of the branch to delete") @PathVariable UUID id) {
        branchService.deleteBranch(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Branch deleted successfully")
                .build());
    }
}
