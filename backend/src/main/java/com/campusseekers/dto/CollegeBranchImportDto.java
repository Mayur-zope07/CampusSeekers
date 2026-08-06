package com.campusseekers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollegeBranchImportDto {
    private String collegeCode;
    private String branchCode;
    private String intakeCapacity;
    private String feesPerYear;
    private String durationYears;
}
