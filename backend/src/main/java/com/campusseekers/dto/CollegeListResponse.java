package com.campusseekers.dto;

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
public class CollegeListResponse {
    private UUID id;
    private String collegeCode;
    private String name;
    private String city;
    private String state;
    private CollegeType collegeType;
    private String naac;
    private Boolean nba;
    private String logo;
}
