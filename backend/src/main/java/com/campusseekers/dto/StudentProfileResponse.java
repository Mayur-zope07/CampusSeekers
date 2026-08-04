package com.campusseekers.dto;

import com.campusseekers.entity.Category;
import com.campusseekers.entity.Gender;
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
public class StudentProfileResponse {
    private UUID id;
    private UUID userId;
    private String firstName;
    private String lastName;
    private String phone;
    private Gender gender;
    private Category category;
    private String subCategory;
    private String homeState;
    private String homeDistrict;
    private Instant createdAt;
    private Instant updatedAt;
}
