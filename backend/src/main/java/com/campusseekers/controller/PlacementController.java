package com.campusseekers.controller;

import com.campusseekers.dto.ApiResponse;
import com.campusseekers.dto.PlacementRequest;
import com.campusseekers.dto.PlacementResponse;
import com.campusseekers.service.PlacementService;
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
@Tag(name = "Placements", description = "Endpoints for managing college placement statistics")
public class PlacementController {

    private final PlacementService placementService;

    @GetMapping("/api/placements")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "Get list of all placements", description = "Returns a list of all placement records. Accessible by students and admins.")
    public ResponseEntity<ApiResponse<List<PlacementResponse>>> getAllPlacements() {
        List<PlacementResponse> response = placementService.getAllPlacements();
        return ResponseEntity.ok(ApiResponse.<List<PlacementResponse>>builder()
                .success(true)
                .message("Placement records retrieved successfully")
                .data(response)
                .build());
    }

    @GetMapping("/api/placements/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "Get placement record by ID", description = "Retrieves a placement record by UUID. Accessible by students and admins.")
    public ResponseEntity<ApiResponse<PlacementResponse>> getPlacementById(
            @Parameter(description = "The UUID of the placement record") @PathVariable UUID id) {
        PlacementResponse response = placementService.getPlacementById(id);
        return ResponseEntity.ok(ApiResponse.<PlacementResponse>builder()
                .success(true)
                .message("Placement record retrieved successfully")
                .data(response)
                .build());
    }

    @PostMapping("/api/admin/placements")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new placement record", description = "Registers a placement record for a college. Restricted to ADMIN users only.")
    public ResponseEntity<ApiResponse<PlacementResponse>> createPlacement(
            @Valid @RequestBody PlacementRequest request) {
        PlacementResponse response = placementService.createPlacement(request);
        return new ResponseEntity<>(ApiResponse.<PlacementResponse>builder()
                .success(true)
                .message("Placement record created successfully")
                .data(response)
                .build(), HttpStatus.CREATED);
    }

    @PutMapping("/api/admin/placements/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update placement details", description = "Modifies packaging numbers or year details. Restricted to ADMIN users only.")
    public ResponseEntity<ApiResponse<PlacementResponse>> updatePlacement(
            @Parameter(description = "The UUID of the placement record to update") @PathVariable UUID id,
            @Valid @RequestBody PlacementRequest request) {
        PlacementResponse response = placementService.updatePlacement(id, request);
        return ResponseEntity.ok(ApiResponse.<PlacementResponse>builder()
                .success(true)
                .message("Placement record updated successfully")
                .data(response)
                .build());
    }

    @DeleteMapping("/api/admin/placements/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete placement record", description = "Deletes placement statistics record. Restricted to ADMIN users only.")
    public ResponseEntity<ApiResponse<Void>> deletePlacement(
            @Parameter(description = "The UUID of the placement record to delete") @PathVariable UUID id) {
        placementService.deletePlacement(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Placement record deleted successfully")
                .build());
    }
}
