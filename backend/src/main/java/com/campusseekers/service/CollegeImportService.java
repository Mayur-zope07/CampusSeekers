package com.campusseekers.service;

import com.campusseekers.dto.CollegeImportDto;
import com.campusseekers.dto.ImportSummaryResponse;
import com.campusseekers.dto.ImportSummaryResponse.DatasetStats;
import com.campusseekers.entity.College;
import com.campusseekers.mapper.CollegeImportMapper;
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
public class CollegeImportService {

    private final CsvImportService csvImportService;
    private final CollegeRepository collegeRepository;
    private final CollegeImportMapper collegeMapper;

    @PersistenceContext
    private EntityManager entityManager;

    private static final String[] HEADERS = {
            "college_code", "name", "college_type", "establishment_year", "city", "state",
            "website", "naac_grade", "nba_accredited", "campus_size", "logo_url", "status"
    };

    @Transactional
    public ImportSummaryResponse importCsv(String pathStr, boolean replaceExisting, boolean dryRun, long maxFileSize, int batchSize) {
        log.info("Starting College import. File: {}, replaceExisting: {}, dryRun: {}", pathStr, replaceExisting, dryRun);
        long startTime = System.currentTimeMillis();

        int processed = 0;
        int inserted = 0;
        int skipped = 0;
        int duplicate = 0;
        List<String> errors = new ArrayList<>();

        try (CSVParser parser = csvImportService.parseCsv(pathStr, HEADERS, maxFileSize)) {
            Set<String> csvCodes = new HashSet<>();
            List<CollegeImportDto> dtos = new ArrayList<>();

            // 1. Parse and validate CSV duplicates
            for (CSVRecord record : parser) {
                processed++;
                String collegeCode = record.get("college_code");
                if (collegeCode == null || collegeCode.isBlank()) {
                    errors.add("Line " + (record.getRecordNumber() + 1) + ": College code is blank");
                    continue;
                }
                collegeCode = collegeCode.trim();
                if (!csvCodes.add(collegeCode)) {
                    throw new IllegalArgumentException("Duplicate college code inside CSV: " + collegeCode);
                }

                CollegeImportDto dto = CollegeImportDto.builder()
                        .collegeCode(collegeCode)
                        .name(record.get("name"))
                        .collegeType(record.get("college_type"))
                        .establishmentYear(record.get("establishment_year"))
                        .city(record.get("city"))
                        .state(record.get("state"))
                        .website(record.get("website"))
                        .naacGrade(record.get("naac_grade"))
                        .nbaAccredited(record.get("nba_accredited"))
                        .campusSize(record.get("campus_size"))
                        .logoUrl(record.get("logo_url"))
                        .status(record.get("status"))
                        .build();
                dtos.add(dto);
            }

            if (!errors.isEmpty()) {
                throw new IllegalArgumentException("CSV validation errors: " + errors);
            }

            // 2. Perform deletes in transaction if replaceExisting=true
            if (replaceExisting && !dryRun) {
                collegeRepository.deleteAllInBatch();
            }

            // 3. Process entities
            List<College> batch = new ArrayList<>();
            for (CollegeImportDto dto : dtos) {
                boolean exists = !replaceExisting && collegeRepository.existsByCollegeCode(dto.getCollegeCode());
                if (exists) {
                    duplicate++;
                    skipped++;
                    continue;
                }

                College college = collegeMapper.toEntity(dto);
                inserted++;

                if (!dryRun) {
                    batch.add(college);
                    if (batch.size() >= batchSize) {
                        collegeRepository.saveAll(batch);
                        if (entityManager != null) {
                            entityManager.flush();
                            entityManager.clear();
                        }
                        log.info("Saved batch of colleges. Progress: {}/{}", inserted, dtos.size());
                        batch.clear();
                    }
                }
            }

            if (!dryRun && !batch.isEmpty()) {
                collegeRepository.saveAll(batch);
                if (entityManager != null) {
                    entityManager.flush();
                    entityManager.clear();
                }
                log.info("Saved final batch of colleges. Total inserted: {}", inserted);
            }

        } catch (IOException e) {
            log.error("Failed to read colleges CSV", e);
            throw new RuntimeException("Failed to read colleges CSV file", e);
        }

        long duration = System.currentTimeMillis() - startTime;
        DatasetStats stats = DatasetStats.builder()
                .processed(processed)
                .inserted(inserted)
                .updated(0)
                .skipped(skipped)
                .duplicate(duplicate)
                .build();

        log.info("Colleges import finished. Processed: {}, Inserted: {}, Skipped: {}, Time: {} ms",
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
                .datasetDetails(Map.of("Colleges", stats))
                .build();
    }
}
