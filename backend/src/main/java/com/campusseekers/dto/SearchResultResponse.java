package com.campusseekers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultResponse {
    private UUID collegeId;
    private String collegeCode;
    private String collegeName;
    private UUID branchId;
    private String branchName;
    private String branchCode;
    private String city;
    private String state;
    private BigDecimal studentPercentile;
    private BigDecimal closingPercentile;
    private BigDecimal difference;
    private String classification;
}
