package com.campusseekers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollegeImportDto {
    private String collegeCode;
    private String name;
    private String collegeType;
    private String establishmentYear;
    private String city;
    private String state;
    private String website;
    private String naacGrade;
    private String nbaAccredited;
    private String campusSize;
    private String logoUrl;
    private String status;
}
