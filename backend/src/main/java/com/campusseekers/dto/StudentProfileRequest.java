package com.campusseekers.dto;

import com.campusseekers.entity.Category;
import com.campusseekers.entity.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentProfileRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name cannot exceed 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    private String lastName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be a valid 10-digit number")
    private String phone;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotNull(message = "Category is required")
    private Category category;

    @Size(max = 50, message = "Sub-category cannot exceed 50 characters")
    private String subCategory;

    @NotBlank(message = "Home state is required")
    @Size(max = 100, message = "Home state cannot exceed 100 characters")
    private String homeState;

    @NotBlank(message = "Home district is required")
    @Size(max = 100, message = "Home district cannot exceed 100 characters")
    private String homeDistrict;
}
