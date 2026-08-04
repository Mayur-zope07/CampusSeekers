package com.campusseekers.dto;

import com.campusseekers.entity.CollegeStatus;
import com.campusseekers.entity.CollegeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollegeResponse {
    private UUID id;
    private String name;
    private String collegeCode;
    private CollegeType collegeType;
    private Integer establishmentYear;
    private String city;
    private String state;
    private String website;
    private String naacGrade;
    private Boolean nbaAccredited;
    private String campusSize;
    private String logoUrl;
    private CollegeStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
