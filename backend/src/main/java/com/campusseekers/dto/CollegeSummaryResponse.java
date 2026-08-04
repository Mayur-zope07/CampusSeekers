package com.campusseekers.dto;

import com.campusseekers.entity.CollegeStatus;
import com.campusseekers.entity.CollegeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollegeSummaryResponse {
    private UUID id;
    private String name;
    private String collegeCode;
    private CollegeType collegeType;
    private String city;
    private String state;
    private String logoUrl;
    private CollegeStatus status;
}
