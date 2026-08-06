package com.campusseekers.controller;

import com.campusseekers.dto.*;
import com.campusseekers.entity.Category;
import com.campusseekers.entity.ExamName;
import com.campusseekers.exception.BadRequestException;
import com.campusseekers.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
@Tag(name = "Search & Discovery APIs", description = "Endpoints for searching, filtering, and comparing colleges")
public class SearchController {

    private final PercentileSearchService percentileSearchService;
    private final CollegeComparisonService collegeComparisonService;

    private Pageable createPageable(int page, int size, String sort, String direction) {
        if (page < 0) {
            throw new BadRequestException("Page index must not be less than zero");
        }
        if (size <= 0) {
            throw new BadRequestException("Page size must be greater than zero");
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

    @GetMapping("/search")
    @Operation(summary = "Percentile search", description = "Search eligible college branches matching target student percentile score bounds")
    public ResponseEntity<ApiResponse<PageResponse<SearchResultResponse>>> searchByPercentile(
            @RequestParam ExamName exam,
            @RequestParam Integer year,
            @RequestParam BigDecimal percentile,
            @RequestParam Category category,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String direction) {

        Pageable pageable = createPageable(page, size, sort, direction);
        PageResponse<SearchResultResponse> response = percentileSearchService.searchByPercentile(exam, year, percentile, category, branch, city, state, pageable);
        return ResponseEntity.ok(ApiResponse.<PageResponse<SearchResultResponse>>builder()
                .success(true)
                .message("Percentile search completed successfully.")
                .data(response)
                .build());
    }

    @GetMapping("/compare")
    @Operation(summary = "Compare colleges", description = "Compare statistics for multiple colleges (2 to 5)")
    public ResponseEntity<ApiResponse<ComparisonResponse>> compareColleges(@RequestParam List<UUID> collegeIds) {
        if (collegeIds == null || collegeIds.size() < 2 || collegeIds.size() > 5) {
            throw new BadRequestException("Comparison list must contain between 2 and 5 college IDs");
        }
        ComparisonResponse response = collegeComparisonService.compareColleges(collegeIds);
        return ResponseEntity.ok(ApiResponse.<ComparisonResponse>builder()
                .success(true)
                .message("Colleges comparison completed successfully.")
                .data(response)
                .build());
    }
}
