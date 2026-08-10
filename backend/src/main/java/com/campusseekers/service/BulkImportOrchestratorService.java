package com.campusseekers.service;

import com.campusseekers.dto.ImportSummaryResponse;
import com.campusseekers.dto.ImportSummaryResponse.DatasetStats;
import com.campusseekers.repository.BranchRepository;
import com.campusseekers.repository.CollegeBranchRepository;
import com.campusseekers.repository.CollegeRepository;
import com.campusseekers.repository.CutoffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BulkImportOrchestratorService {

    private final CsvImportService csvImportService;
    private final CollegeImportService collegeImportService;
    private final BranchImportService branchImportService;
    private final CollegeBranchImportService collegeBranchImportService;
    private final CutoffImportService cutoffImportService;
    private final SeatMatrixImportService seatMatrixImportService;

    private final CollegeRepository collegeRepository;
    private final BranchRepository branchRepository;
    private final CollegeBranchRepository collegeBranchRepository;
    private final CutoffRepository cutoffRepository;

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

    public ImportSummaryResponse importAll(boolean replaceExisting, Boolean dryRunParam) {
        boolean dryRun = dryRunParam != null ? dryRunParam : dryRunDefault;
        log.info("Starting Bulk Import Orchestrator. replaceExisting: {}, dryRun: {}", replaceExisting, dryRun);
        long startTime = System.currentTimeMillis();

        List<String> validationErrors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // 1. Verify all files exist and are within size bounds
        Map<String, String> paths = new LinkedHashMap<>();
        paths.put("Colleges", collegesPath);
        paths.put("Branches", branchesPath);
        paths.put("College Branches", collegeBranchesPath);
        paths.put("Cutoffs", cutoffsPath);
        paths.put("Seat Matrix", seatMatrixPath);

        for (Map.Entry<String, String> entry : paths.entrySet()) {
            Path p = Paths.get(entry.getValue());
            if (!Files.exists(p)) {
                log.error("CSV file for {} not found at path: {}", entry.getKey(), entry.getValue());
                throw new RuntimeException(new FileNotFoundException("CSV file for " + entry.getKey() + " not found at: " + entry.getValue()));
            }
            try {
                long size = Files.size(p);
                if (size > maxFileSize) {
                    throw new IllegalArgumentException("CSV file for " + entry.getKey() + " size (" + size + " bytes) exceeds maximum limit of " + maxFileSize + " bytes");
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to get file size for " + entry.getKey(), e);
            }
        }

        // 2. Pre-Import Verification (Dry validation in memory to check duplicates and relations)
        Set<String> colleges = new HashSet<>();
        Set<String> branches = new HashSet<>();
        Set<String> collegeBranches = new HashSet<>();
        
        Set<String> collegesInCsv = new HashSet<>();
        Set<String> branchesInCsv = new HashSet<>();
        Set<String> collegeBranchesInCsv = new HashSet<>();

        // Load existing entities into maps to validate relationships against current DB state
        collegeRepository.findAll().forEach(c -> colleges.add(c.getCollegeCode()));
        branchRepository.findAll().forEach(b -> branches.add(b.getBranchCode()));
        collegeBranchRepository.findAll().forEach(cb -> 
            collegeBranches.add(cb.getCollege().getCollegeCode() + "-" + cb.getBranch().getBranchCode())
        );

        try {
            // Validate Colleges CSV
            try (CSVParser parser = csvImportService.parseCsv(collegesPath, new String[]{"college_code"}, maxFileSize)) {
                for (CSVRecord record : parser) {
                    String code = record.get("college_code");
                    if (code != null && !code.isBlank()) {
                        code = code.trim();
                        if (!collegesInCsv.add(code)) {
                            // If replaceExisting is true, duplicates inside the CSV are still errors.
                            // If replaceExisting is false, we skip duplicate keys relative to DB, but duplicates inside CSV are still invalid.
                            throw new IllegalArgumentException("Duplicate college code inside CSV: " + code);
                        }
                        colleges.add(code);
                    }
                }
            }

            // Validate Branches CSV
            try (CSVParser parser = csvImportService.parseCsv(branchesPath, new String[]{"branch_code"}, maxFileSize)) {
                for (CSVRecord record : parser) {
                    String code = record.get("branch_code");
                    if (code != null && !code.isBlank()) {
                        code = code.trim();
                        if (!branchesInCsv.add(code)) {
                            throw new IllegalArgumentException("Duplicate branch code inside CSV: " + code);
                        }
                        branches.add(code);
                    }
                }
            }

            // Validate College Branches CSV
            try (CSVParser parser = csvImportService.parseCsv(collegeBranchesPath, new String[]{"college_code", "branch_code"}, maxFileSize)) {
                for (CSVRecord record : parser) {
                    String col = record.get("college_code").trim();
                    String br = record.get("branch_code").trim();
                    String key = col + "-" + br;

                    if (!colleges.contains(col)) {
                        throw new IllegalArgumentException("Referential Integrity Failure in College Branches: College code " + col + " not found");
                    }
                    if (!branches.contains(br)) {
                        throw new IllegalArgumentException("Referential Integrity Failure in College Branches: Branch code " + br + " not found");
                    }
                    if (!collegeBranchesInCsv.add(key)) {
                        throw new IllegalArgumentException("Duplicate college branch mapping inside CSV: " + key);
                    }
                    collegeBranches.add(key);
                }
            }

            // Validate Cutoffs CSV
            try (CSVParser parser = csvImportService.parseCsv(cutoffsPath, new String[]{"college_code", "branch_code", "exam_name", "year", "round", "category", "raw_seat_type", "stage"}, maxFileSize)) {
                Set<String> cutoffKeys = new HashSet<>();
                for (CSVRecord record : parser) {
                    String col = record.get("college_code").trim();
                    String br = record.get("branch_code").trim();
                    String cbKey = col + "-" + br;

                    if (!collegeBranches.contains(cbKey)) {
                        throw new IllegalArgumentException("Referential Integrity Failure in Cutoffs: CollegeBranch " + cbKey + " not found");
                    }

                    String key = cbKey + "|" + record.get("exam_name") + "|" + record.get("year") + "|" + record.get("round") + "|" + record.get("category") + "|" + record.get("raw_seat_type") + "|" + record.get("stage");
                    if (!cutoffKeys.add(key)) {
                        throw new IllegalArgumentException("Duplicate cutoff record inside CSV: " + key);
                    }
                }
            }

            // Validate Seat Matrix CSV
            try (CSVParser parser = csvImportService.parseCsv(seatMatrixPath, new String[]{"college_code", "branch_code", "intake_capacity"}, maxFileSize)) {
                Set<String> smKeys = new HashSet<>();
                for (CSVRecord record : parser) {
                    String col = record.get("college_code").trim();
                    String br = record.get("branch_code").trim();
                    String cbKey = col + "-" + br;

                    if (!collegeBranches.contains(cbKey)) {
                        throw new IllegalArgumentException("Referential Integrity Failure in Seat Matrix: CollegeBranch " + cbKey + " not found");
                    }
                    if (!smKeys.add(cbKey)) {
                        throw new IllegalArgumentException("Duplicate seat matrix mapping inside CSV: " + cbKey);
                    }
                }
            }

        } catch (Exception e) {
            log.error("Pre-Import validation failed", e);
            validationErrors.add(e.getMessage());
            return ImportSummaryResponse.builder()
                    .status("FAILED")
                    .executionTime("0 seconds")
                    .datasetsImported(0)
                    .validationErrors(validationErrors)
                    .build();
        }

        // 3. Clear database tables in dependency order if replaceExisting=true
        if (replaceExisting && !dryRun) {
            clearDatabaseInDependencyOrder();
        }

        // 4. Run step imports inside distinct transactions
        Set<String> contextualColleges = new HashSet<>();
        Set<String> contextualBranches = new HashSet<>();
        Set<String> contextualCollegeBranches = new HashSet<>();

        ImportSummaryResponse colSummary = collegeImportService.importCsv(collegesPath, replaceExisting, dryRun, maxFileSize, batchSize);
        colSummary.getDatasetDetails().keySet().forEach(key -> 
            contextualColleges.addAll(colleges)
        );

        ImportSummaryResponse brSummary = branchImportService.importCsv(branchesPath, replaceExisting, dryRun, maxFileSize, batchSize);
        brSummary.getDatasetDetails().keySet().forEach(key -> 
            contextualBranches.addAll(branches)
        );

        ImportSummaryResponse cbSummary = collegeBranchImportService.importCsv(collegeBranchesPath, replaceExisting, dryRun, maxFileSize, batchSize,
                contextualColleges, contextualBranches, contextualCollegeBranches);

        ImportSummaryResponse cutSummary = cutoffImportService.importCsv(cutoffsPath, replaceExisting, dryRun, maxFileSize, batchSize,
                contextualCollegeBranches);

        ImportSummaryResponse smSummary = seatMatrixImportService.importCsv(seatMatrixPath, replaceExisting, dryRun, maxFileSize, batchSize,
                contextualCollegeBranches);

        // Compile combined stats
        int datasetsImported = 5;
        int rowsProcessed = colSummary.getRowsProcessed() + brSummary.getRowsProcessed() + cbSummary.getRowsProcessed() + cutSummary.getRowsProcessed() + smSummary.getRowsProcessed();
        int rowsInserted = colSummary.getRowsInserted() + brSummary.getRowsInserted() + cbSummary.getRowsInserted() + cutSummary.getRowsInserted() + smSummary.getRowsInserted();
        int rowsUpdated = colSummary.getRowsUpdated() + brSummary.getRowsUpdated() + cbSummary.getRowsUpdated() + cutSummary.getRowsUpdated() + smSummary.getRowsUpdated();
        int rowsSkipped = colSummary.getRowsSkipped() + brSummary.getRowsSkipped() + cbSummary.getRowsSkipped() + cutSummary.getRowsSkipped() + smSummary.getRowsSkipped();
        int duplicateRows = colSummary.getDuplicateRows() + brSummary.getDuplicateRows() + cbSummary.getDuplicateRows() + cutSummary.getDuplicateRows() + smSummary.getDuplicateRows();

        Map<String, DatasetStats> details = new LinkedHashMap<>();
        details.put("Colleges", colSummary.getDatasetDetails().get("Colleges"));
        details.put("Branches", brSummary.getDatasetDetails().get("Branches"));
        details.put("College Branches", cbSummary.getDatasetDetails().get("College Branches"));
        details.put("Cutoffs", cutSummary.getDatasetDetails().get("Cutoffs"));
        details.put("Seat Matrix", smSummary.getDatasetDetails().get("Seat Matrix"));

        long duration = System.currentTimeMillis() - startTime;
        log.info("Bulk Import Orchestrator completed successfully in {} ms", duration);

        return ImportSummaryResponse.builder()
                .status("SUCCESS")
                .executionTime((duration / 1000.0) + " seconds")
                .datasetsImported(datasetsImported)
                .rowsProcessed(rowsProcessed)
                .rowsInserted(rowsInserted)
                .rowsUpdated(rowsUpdated)
                .rowsSkipped(rowsSkipped)
                .duplicateRows(duplicateRows)
                .validationErrors(validationErrors)
                .warnings(warnings)
                .datasetDetails(details)
                .build();
    }

    @Transactional
    public void clearDatabaseInDependencyOrder() {
        log.info("Deleting existing datasets in dependency order...");
        cutoffRepository.deleteAllInBatch();
        collegeBranchRepository.deleteAllInBatch();
        branchRepository.deleteAllInBatch();
        collegeRepository.deleteAllInBatch();
        log.info("Successfully cleared database tables.");
    }
}
