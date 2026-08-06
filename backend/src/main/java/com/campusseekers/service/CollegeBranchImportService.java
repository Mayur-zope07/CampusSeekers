package com.campusseekers.service;

import com.campusseekers.dto.CollegeBranchImportDto;
import com.campusseekers.dto.ImportSummaryResponse;
import com.campusseekers.dto.ImportSummaryResponse.DatasetStats;
import com.campusseekers.entity.Branch;
import com.campusseekers.entity.College;
import com.campusseekers.entity.CollegeBranch;
import com.campusseekers.mapper.CollegeBranchImportMapper;
import com.campusseekers.repository.BranchRepository;
import com.campusseekers.repository.CollegeBranchRepository;
import com.campusseekers.repository.CollegeRepository;
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
public class CollegeBranchImportService {

    private final CsvImportService csvImportService;
    private final CollegeBranchRepository collegeBranchRepository;
    private final CollegeRepository collegeRepository;
    private final BranchRepository branchRepository;
    private final CollegeBranchImportMapper collegeBranchMapper;

    @PersistenceContext
    private EntityManager entityManager;

    private static final String[] HEADERS = {
            "college_code", "branch_code", "intake_capacity", "fees_per_year", "duration_years"
    };

    @Transactional
    public ImportSummaryResponse importCsv(String pathStr, boolean replaceExisting, boolean dryRun, long maxFileSize, int batchSize,
                                           Set<String> contextualColleges, Set<String> contextualBranches, Set<String> currentImportedCollegeBranches) {
        log.info("Starting CollegeBranch import. File: {}, replaceExisting: {}, dryRun: {}", pathStr, replaceExisting, dryRun);
        long startTime = System.currentTimeMillis();

        int processed = 0;
        int inserted = 0;
        int skipped = 0;
        int duplicate = 0;
        List<String> errors = new ArrayList<>();

        // Load existing entities for validation and lookups
        List<College> dbColleges = collegeRepository.findAll();
        Map<String, College> collegeMap = new HashMap<>();
        for (College c : dbColleges) {
            collegeMap.put(c.getCollegeCode(), c);
        }

        List<Branch> dbBranches = branchRepository.findAll();
        Map<String, Branch> branchMap = new HashMap<>();
        for (Branch b : dbBranches) {
            branchMap.put(b.getBranchCode(), b);
        }

        try (CSVParser parser = csvImportService.parseCsv(pathStr, HEADERS, maxFileSize)) {
            Set<String> csvKeys = new HashSet<>();
            List<CollegeBranchImportDto> dtos = new ArrayList<>();

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
                String key = collegeCode + "-" + branchCode;

                if (!csvKeys.add(key)) {
                    throw new IllegalArgumentException("Duplicate college branch mapping inside CSV: " + key);
                }

                // Verify referential integrity
                boolean collegeExists = collegeMap.containsKey(collegeCode) || (contextualColleges != null && contextualColleges.contains(collegeCode));
                boolean branchExists = branchMap.containsKey(branchCode) || (contextualBranches != null && contextualBranches.contains(branchCode));

                if (!collegeExists) {
                    throw new IllegalArgumentException("Referential Integrity Failure: College code " + collegeCode + " not found in database or current import context");
                }
                if (!branchExists) {
                    throw new IllegalArgumentException("Referential Integrity Failure: Branch code " + branchCode + " not found in database or current import context");
                }

                CollegeBranchImportDto dto = CollegeBranchImportDto.builder()
                        .collegeCode(collegeCode)
                        .branchCode(branchCode)
                        .intakeCapacity(record.get("intake_capacity"))
                        .feesPerYear(record.get("fees_per_year"))
                        .durationYears(record.get("duration_years"))
                        .build();
                dtos.add(dto);
                
                // Track for downstream validation
                if (currentImportedCollegeBranches != null) {
                    currentImportedCollegeBranches.add(key);
                }
            }

            if (!errors.isEmpty()) {
                throw new IllegalArgumentException("CSV validation errors: " + errors);
            }

            // 2. Perform deletes if replaceExisting=true
            if (replaceExisting && !dryRun) {
                collegeBranchRepository.deleteAllInBatch();
            }

            // 3. Process entities
            List<CollegeBranch> batch = new ArrayList<>();
            for (CollegeBranchImportDto dto : dtos) {
                College college = collegeMap.get(dto.getCollegeCode());
                Branch branch = branchMap.get(dto.getBranchCode());

                boolean exists = false;
                if (!replaceExisting && college != null && branch != null) {
                    exists = collegeBranchRepository.existsByCollegeIdAndBranchId(college.getId(), branch.getId());
                }

                if (exists) {
                    duplicate++;
                    skipped++;
                    continue;
                }

                inserted++;

                if (!dryRun) {
                    // For mapping, if entities were newly inserted in dryRun/db we need actual entities.
                    // If running in bulk import all with replaceExisting, college and branch must exist in database now.
                    if (college == null) {
                        college = collegeRepository.findByCollegeCode(dto.getCollegeCode())
                                .orElseThrow(() -> new IllegalArgumentException("College not found: " + dto.getCollegeCode()));
                        collegeMap.put(college.getCollegeCode(), college);
                    }
                    if (branch == null) {
                        branch = branchRepository.findByBranchCode(dto.getBranchCode())
                                .orElseThrow(() -> new IllegalArgumentException("Branch not found: " + dto.getBranchCode()));
                        branchMap.put(branch.getBranchCode(), branch);
                    }

                    CollegeBranch collegeBranch = collegeBranchMapper.toEntity(dto, college, branch);
                    batch.add(collegeBranch);
                    if (batch.size() >= batchSize) {
                        collegeBranchRepository.saveAll(batch);
                        if (entityManager != null) {
                            entityManager.flush();
                            entityManager.clear();
                        }
                        log.info("Saved batch of college branches. Progress: {}/{}", inserted, dtos.size());
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
                log.info("Saved final batch of college branches. Total inserted: {}", inserted);
            }

        } catch (IOException e) {
            log.error("Failed to read college branches CSV", e);
            throw new RuntimeException("Failed to read college branches CSV file", e);
        }

        long duration = System.currentTimeMillis() - startTime;
        DatasetStats stats = DatasetStats.builder()
                .processed(processed)
                .inserted(inserted)
                .updated(0)
                .skipped(skipped)
                .duplicate(duplicate)
                .build();

        log.info("CollegeBranches import finished. Processed: {}, Inserted: {}, Skipped: {}, Time: {} ms",
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
                .datasetDetails(Map.of("College Branches", stats))
                .build();
    }
}
