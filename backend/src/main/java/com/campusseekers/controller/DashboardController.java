package com.campusseekers.controller;

import com.campusseekers.dto.ApiResponse;
import com.campusseekers.dto.DashboardResponse;
import com.campusseekers.dto.DashboardStatisticsResponse;
import com.campusseekers.dto.ExportResponse;
import com.campusseekers.dto.ShortlistResponse;
import com.campusseekers.dto.WishlistResponse;
import com.campusseekers.entity.StudentProfile;
import com.campusseekers.entity.User;
import com.campusseekers.exception.BadRequestException;
import com.campusseekers.exception.ResourceNotFoundException;
import com.campusseekers.exception.UnauthorizedException;
import com.campusseekers.repository.StudentProfileRepository;
import com.campusseekers.repository.UserRepository;
import com.campusseekers.service.*;
import com.campusseekers.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Dashboard", description = "Endpoints for viewing personalized student workflow statistics and downloading PDF/CSV data exports")
public class DashboardController {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final DashboardService dashboardService;
    private final DashboardStatisticsService dashboardStatisticsService;
    private final WishlistService wishlistService;
    private final ShortlistService shortlistService;
    private final List<ExportService> exportServices;

    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get student dashboard preview", description = "Returns a consolidated dashboard response containing statistics, wishlist, shortlists, and activity feeds.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dashboard retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {
        DashboardResponse response = dashboardService.getDashboard();
        return ResponseEntity.ok(ApiResponse.<DashboardResponse>builder()
                .success(true)
                .message("Dashboard retrieved successfully")
                .data(response)
                .build());
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "Get workflow statistics", description = "Returns cached student workflow counts, breakdown, and average fee statistics. Admin can view overall analytics.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<DashboardStatisticsResponse>> getStatistics() {
        StudentProfile studentProfile = getAuthenticatedStudentProfile();
        DashboardStatisticsResponse response = dashboardStatisticsService.getStatistics(studentProfile.getId());
        return ResponseEntity.ok(ApiResponse.<DashboardStatisticsResponse>builder()
                .success(true)
                .message("Statistics retrieved successfully")
                .data(response)
                .build());
    }

    @GetMapping("/export/pdf")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Export data as PDF", description = "Generates and downloads a PDF document of the specified dataset (wishlist, shortlist, recommendations, or dashboard summary).")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "PDF report generated successfully",
                    content = @Content(mediaType = "application/pdf")),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid export type parameter"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<byte[]> exportPdf(@RequestParam String type) {
        ExportResponse response = executeExport("PDF", type);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + response.fileName())
                .contentType(MediaType.parseMediaType(response.contentType()))
                .body(response.fileContent());
    }

    @GetMapping("/export/csv")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Export data as CSV", description = "Generates and downloads a CSV spreadsheet of the specified dataset (wishlist, shortlist, recommendations, or dashboard summary).")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "CSV report generated successfully",
                    content = @Content(mediaType = "text/csv")),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid export type parameter"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<byte[]> exportCsv(@RequestParam String type) {
        ExportResponse response = executeExport("CSV", type);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + response.fileName())
                .contentType(MediaType.parseMediaType(response.contentType()))
                .body(response.fileContent());
    }

    private ExportResponse executeExport(String format, String type) {
        ExportService exportService = exportServices.stream()
                .filter(s -> s.getFormat().equalsIgnoreCase(format))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Unsupported export format: " + format));

        StudentProfile studentProfile = getAuthenticatedStudentProfile();

        return switch (type.toLowerCase()) {
            case "wishlist" -> {
                List<WishlistResponse> wishlist = wishlistService.searchWishlist(null, null, Pageable.unpaged()).getContent();
                yield exportService.exportWishlist(wishlist);
            }
            case "shortlist" -> {
                List<ShortlistResponse> shortlist = shortlistService.searchShortlist(
                        null, null, null, null, null, null, null, null, null, Pageable.unpaged()
                ).getContent();
                yield exportService.exportShortlist(shortlist);
            }
            case "recommendations" -> {
                DashboardResponse db = dashboardService.getDashboard();
                yield exportService.exportRecommendations(db.recentRecommendations());
            }
            case "dashboard" -> {
                DashboardResponse db = dashboardService.getDashboard();
                yield exportService.exportDashboard(db);
            }
            default -> throw new BadRequestException("Unsupported export type: " + type);
        };
    }

    private StudentProfile getAuthenticatedStudentProfile() {
        String email = SecurityUtils.getCurrentUserEmail()
                .orElseThrow(() -> new UnauthorizedException("User is not authenticated"));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return studentProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user: " + email));
    }
}
