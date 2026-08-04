package com.campusseekers.controller;

import com.campusseekers.dto.ApiResponse;
import com.campusseekers.dto.CutoffRequest;
import com.campusseekers.dto.CutoffResponse;
import com.campusseekers.service.CutoffService;
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
@Tag(name = "Cutoffs", description = "Endpoints for managing entrance cutoff details")
public class CutoffController {

    private final CutoffService cutoffService;

    @GetMapping("/api/cutoffs")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "Get list of all cutoff records", description = "Returns all cutoff entries. Accessible by students and admins.")
    public ResponseEntity<ApiResponse<List<CutoffResponse>>> getAllCutoffs() {
        List<CutoffResponse> response = cutoffService.getAllCutoffs();
        return ResponseEntity.ok(ApiResponse.<List<CutoffResponse>>builder()
                .success(true)
                .message("Cutoff records retrieved successfully")
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
