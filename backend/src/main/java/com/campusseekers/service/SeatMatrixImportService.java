package com.campusseekers.service;

import com.campusseekers.dto.ImportSummaryResponse;
import com.campusseekers.dto.ImportSummaryResponse.DatasetStats;
import com.campusseekers.dto.SeatMatrixImportDto;
import com.campusseekers.entity.CollegeBranch;
import com.campusseekers.mapper.SeatMatrixImportMapper;
import com.campusseekers.repository.CollegeBranchRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatMatrixImportService {

    private final CsvImportService csvImportService;
    private final CollegeBranchRepository collegeBranchRepository;
    private final SeatMatrixImportMapper seatMatrixMapper;

    @PersistenceContext
    private EntityManager entityManager;

    private static final String[] HEADERS = {"college_code", "branch_code", "intake_capacity"};

    @Transactional
    public ImportSummaryResponse importCsv(String pathStr, boolean replaceExisting, boolean dryRun, long maxFileSize, int batchSize,
                                           Set<String> contextualCollegeBranches) {
        log.info("Starting Seat Matrix import. File: {}, replaceExisting: {}, dryRun: {}", pathStr, replaceExisting, dryRun);
        long startTime = System.currentTimeMillis();

        int processed = 0;
        int updated = 0;
        int skipped = 0;
        int duplicate = 0;
        List<String> errors = new ArrayList<>();

        // Load all mapping associations
        List<CollegeBranch> cbList = collegeBranchRepository.findAll();
        Map<String, CollegeBranch> cbMap = new HashMap<>();
        for (CollegeBranch cb : cbList) {
            String key = cb.getCollege().getCollegeCode() + "-" + cb.getBranch().getBranchCode();
            cbMap.put(key, cb);
        }

        try (CSVParser parser = csvImportService.parseCsv(pathStr, HEADERS, maxFileSize)) {
            Set<String> csvKeys = new HashSet<>();
            List<SeatMatrixImportDto> dtos = new ArrayList<>();

            // 1. Parse and validate CSV
            for (CSVRecord record : parser) {
                processed++;
                String collegeCode = record.get("college_code");
                String branchCode = record.get("branch_code");

                if (collegeCode == null || collegeCode.isBlank()) {
                    errors.add("Line " + (record.getRecordNumber() + 1) + ": College code is blank");
                    continue;
                }
                if (branchCode == null || branchCode.isBlank()) {
                    errors.add("Line " + (record.getRecordNumber() + 1) + ": Branch code is blank");
                    continue;
                }

                collegeCode = collegeCode.trim();
                branchCode = branchCode.trim();
                String cbKey = collegeCode + "-" + branchCode;

                // Validate referential integrity
                boolean cbExists = cbMap.containsKey(cbKey) || (contextualCollegeBranches != null && contextualCollegeBranches.contains(cbKey));
                if (!cbExists) {
                    throw new IllegalArgumentException("Referential Integrity Failure: CollegeBranch mapping " + cbKey + " not found in database or current import context");
                }

                // Check internal CSV duplicates
                if (!csvKeys.add(cbKey)) {
                    throw new IllegalArgumentException("Duplicate seat matrix mapping inside CSV: " + cbKey);
                }

                SeatMatrixImportDto dto = SeatMatrixImportDto.builder()
                        .collegeCode(collegeCode)
                        .branchCode(branchCode)
                        .intakeCapacity(record.get("intake_capacity"))
                        .build();
                dtos.add(dto);
            }

            if (!errors.isEmpty()) {
                throw new IllegalArgumentException("CSV validation errors: " + errors);
            }

            // 2. Process entities
            List<CollegeBranch> batch = new ArrayList<>();
            for (SeatMatrixImportDto dto : dtos) {
                String cbKey = dto.getCollegeCode() + "-" + dto.getBranchCode();
                CollegeBranch cb = cbMap.get(cbKey);

                boolean alreadySet = false;
                if (!replaceExisting && cb != null && cb.getIntakeCapacity() != null && cb.getIntakeCapacity() > 0) {
                    alreadySet = true;
                }

                if (alreadySet) {
                    duplicate++;
                    skipped++;
                    continue;
                }

                updated++;

                if (!dryRun) {
                    if (cb == null) {
                        // Lookup if newly created in current transaction
                        final String finalCbKey = cbKey;
                        cb = collegeBranchRepository.findAll().stream()
                                .filter(item -> (item.getCollege().getCollegeCode() + "-" + item.getBranch().getBranchCode()).equals(finalCbKey))
                                .findFirst()
                                .orElseThrow(() -> new IllegalArgumentException("CollegeBranch not found: " + finalCbKey));
                        cbMap.put(cbKey, cb);
                    }

                    seatMatrixMapper.updateEntity(dto, cb);
                    batch.add(cb);
                    if (batch.size() >= batchSize) {
                        collegeBranchRepository.saveAll(batch);
                        if (entityManager != null) {
                            entityManager.flush();
                            entityManager.clear();
                        }
                        log.info("Updated batch of seat capacities. Progress: {}/{}", updated, dtos.size());
                        batch.clear();
                    }
                }
            }

            if (!dryRun && !batch.isEmpty()) {
                collegeBranchRepository.saveAll(batch);
                if (entityManager != null) {
                    entityManager.flush();
                    entityManager.clear();
                }
                log.info("Saved final batch of seat capacities. Total updated: {}", updated);
            }

        } catch (IOException e) {
            log.error("Failed to read seat matrix CSV", e);
            throw new RuntimeException("Failed to read seat matrix CSV file", e);
        }

        long duration = System.currentTimeMillis() - startTime;
        DatasetStats stats = DatasetStats.builder()
                .processed(processed)
                .inserted(0)
                .updated(updated)
                .skipped(skipped)
                .duplicate(duplicate)
                .build();

        log.info("Seat Matrix import finished. Processed: {}, Updated: {}, Skipped: {}, Time: {} ms",
                processed, updated, skipped, duration);

        return ImportSummaryResponse.builder()
                .status("SUCCESS")
                .executionTime((duration / 1000.0) + " seconds")
                .datasetsImported(1)
                .rowsProcessed(processed)
                .rowsInserted(0)
                .rowsUpdated(updated)
                .rowsSkipped(skipped)
                .duplicateRows(duplicate)
                .datasetDetails(Map.of("Seat Matrix", stats))
                .build();
    }
}
