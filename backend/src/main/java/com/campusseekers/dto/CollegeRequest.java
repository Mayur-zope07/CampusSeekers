package com.campusseekers.dto;

import com.campusseekers.entity.CollegeStatus;
import com.campusseekers.entity.CollegeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollegeRequest {

    @NotBlank(message = "College name is required")
    @Size(max = 255, message = "College name cannot exceed 255 characters")
    private String name;

    @NotBlank(message = "College code is required")
    @Size(max = 50, message = "College code cannot exceed 50 characters")
    private String collegeCode;

    @NotNull(message = "College type is required")
    private CollegeType collegeType;

    @NotNull(message = "Establishment year is required")
    @Positive(message = "Establishment year must be positive")
    private Integer establishmentYear;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 100, message = "State cannot exceed 100 characters")
    private String state;

    @Size(max = 255, message = "Website URL cannot exceed 255 characters")
    @URL(message = "Invalid website URL")
    private String website;

    @Size(max = 10, message = "NAAC Grade cannot exceed 10 characters")
    private String naacGrade;

    @NotNull(message = "NBA accreditation status is required")
    private Boolean nbaAccredited;

    @Size(max = 50, message = "Campus size description cannot exceed 50 characters")
    private String campusSize;

    @Size(max = 255, message = "Logo URL cannot exceed 255 characters")
    @URL(message = "Invalid logo URL")
    private String logoUrl;

    @NotNull(message = "Status is required")
    private CollegeStatus status;
}
