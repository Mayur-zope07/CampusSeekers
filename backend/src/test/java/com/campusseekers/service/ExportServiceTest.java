package com.campusseekers.service;

import com.campusseekers.dto.ExportResponse;
import com.campusseekers.dto.ShortlistResponse;
import com.campusseekers.dto.WishlistResponse;
import com.campusseekers.dto.RecommendationItemResponse;
import com.campusseekers.entity.AdmissionStatus;
import com.campusseekers.entity.CollegeType;
import com.campusseekers.entity.RecommendationCategory;
import com.campusseekers.service.impl.CsvExportService;
import com.campusseekers.service.impl.PdfExportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ExportServiceTest {

    private CsvExportService csvExportService;
    private PdfExportService pdfExportService;

    private List<WishlistResponse> wishlist;
    private List<ShortlistResponse> shortlist;
    private List<RecommendationItemResponse> recommendations;

    @BeforeEach
    void setUp() {
        csvExportService = new CsvExportService();
        pdfExportService = new PdfExportService();

        UUID studentId = UUID.randomUUID();
        UUID collegeId = UUID.randomUUID();

        wishlist = List.of(new WishlistResponse(
                UUID.randomUUID(), studentId, collegeId, "1002", "COEP", "Pune", "MH", "A++", Instant.now(), false
        ));

        shortlist = List.of(new ShortlistResponse(
                UUID.randomUUID(), studentId, UUID.randomUUID(), collegeId, "1002", "COEP",
                UUID.randomUUID(), "CO", "Computers", "Pune", "MH", "A++",
                new BigDecimal("120000.00"), 1, "note", false, Instant.now(), null
        ));

        recommendations = List.of(new RecommendationItemResponse(
                collegeId, "1002", "COEP", UUID.randomUUID(), "CO", "Computers", "Pune", "MH",
                CollegeType.GOVERNMENT, "A++", true, 4, 120, new BigDecimal("120000.00"),
                new BigDecimal("94.50"), new BigDecimal("95.00"), new BigDecimal("0.50"),
                RecommendationCategory.DREAM, null, "reason", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        ));
    }

    @Test
    void csvExport_ShouldGenerateCsvBytes() {
        assertEquals("CSV", csvExportService.getFormat());

        ExportResponse wCsv = csvExportService.exportWishlist(wishlist);
        assertNotNull(wCsv);
        assertTrue(wCsv.fileContent().length > 0);
        assertTrue(new String(wCsv.fileContent()).contains("COEP"));

        ExportResponse sCsv = csvExportService.exportShortlist(shortlist);
        assertNotNull(sCsv);
        assertTrue(sCsv.fileContent().length > 0);
        assertTrue(new String(sCsv.fileContent()).contains("Computers"));
    }

    @Test
    void pdfExport_ShouldGeneratePdfBytes() {
        assertEquals("PDF", pdfExportService.getFormat());

        ExportResponse wPdf = pdfExportService.exportWishlist(wishlist);
        assertNotNull(wPdf);
        assertTrue(wPdf.fileContent().length > 0);

        ExportResponse sPdf = pdfExportService.exportShortlist(shortlist);
        assertNotNull(sPdf);
        assertTrue(sPdf.fileContent().length > 0);
    }
}
