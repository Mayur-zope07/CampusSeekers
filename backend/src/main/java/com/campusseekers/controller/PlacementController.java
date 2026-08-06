package com.campusseekers.controller;

import com.campusseekers.dto.ApiResponse;
import com.campusseekers.dto.PageResponse;
import com.campusseekers.dto.PlacementRequest;
import com.campusseekers.dto.PlacementResponse;
import com.campusseekers.service.PlacementSearchService;
import com.campusseekers.service.PlacementService;
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
@Tag(name = "Placements", description = "Endpoints for managing college placement statistics")
public class PlacementController {

    private final PlacementService placementService;
    private final PlacementSearchService placementSearchService;

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

    @GetMapping("/api/placements")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "Search placements", description = "Search and filter placements records")
    public ResponseEntity<ApiResponse<PageResponse<PlacementResponse>>> getPlacements(
            @RequestParam(required = false) String college,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) BigDecimal minAveragePackage,
            @RequestParam(required = false) BigDecimal minHighestPackage,
            @RequestParam(required = false) BigDecimal minRatio,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String direction) {

        Pageable pageable = createPageable(page, size, sort, direction);
        PageResponse<PlacementResponse> response = placementSearchService.searchPlacements(college, year, minAveragePackage, minHighestPackage, minRatio, pageable);
        return ResponseEntity.ok(ApiResponse.<PageResponse<PlacementResponse>>builder()
                .success(true)
                .message("Placements retrieved successfully.")
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
