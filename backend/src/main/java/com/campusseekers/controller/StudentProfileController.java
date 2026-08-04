package com.campusseekers.controller;

import com.campusseekers.dto.ApiResponse;
import com.campusseekers.dto.StudentProfileRequest;
import com.campusseekers.dto.StudentProfileResponse;
import com.campusseekers.service.StudentProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Student Profile", description = "Endpoints for managing the student profile")
public class StudentProfileController {

    private final StudentProfileService studentProfileService;

    @GetMapping
    @Operation(summary = "Get current student profile", description = "Fetch details of the student profile associated with the authenticated user.")
    public ResponseEntity<ApiResponse<StudentProfileResponse>> getCurrentProfile() {
        StudentProfileResponse response = studentProfileService.getCurrentProfile();
        return ResponseEntity.ok(ApiResponse.<StudentProfileResponse>builder()
                .success(true)
                .message("Student profile retrieved successfully")
                .data(response)
                .build());
    }

    @PostMapping
    @Operation(summary = "Create student profile", description = "Initialize a student profile for the logged-in user.")
    public ResponseEntity<ApiResponse<StudentProfileResponse>> createProfile(@Valid @RequestBody StudentProfileRequest request) {
        StudentProfileResponse response = studentProfileService.createProfile(request);
        return new ResponseEntity<>(ApiResponse.<StudentProfileResponse>builder()
                .success(true)
                .message("Student profile created successfully")
                .data(response)
                .build(), HttpStatus.CREATED);
    }

    @PutMapping
    @Operation(summary = "Update student profile", description = "Modify details of the student profile associated with the logged-in user.")
    public ResponseEntity<ApiResponse<StudentProfileResponse>> updateProfile(@Valid @RequestBody StudentProfileRequest request) {
        StudentProfileResponse response = studentProfileService.updateProfile(request);
        return ResponseEntity.ok(ApiResponse.<StudentProfileResponse>builder()
                .success(true)
                .message("Student profile updated successfully")
                .data(response)
                .build());
    }
}
