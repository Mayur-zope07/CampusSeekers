package com.campusseekers.service.impl;

import com.campusseekers.dto.ExportResponse;
import com.campusseekers.dto.RecommendationItemResponse;
import com.campusseekers.dto.ShortlistResponse;
import com.campusseekers.dto.WishlistResponse;
import com.campusseekers.dto.DashboardResponse;
import com.campusseekers.dto.DashboardStatisticsResponse;
import com.campusseekers.service.ExportService;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Slf4j
@Service
public class PdfExportService implements ExportService {

    @Override
    public String getFormat() {
        return "PDF";
    }

    @Override
    public ExportResponse exportWishlist(List<WishlistResponse> wishlist) {
        log.info("Generating Wishlist PDF export for {} items", wishlist.size());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Font Styling
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
            Paragraph title = new Paragraph("CampusSeekers - Saved Wishlist", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Table setup
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.5f, 3.5f, 1.5f, 1.5f, 1.0f});

            addHeaderCell(table, "Code");
            addHeaderCell(table, "College Name");
            addHeaderCell(table, "City");
            addHeaderCell(table, "State");
            addHeaderCell(table, "NAAC");

            for (WishlistResponse w : wishlist) {
                addDataCell(table, w.collegeCode());
                addDataCell(table, w.collegeName());
                addDataCell(table, w.city());
                addDataCell(table, w.state());
                addDataCell(table, w.naacGrade());
            }

            document.add(table);
            document.close();
        } catch (Exception e) {
            log.error("Failed to generate Wishlist PDF", e);
        }
        return new ExportResponse("wishlist.pdf", "application/pdf", out.toByteArray());
    }

    @Override
    public ExportResponse exportShortlist(List<ShortlistResponse> shortlist) {
        log.info("Generating Shortlist PDF export for {} items", shortlist.size());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate());
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
            Paragraph title = new Paragraph("CampusSeekers - Shortlisted College Branches", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.5f, 3.0f, 2.5f, 1.5f, 1.0f, 1.5f, 2.0f});

            addHeaderCell(table, "Code");
            addHeaderCell(table, "College Name");
            addHeaderCell(table, "Branch Name");
            addHeaderCell(table, "City");
            addHeaderCell(table, "Priority");
            addHeaderCell(table, "Fees");
            addHeaderCell(table, "Status");

            for (ShortlistResponse s : shortlist) {
                String status = s.tracker() != null ? s.tracker().currentStatus().name() : "INTERESTED";
                addDataCell(table, s.collegeCode());
                addDataCell(table, s.collegeName());
                addDataCell(table, s.branchName());
                addDataCell(table, s.city());
                addDataCell(table, s.priority() != null ? String.valueOf(s.priority()) : "-");
                addDataCell(table, s.feesPerYear() != null ? s.feesPerYear().toString() : "-");
                addDataCell(table, status);
            }

            document.add(table);
            document.close();
        } catch (Exception e) {
            log.error("Failed to generate Shortlist PDF", e);
        }
        return new ExportResponse("shortlist.pdf", "application/pdf", out.toByteArray());
    }

    @Override
    public ExportResponse exportRecommendations(List<RecommendationItemResponse> recommendations) {
        log.info("Generating Recommendations PDF export for {} items", recommendations.size());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate());
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
            Paragraph title = new Paragraph("CampusSeekers - College Recommendation Summary", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.5f, 3.0f, 2.5f, 1.5f, 1.2f, 1.2f, 1.5f});

            addHeaderCell(table, "Code");
            addHeaderCell(table, "College Name");
            addHeaderCell(table, "Branch Name");
            addHeaderCell(table, "City");
            addHeaderCell(table, "Cutoff %");
            addHeaderCell(table, "Diff %");
            addHeaderCell(table, "Category");

            for (RecommendationItemResponse r : recommendations) {
                addDataCell(table, r.collegeCode());
                addDataCell(table, r.collegeName());
                addDataCell(table, r.branchName());
                addDataCell(table, r.city());
                addDataCell(table, r.closingPercentile() != null ? r.closingPercentile().toString() : "-");
                addDataCell(table, r.percentileDifference() != null ? r.percentileDifference().toString() : "-");
                addDataCell(table, r.recommendationCategory() != null ? r.recommendationCategory().name() : "-");
            }

            document.add(table);
            document.close();
        } catch (Exception e) {
            log.error("Failed to generate Recommendations PDF", e);
        }
        return new ExportResponse("recommendations.pdf", "application/pdf", out.toByteArray());
    }

    @Override
    public ExportResponse exportDashboard(DashboardResponse dashboard) {
        log.info("Generating Dashboard PDF export");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
            Paragraph title = new Paragraph("CampusSeekers - Student Dashboard Summary", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3.0f, 3.0f});

            addHeaderCell(table, "Metric Name");
            addHeaderCell(table, "Value");

            DashboardStatisticsResponse stats = dashboard.statistics();
            addDataCell(table, "Wishlist Count");
            addDataCell(table, String.valueOf(stats.wishlistCount()));

            addDataCell(table, "Shortlist Count");
            addDataCell(table, String.valueOf(stats.shortlistCount()));

            addDataCell(table, "Recommendation Count");
            addDataCell(table, String.valueOf(stats.recommendationCount()));

            addDataCell(table, "SAFE Recommendations");
            addDataCell(table, String.valueOf(stats.safeCount()));

            addDataCell(table, "TARGET Recommendations");
            addDataCell(table, String.valueOf(stats.targetCount()));

            addDataCell(table, "DREAM Recommendations");
            addDataCell(table, String.valueOf(stats.dreamCount()));

            addDataCell(table, "Active Applications");
            addDataCell(table, String.valueOf(stats.applicationsCount()));

            addDataCell(table, "Average Shortlisted Fees");
            addDataCell(table, stats.averageFees() != null ? stats.averageFees().toString() : "0.00");

            addDataCell(table, "Highest Shortlisted Package");
            addDataCell(table, stats.highestPackage() != null ? stats.highestPackage().toString() + " LPA" : "0.00 LPA");

            addDataCell(table, "Lowest Closing Cutoff");
            addDataCell(table, stats.lowestCutoff() != null ? stats.lowestCutoff().toString() : "0.00");

            document.add(table);
            document.close();
        } catch (Exception e) {
            log.error("Failed to generate Dashboard PDF", e);
        }
        return new ExportResponse("dashboard_summary.pdf", "application/pdf", out.toByteArray());
    }

    private void addHeaderCell(PdfPTable table, String text) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(text, headerFont));
        cell.setBackgroundColor(new Color(41, 128, 185)); // Brand blue
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6);
        table.addCell(cell);
    }

    private void addDataCell(PdfPTable table, String text) {
        Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
        PdfPCell cell = new PdfPCell(new Phrase(text == null ? "" : text, dataFont));
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        table.addCell(cell);
    }
}
