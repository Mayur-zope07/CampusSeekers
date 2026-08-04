package com.campusseekers.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "student_profiles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name cannot exceed 100 characters")
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    @Column(name = "phone", length = 20)
    private String phone;

    @NotBlank(message = "Gender is required")
    @Size(max = 20, message = "Gender cannot exceed 20 characters")
    @Column(name = "gender", nullable = false, length = 20)
    private String gender;

    @NotBlank(message = "Category is required")
    @Size(max = 50, message = "Category cannot exceed 50 characters")
    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Size(max = 50, message = "Sub-category cannot exceed 50 characters")
    @Column(name = "sub_category", length = 50)
    private String subCategory;

    @NotBlank(message = "Home state is required")
    @Size(max = 100, message = "Home state cannot exceed 100 characters")
    @Column(name = "home_state", nullable = false, length = 100)
    private String homeState;

    @NotBlank(message = "Home district is required")
    @Size(max = 100, message = "Home district cannot exceed 100 characters")
    @Column(name = "home_district", nullable = false, length = 100)
    private String homeDistrict;

    @Builder.Default
    @OneToMany(mappedBy = "studentProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ExamScore> examScores = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "studentProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Shortlist> shortlists = new ArrayList<>();
}
