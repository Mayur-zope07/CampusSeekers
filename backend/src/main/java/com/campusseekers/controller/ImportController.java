package com.campusseekers.controller;

import com.campusseekers.dto.ApiResponse;
import com.campusseekers.dto.ImportSummaryResponse;
import com.campusseekers.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@Slf4j
@RestController
@RequestMapping("/api/admin/import")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ImportController {

    private final BulkImportOrchestratorService orchestratorService;
    private final CollegeImportService collegeImportService;
    private final BranchImportService branchImportService;
    private final CollegeBranchImportService collegeBranchImportService;
    private final CutoffImportService cutoffImportService;
    private final SeatMatrixImportService seatMatrixImportService;

    @Value("${import.batch-size:500}")
    private int batchSize;

    @Value("${import.dry-run-default:false}")
    private boolean dryRunDefault;

    @Value("${import.max-file-size:10485760}")
    private long maxFileSize;

    @Value("${import.paths.colleges}")
    private String collegesPath;

    @Value("${import.paths.branches}")
    private String branchesPath;

    @Value("${import.paths.college-branches}")
    private String collegeBranchesPath;

    @Value("${import.paths.cutoffs}")
    private String cutoffsPath;

    @Value("${import.paths.seat-matrix}")
    private String seatMatrixPath;

    @PostMapping("/all")
    public ResponseEntity<ApiResponse<ImportSummaryResponse>> importAll(
            @RequestParam(required = false, defaultValue = "false") boolean replaceExisting,
            @RequestParam(required = false) Boolean dryRun) {
        
        ImportSummaryResponse response = orchestratorService.importAll(replaceExisting, dryRun);
        if ("FAILED".equals(response.getStatus())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<ImportSummaryResponse>builder()
                            .success(false)
                            .message("Bulk import pre-validation failed.")
                            .data(response)
                            .build());
        }
        return ResponseEntity.ok(ApiResponse.<ImportSummaryResponse>builder()
                .success(true)
                .message("Bulk import completed successfully.")
                .data(response)
                .build());
    }

    @PostMapping("/colleges")
    public ResponseEntity<ApiResponse<ImportSummaryResponse>> importColleges(
            @RequestParam(required = false, defaultValue = "false") boolean replaceExisting,
            @RequestParam(required = false) Boolean dryRun) {
        boolean isDry = dryRun != null ? dryRun : dryRunDefault;
        ImportSummaryResponse response = collegeImportService.importCsv(collegesPath, replaceExisting, isDry, maxFileSize, batchSize);
        return ResponseEntity.ok(ApiResponse.<ImportSummaryResponse>builder()
                .success(true)
                .message("Colleges import completed successfully.")
                .data(response)
                .build());
    }

    @PostMapping("/branches")
    public ResponseEntity<ApiResponse<ImportSummaryResponse>> importBranches(
            @RequestParam(required = false, defaultValue = "false") boolean replaceExisting,
            @RequestParam(required = false) Boolean dryRun) {
        boolean isDry = dryRun != null ? dryRun : dryRunDefault;
        ImportSummaryResponse response = branchImportService.importCsv(branchesPath, replaceExisting, isDry, maxFileSize, batchSize);
        return ResponseEntity.ok(ApiResponse.<ImportSummaryResponse>builder()
                .success(true)
                .message("Branches import completed successfully.")
                .data(response)
                .build());
    }

    @PostMapping("/college-branches")
    public ResponseEntity<ApiResponse<ImportSummaryResponse>> importCollegeBranches(
            @RequestParam(required = false, defaultValue = "false") boolean replaceExisting,
            @RequestParam(required = false) Boolean dryRun) {
        boolean isDry = dryRun != null ? dryRun : dryRunDefault;
        ImportSummaryResponse response = collegeBranchImportService.importCsv(collegeBranchesPath, replaceExisting, isDry, maxFileSize, batchSize,
                Collections.emptySet(), Collections.emptySet(), Collections.emptySet());
        return ResponseEntity.ok(ApiResponse.<ImportSummaryResponse>builder()
                .success(true)
                .message("College branches import completed successfully.")
                .data(response)
                .build());
    }

    @PostMapping("/cutoffs")
    public ResponseEntity<ApiResponse<ImportSummaryResponse>> importCutoffs(
            @RequestParam(required = false, defaultValue = "false") boolean replaceExisting,
            @RequestParam(required = false) Boolean dryRun) {
        boolean isDry = dryRun != null ? dryRun : dryRunDefault;
        ImportSummaryResponse response = cutoffImportService.importCsv(cutoffsPath, replaceExisting, isDry, maxFileSize, batchSize,
                Collections.emptySet());
        return ResponseEntity.ok(ApiResponse.<ImportSummaryResponse>builder()
                .success(true)
                .message("Cutoffs import completed successfully.")
                .data(response)
                .build());
    }

    @PostMapping("/seat-matrix")
    public ResponseEntity<ApiResponse<ImportSummaryResponse>> importSeatMatrix(
            @RequestParam(required = false, defaultValue = "false") boolean replaceExisting,
            @RequestParam(required = false) Boolean dryRun) {
        boolean isDry = dryRun != null ? dryRun : dryRunDefault;
        ImportSummaryResponse response = seatMatrixImportService.importCsv(seatMatrixPath, replaceExisting, isDry, maxFileSize, batchSize,
                Collections.emptySet());
        return ResponseEntity.ok(ApiResponse.<ImportSummaryResponse>builder()
                .success(true)
                .message("Seat matrix import completed successfully.")
                .data(response)
                .build());
    }
}
