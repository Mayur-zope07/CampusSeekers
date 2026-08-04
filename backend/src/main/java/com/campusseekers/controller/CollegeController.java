package com.campusseekers.controller;

import com.campusseekers.dto.ApiResponse;
import com.campusseekers.dto.CollegeRequest;
import com.campusseekers.dto.CollegeResponse;
import com.campusseekers.service.CollegeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Colleges", description = "Endpoints for managing colleges details")
public class CollegeController {

    private final CollegeService collegeService;

    @GetMapping("/api/colleges")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "Get list of all colleges", description = "Returns a list of all active colleges. Accessible by students and admins.")
    public ResponseEntity<com.campusseekers.dto.ApiResponse<List<CollegeResponse>>> getAllColleges() {
        List<CollegeResponse> response = collegeService.getAllColleges();
        return ResponseEntity.ok(com.campusseekers.dto.ApiResponse.<List<CollegeResponse>>builder()
                .success(true)
                .message("Colleges retrieved successfully")
                .data(response)
                .build());
    }

    @GetMapping("/api/colleges/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "Get college by ID", description = "Retrieves complete details of a specific college. Accessible by students and admins.")
    public ResponseEntity<com.campusseekers.dto.ApiResponse<CollegeResponse>> getCollegeById(
            @Parameter(description = "The UUID of the college to fetch") @PathVariable UUID id) {
        CollegeResponse response = collegeService.getCollegeById(id);
        return ResponseEntity.ok(com.campusseekers.dto.ApiResponse.<CollegeResponse>builder()
                .success(true)
                .message("College retrieved successfully")
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
