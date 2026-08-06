package com.campusseekers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CutoffImportDto {
    private String collegeCode;
    private String branchCode;
    private String examName;
    private String year;
    private String round;
    private String category;
    private String rawSeatType;
    private String closingRank;
    private String closingPercentile;
}
