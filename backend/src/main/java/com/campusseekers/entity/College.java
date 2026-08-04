package com.campusseekers.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "colleges")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class College extends BaseEntity {

    @NotBlank(message = "College name is required")
    @Size(max = 255, message = "College name cannot exceed 255 characters")
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @NotBlank(message = "College code is required")
    @Size(max = 50, message = "College code cannot exceed 50 characters")
    @Column(name = "college_code", unique = true, nullable = false, length = 50)
    private String collegeCode;

    @NotBlank(message = "College type is required")
    @Size(max = 50, message = "College type cannot exceed 50 characters")
    @Column(name = "college_type", nullable = false, length = 50)
    private String collegeType;

    @NotNull(message = "Establishment year is required")
    @Min(value = 1800, message = "Establishment year must be realistic")
    @Column(name = "establishment_year", nullable = false)
    private Integer establishmentYear;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City cannot exceed 100 characters")
    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 100, message = "State cannot exceed 100 characters")
    @Column(name = "state", nullable = false, length = 100)
    private String state;

    @Size(max = 255, message = "Website URL cannot exceed 255 characters")
    @Column(name = "website", length = 255)
    private String website;

    @Size(max = 10, message = "NAAC Grade cannot exceed 10 characters")
    @Column(name = "naac_grade", length = 10)
    private String naacGrade;

    @NotNull(message = "NBA accreditation status is required")
    @Column(name = "nba_accredited", nullable = false)
    @Builder.Default
    private Boolean nbaAccredited = false;

    @Size(max = 50, message = "Campus size description cannot exceed 50 characters")
    @Column(name = "campus_size", length = 50)
    private String campusSize;

    @Size(max = 255, message = "Logo URL cannot exceed 255 characters")
    @Column(name = "logo_url", length = 255)
    private String logoUrl;

    @Builder.Default
    @OneToMany(mappedBy = "college", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<CollegeBranch> collegeBranches = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "college", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Placement> placements = new ArrayList<>();
}
