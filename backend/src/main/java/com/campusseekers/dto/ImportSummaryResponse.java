package com.campusseekers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportSummaryResponse {
    private String status;
    private String executionTime;
    private int datasetsImported;
    private int rowsProcessed;
    private int rowsInserted;
    private int rowsUpdated;
    private int rowsSkipped;
    private int duplicateRows;
    @Builder.Default
    private List<String> validationErrors = new ArrayList<>();
    @Builder.Default
    private List<String> warnings = new ArrayList<>();
    
    private Map<String, DatasetStats> datasetDetails;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DatasetStats {
        private int processed;
        private int inserted;
        private int updated;
        private int skipped;
        private int duplicate;
    }
}
