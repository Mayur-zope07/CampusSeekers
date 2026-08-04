package com.campusseekers.controller;

import com.campusseekers.dto.ApiResponse;
import com.campusseekers.dto.BranchRequest;
import com.campusseekers.dto.BranchResponse;
import com.campusseekers.service.BranchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Branches", description = "Endpoints for managing college branches")
public class BranchController {

    private final BranchService branchService;

    @GetMapping("/api/branches")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "Get list of all branches", description = "Returns a list of all branches. Accessible by students and admins.")
    public ResponseEntity<ApiResponse<List<BranchResponse>>> getAllBranches() {
        List<BranchResponse> response = branchService.getAllBranches();
        return ResponseEntity.ok(ApiResponse.<List<BranchResponse>>builder()
                .success(true)
                .message("Branches retrieved successfully")
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
