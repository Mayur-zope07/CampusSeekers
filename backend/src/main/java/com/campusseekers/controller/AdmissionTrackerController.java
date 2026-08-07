package com.campusseekers.controller;

import com.campusseekers.dto.ApiResponse;
import com.campusseekers.dto.AdmissionTrackerRequest;
import com.campusseekers.dto.AdmissionTrackerResponse;
import com.campusseekers.dto.AdmissionTrackerHistoryResponse;
import com.campusseekers.service.AdmissionTrackerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admission-tracker")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admission Tracker", description = "Endpoints for tracking status and viewing history of shortlisted college applications")
public class AdmissionTrackerController {

    private final AdmissionTrackerService admissionTrackerService;

    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get all admission trackers", description = "Retrieves current status for all active shortlisted branch applications.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Admission trackers retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<List<AdmissionTrackerResponse>>> getAdmissionTrackers() {
        List<AdmissionTrackerResponse> response = admissionTrackerService.getAdmissionTrackers();
        return ResponseEntity.ok(ApiResponse.<List<AdmissionTrackerResponse>>builder()
                .success(true)
                .message("Admission trackers retrieved successfully")
                .data(response)
                .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Update admission status", description = "Updates status and appends remark log to the admission application timeline.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Admission status updated successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid payload or validation failures"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Admission tracker not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Unprocessable Entity - Invalid status state transition")
    })
    public ResponseEntity<ApiResponse<AdmissionTrackerResponse>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody AdmissionTrackerRequest request) {
        AdmissionTrackerResponse response = admissionTrackerService.updateStatus(id, request);
        return ResponseEntity.ok(ApiResponse.<AdmissionTrackerResponse>builder()
                .success(true)
                .message("Admission status updated successfully")
                .data(response)
                .build());
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get timeline history of an application", description = "Retrieves the chronological timeline logs of status updates for an application.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Timeline history retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Admission tracker not found")
    })
    public ResponseEntity<ApiResponse<List<AdmissionTrackerHistoryResponse>>> getTimelineHistory(
            @PathVariable UUID id) {
        List<AdmissionTrackerHistoryResponse> response = admissionTrackerService.getHistory(id);
        return ResponseEntity.ok(ApiResponse.<List<AdmissionTrackerHistoryResponse>>builder()
                .success(true)
                .message("Timeline history retrieved successfully")
                .data(response)
                .build());
    }
}
