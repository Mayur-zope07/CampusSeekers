package com.campusseekers.service.impl;

import com.campusseekers.dto.ExportResponse;
import com.campusseekers.dto.RecommendationItemResponse;
import com.campusseekers.dto.ShortlistResponse;
import com.campusseekers.dto.WishlistResponse;
import com.campusseekers.dto.DashboardResponse;
import com.campusseekers.dto.DashboardStatisticsResponse;
import com.campusseekers.service.ExportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Service
public class CsvExportService implements ExportService {

    @Override
    public String getFormat() {
        return "CSV";
    }

    @Override
    public ExportResponse exportWishlist(List<WishlistResponse> wishlist) {
        log.info("Generating Wishlist CSV export for {} items", wishlist.size());
        StringWriter writer = new StringWriter();
        writer.write("ID,College Code,College Name,City,State,NAAC Grade,Created At\n");
        for (WishlistResponse w : wishlist) {
            writer.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    w.id(),
                    escapeCsv(w.collegeCode()),
                    escapeCsv(w.collegeName()),
                    escapeCsv(w.city()),
                    escapeCsv(w.state()),
                    escapeCsv(w.naacGrade()),
                    w.createdAt()
            ));
        }
        byte[] bytes = writer.toString().getBytes(StandardCharsets.UTF_8);
        return new ExportResponse("wishlist.csv", "text/csv", bytes);
    }

    @Override
    public ExportResponse exportShortlist(List<ShortlistResponse> shortlist) {
        log.info("Generating Shortlist CSV export for {} items", shortlist.size());
        StringWriter writer = new StringWriter();
        writer.write("ID,College Code,College Name,Branch Code,Branch Name,City,State,Fees Per Year,Priority,Notes,Admission Status\n");
        for (ShortlistResponse s : shortlist) {
            String status = s.tracker() != null ? s.tracker().currentStatus().name() : "N/A";
            writer.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    s.id(),
                    escapeCsv(s.collegeCode()),
                    escapeCsv(s.collegeName()),
                    escapeCsv(s.branchCode()),
                    escapeCsv(s.branchName()),
                    escapeCsv(s.city()),
                    escapeCsv(s.state()),
                    s.feesPerYear(),
                    s.priority() != null ? s.priority() : "",
                    escapeCsv(s.notes()),
                    status
            ));
        }
        byte[] bytes = writer.toString().getBytes(StandardCharsets.UTF_8);
        return new ExportResponse("shortlist.csv", "text/csv", bytes);
    }

    @Override
    public ExportResponse exportRecommendations(List<RecommendationItemResponse> recommendations) {
        log.info("Generating Recommendations CSV export for {} items", recommendations.size());
        StringWriter writer = new StringWriter();
        writer.write("College Code,College Name,Branch Code,Branch Name,City,State,College Type,Closing Percentile,Student Percentile,Difference,Category,Reason\n");
        for (RecommendationItemResponse r : recommendations) {
            writer.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    escapeCsv(r.collegeCode()),
                    escapeCsv(r.collegeName()),
                    escapeCsv(r.branchCode()),
                    escapeCsv(r.branchName()),
                    escapeCsv(r.city()),
                    escapeCsv(r.state()),
                    r.collegeType(),
                    r.closingPercentile(),
                    r.studentPercentile(),
                    r.percentileDifference(),
                    r.recommendationCategory(),
                    escapeCsv(r.humanReadableReason())
            ));
        }
        byte[] bytes = writer.toString().getBytes(StandardCharsets.UTF_8);
        return new ExportResponse("recommendations.csv", "text/csv", bytes);
    }

    @Override
    public ExportResponse exportDashboard(DashboardResponse dashboard) {
        log.info("Generating Dashboard CSV export");
        StringWriter writer = new StringWriter();
        writer.write("Metric,Value\n");
        DashboardStatisticsResponse stats = dashboard.statistics();
        writer.write(String.format("Wishlist Count,%d\n", stats.wishlistCount()));
        writer.write(String.format("Shortlist Count,%d\n", stats.shortlistCount()));
        writer.write(String.format("Recommendation Count,%d\n", stats.recommendationCount()));
        writer.write(String.format("SAFE Recommendations,%d\n", stats.safeCount()));
        writer.write(String.format("TARGET Recommendations,%d\n", stats.targetCount()));
        writer.write(String.format("DREAM Recommendations,%d\n", stats.dreamCount()));
        writer.write(String.format("Applications Count,%d\n", stats.applicationsCount()));
        writer.write(String.format("Average Fees,%s\n", stats.averageFees()));
        writer.write(String.format("Highest Package,%s\n", stats.highestPackage()));
        writer.write(String.format("Lowest Cutoff,%s\n", stats.lowestCutoff()));

        byte[] bytes = writer.toString().getBytes(StandardCharsets.UTF_8);
        return new ExportResponse("dashboard_summary.csv", "text/csv", bytes);
    }

    private String escapeCsv(String val) {
        if (val == null) {
            return "";
        }
        return val.replace("\"", "\"\"");
    }
}
