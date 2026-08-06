package com.campusseekers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatMatrixImportDto {
    private String collegeCode;
    private String branchCode;
    private String intakeCapacity;
}
