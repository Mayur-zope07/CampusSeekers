package com.campusseekers.service;

import com.campusseekers.dto.StudentProfileRequest;
import com.campusseekers.dto.StudentProfileResponse;

public interface StudentProfileService {
    StudentProfileResponse createProfile(StudentProfileRequest request);
    StudentProfileResponse updateProfile(StudentProfileRequest request);
    StudentProfileResponse getCurrentProfile();
}
