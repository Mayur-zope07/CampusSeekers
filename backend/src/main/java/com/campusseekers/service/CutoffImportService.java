package com.campusseekers.service;

import com.campusseekers.dto.CutoffImportDto;
import com.campusseekers.dto.ImportSummaryResponse;
import com.campusseekers.dto.ImportSummaryResponse.DatasetStats;
import com.campusseekers.entity.Category;
import com.campusseekers.entity.CollegeBranch;
import com.campusseekers.entity.Cutoff;
import com.campusseekers.entity.ExamName;
import com.campusseekers.mapper.CutoffImportMapper;
import com.campusseekers.repository.CollegeBranchRepository;
import com.campusseekers.repository.CutoffRepository;
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
public class CutoffImportService {

    private final CsvImportService csvImportService;
    private final CutoffRepository cutoffRepository;
    private final CollegeBranchRepository collegeBranchRepository;
    private final CutoffImportMapper cutoffMapper;

    @PersistenceContext
    private EntityManager entityManager;

    private static final String[] HEADERS = {
            "college_code", "branch_code", "exam_name", "year", "round", "category", "raw_seat_type", "closing_rank", "closing_percentile"
    };

    @Transactional
    public ImportSummaryResponse importCsv(String pathStr, boolean replaceExisting, boolean dryRun, long maxFileSize, int batchSize,
                                           Set<String> contextualCollegeBranches) {
        log.info("Starting Cutoff import. File: {}, replaceExisting: {}, dryRun: {}", pathStr, replaceExisting, dryRun);
        long startTime = System.currentTimeMillis();

        int processed = 0;
        int inserted = 0;
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
            List<CutoffImportDto> dtos = new ArrayList<>();

            // 1. Parse and validate CSV
            for (CSVRecord record : parser) {
                processed++;
                String collegeCode = record.get("college_code");
                String branchCode = record.get("branch_code");
                String examName = record.get("exam_name");
                String year = record.get("year");
                String round = record.get("round");
                String category = record.get("category");
                String rawSeatType = record.get("raw_seat_type");

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
                String duplicateKey = cbKey + "|" + examName + "|" + year + "|" + round + "|" + category + "|" + rawSeatType;
                if (!csvKeys.add(duplicateKey)) {
                    throw new IllegalArgumentException("Duplicate cutoff record inside CSV: " + duplicateKey);
                }

                CutoffImportDto dto = CutoffImportDto.builder()
                        .collegeCode(collegeCode)
                        .branchCode(branchCode)
                        .examName(examName)
                        .year(year)
                        .round(round)
                        .category(category)
                        .rawSeatType(rawSeatType)
                        .closingRank(record.get("closing_rank"))
                        .closingPercentile(record.get("closing_percentile"))
                        .build();
                dtos.add(dto);
            }

            if (!errors.isEmpty()) {
                throw new IllegalArgumentException("CSV validation errors: " + errors);
            }

            // 2. Perform deletes if replaceExisting=true
            if (replaceExisting && !dryRun) {
                cutoffRepository.deleteAllInBatch();
            }

            // 3. Process entities
            List<Cutoff> batch = new ArrayList<>();
            for (CutoffImportDto dto : dtos) {
                String cbKey = dto.getCollegeCode() + "-" + dto.getBranchCode();
                CollegeBranch cb = cbMap.get(cbKey);

                boolean exists = false;
                if (!replaceExisting && cb != null) {
                    try {
                        exists = cutoffRepository.existsByCollegeBranchIdAndExamNameAndYearAndRoundAndCategoryAndRawSeatType(
                                cb.getId(),
                                ExamName.valueOf(dto.getExamName().trim()),
                                Integer.parseInt(dto.getYear().trim()),
                                Integer.parseInt(dto.getRound().trim()),
                                Category.valueOf(dto.getCategory().trim()),
                                dto.getRawSeatType().trim()
                        );
                    } catch (Exception e) {
                        // If parsing enums fails, it doesn't exist
                    }
                }

                if (exists) {
                    duplicate++;
                    skipped++;
                    continue;
                }

                inserted++;

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

                    Cutoff cutoff = cutoffMapper.toEntity(dto, cb);
                    batch.add(cutoff);
                    if (batch.size() >= batchSize) {
                        cutoffRepository.saveAll(batch);
                        if (entityManager != null) {
                            entityManager.flush();
                            entityManager.clear();
                        }
                        log.info("Saved batch of cutoffs. Progress: {}/{}", inserted, dtos.size());
                        batch.clear();
                    }
                }
            }

            if (!dryRun && !batch.isEmpty()) {
                cutoffRepository.saveAll(batch);
                if (entityManager != null) {
                    entityManager.flush();
                    entityManager.clear();
                }
                log.info("Saved final batch of cutoffs. Total inserted: {}", inserted);
            }

        } catch (IOException e) {
            log.error("Failed to read cutoffs CSV", e);
            throw new RuntimeException("Failed to read cutoffs CSV file", e);
        }

        long duration = System.currentTimeMillis() - startTime;
        DatasetStats stats = DatasetStats.builder()
                .processed(processed)
                .inserted(inserted)
                .updated(0)
                .skipped(skipped)
                .duplicate(duplicate)
                .build();

        log.info("Cutoffs import finished. Processed: {}, Inserted: {}, Skipped: {}, Time: {} ms",
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
                .datasetDetails(Map.of("Cutoffs", stats))
                .build();
    }
}
