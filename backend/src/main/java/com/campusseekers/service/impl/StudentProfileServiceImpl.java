package com.campusseekers.service.impl;

import com.campusseekers.dto.StudentProfileRequest;
import com.campusseekers.dto.StudentProfileResponse;
import com.campusseekers.entity.StudentProfile;
import com.campusseekers.entity.User;
import com.campusseekers.exception.DuplicateResourceException;
import com.campusseekers.exception.ResourceNotFoundException;
import com.campusseekers.exception.UnauthorizedException;
import com.campusseekers.mapper.StudentProfileMapper;
import com.campusseekers.repository.StudentProfileRepository;
import com.campusseekers.repository.UserRepository;
import com.campusseekers.service.StudentProfileService;
import com.campusseekers.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentProfileServiceImpl implements StudentProfileService {

    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;
    private final StudentProfileMapper studentProfileMapper;

    @Override
    @Transactional
    public StudentProfileResponse createProfile(StudentProfileRequest request) {
        User user = getAuthenticatedUser();

        if (studentProfileRepository.existsByUser(user)) {
            throw new DuplicateResourceException("Student profile already exists for this user");
        }

        StudentProfile profile = studentProfileMapper.toEntity(request);
        profile.setUser(user);

        StudentProfile savedProfile = studentProfileRepository.save(profile);
        return studentProfileMapper.toResponse(savedProfile);
    }

    @Override
    @Transactional
    public StudentProfileResponse updateProfile(StudentProfileRequest request) {
        User user = getAuthenticatedUser();

        StudentProfile profile = studentProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for this user"));

        studentProfileMapper.updateProfileFromRequest(request, profile);

        StudentProfile updatedProfile = studentProfileRepository.save(profile);
        return studentProfileMapper.toResponse(updatedProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentProfileResponse getCurrentProfile() {
        User user = getAuthenticatedUser();

        StudentProfile profile = studentProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for this user"));

        return studentProfileMapper.toResponse(profile);
    }

    private User getAuthenticatedUser() {
        String email = SecurityUtils.getCurrentUserEmail()
                .orElseThrow(() -> new UnauthorizedException("User is not authenticated"));

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }
}
