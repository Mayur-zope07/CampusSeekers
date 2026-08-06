package com.campusseekers.service;

import com.campusseekers.dto.BranchImportDto;
import com.campusseekers.dto.ImportSummaryResponse;
import com.campusseekers.dto.ImportSummaryResponse.DatasetStats;
import com.campusseekers.entity.Branch;
import com.campusseekers.mapper.BranchImportMapper;
import com.campusseekers.repository.BranchRepository;
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
public class BranchImportService {

    private final CsvImportService csvImportService;
    private final BranchRepository branchRepository;
    private final BranchImportMapper branchMapper;

    @PersistenceContext
    private EntityManager entityManager;

    private static final String[] HEADERS = {"branch_code", "name"};

    @Transactional
    public ImportSummaryResponse importCsv(String pathStr, boolean replaceExisting, boolean dryRun, long maxFileSize, int batchSize) {
        log.info("Starting Branch import. File: {}, replaceExisting: {}, dryRun: {}", pathStr, replaceExisting, dryRun);
        long startTime = System.currentTimeMillis();

        int processed = 0;
        int inserted = 0;
        int skipped = 0;
        int duplicate = 0;
        List<String> errors = new ArrayList<>();

        try (CSVParser parser = csvImportService.parseCsv(pathStr, HEADERS, maxFileSize)) {
            Set<String> csvCodes = new HashSet<>();
            List<BranchImportDto> dtos = new ArrayList<>();

            // 1. Parse and validate CSV duplicates
            for (CSVRecord record : parser) {
                processed++;
                String branchCode = record.get("branch_code");
                if (branchCode == null || branchCode.isBlank()) {
                    errors.add("Line " + (record.getRecordNumber() + 1) + ": Branch code is blank");
                    continue;
                }
                branchCode = branchCode.trim();
                if (!csvCodes.add(branchCode)) {
                    throw new IllegalArgumentException("Duplicate branch code inside CSV: " + branchCode);
                }

                BranchImportDto dto = BranchImportDto.builder()
                        .branchCode(branchCode)
                        .name(record.get("name"))
                        .build();
                dtos.add(dto);
            }

            if (!errors.isEmpty()) {
                throw new IllegalArgumentException("CSV validation errors: " + errors);
            }

            // 2. Perform deletes if replaceExisting=true
            if (replaceExisting && !dryRun) {
                branchRepository.deleteAllInBatch();
            }

            // 3. Process entities
            List<Branch> batch = new ArrayList<>();
            for (BranchImportDto dto : dtos) {
                boolean exists = !replaceExisting && branchRepository.existsByBranchCode(dto.getBranchCode());
                if (exists) {
                    duplicate++;
                    skipped++;
                    continue;
                }

                Branch branch = branchMapper.toEntity(dto);
                inserted++;

                if (!dryRun) {
                    batch.add(branch);
                    if (batch.size() >= batchSize) {
                        branchRepository.saveAll(batch);
                        if (entityManager != null) {
                            entityManager.flush();
                            entityManager.clear();
                        }
                        log.info("Saved batch of branches. Progress: {}/{}", inserted, dtos.size());
                        batch.clear();
                    }
                }
            }

            if (!dryRun && !batch.isEmpty()) {
                branchRepository.saveAll(batch);
                if (entityManager != null) {
                    entityManager.flush();
                    entityManager.clear();
                }
                log.info("Saved final batch of branches. Total inserted: {}", inserted);
            }

        } catch (IOException e) {
            log.error("Failed to read branches CSV", e);
            throw new RuntimeException("Failed to read branches CSV file", e);
        }

        long duration = System.currentTimeMillis() - startTime;
        DatasetStats stats = DatasetStats.builder()
                .processed(processed)
                .inserted(inserted)
                .updated(0)
                .skipped(skipped)
                .duplicate(duplicate)
                .build();

        log.info("Branches import finished. Processed: {}, Inserted: {}, Skipped: {}, Time: {} ms",
                processed, inserted, skipped, duration);

        return ImportSummaryResponse.builder()
                .status("SUCCESS")
                .executionTime((duration / 1000.0) + " seconds")
                .datasetsImported(1)
                .rowsProcessed(processed)
                .rowsInserted(inserted)
                .rowsUpdated(0)
                .rowsSkipped(skipped)
                .duplicateRows(duplicate)
                .datasetDetails(Map.of("Branches", stats))
                .build();
    }
}
