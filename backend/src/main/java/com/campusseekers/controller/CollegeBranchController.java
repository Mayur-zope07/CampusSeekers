package com.campusseekers.controller;

import com.campusseekers.dto.ApiResponse;
import com.campusseekers.dto.CollegeBranchRequest;
import com.campusseekers.dto.CollegeBranchResponse;
import com.campusseekers.service.CollegeBranchService;
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
@Tag(name = "College Branches Mappings", description = "Endpoints for managing college-branch mappings")
public class CollegeBranchController {

    private final CollegeBranchService collegeBranchService;

    @GetMapping("/api/college-branches")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "Get all college-branch mappings", description = "Returns all college-branch mappings. Accessible by students and admins.")
    public ResponseEntity<ApiResponse<List<CollegeBranchResponse>>> getAllCollegeBranches() {
        List<CollegeBranchResponse> response = collegeBranchService.getAllCollegeBranches();
        return ResponseEntity.ok(ApiResponse.<List<CollegeBranchResponse>>builder()
                .success(true)
                .message("College branch mappings retrieved successfully")
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
