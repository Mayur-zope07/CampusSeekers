package com.campusseekers.service.impl;

import com.campusseekers.dto.CollegeRequest;
import com.campusseekers.dto.CollegeResponse;
import com.campusseekers.entity.College;
import com.campusseekers.exception.DuplicateResourceException;
import com.campusseekers.exception.ResourceNotFoundException;
import com.campusseekers.mapper.CollegeMapper;
import com.campusseekers.repository.CollegeRepository;
import com.campusseekers.service.CollegeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CollegeServiceImpl implements CollegeService {

    private final CollegeRepository collegeRepository;
    private final CollegeMapper collegeMapper;

    @Override
    @Transactional
    public CollegeResponse createCollege(CollegeRequest request) {
        if (collegeRepository.existsByCollegeCode(request.getCollegeCode())) {
            throw new DuplicateResourceException("College with code '" + request.getCollegeCode() + "' already exists");
        }

        College college = collegeMapper.toEntity(request);
        College savedCollege = collegeRepository.save(college);
        return collegeMapper.toResponse(savedCollege);
    }

    @Override
    @Transactional
    public CollegeResponse updateCollege(UUID id, CollegeRequest request) {
        College college = collegeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("College not found with ID: " + id));

        if (!college.getCollegeCode().equals(request.getCollegeCode())
                && collegeRepository.existsByCollegeCode(request.getCollegeCode())) {
            throw new DuplicateResourceException("College with code '" + request.getCollegeCode() + "' already exists");
        }

        collegeMapper.updateCollegeFromRequest(request, college);
        College updatedCollege = collegeRepository.save(college);
        return collegeMapper.toResponse(updatedCollege);
    }

    @Override
    @Transactional
    public void deleteCollege(UUID id) {
        College college = collegeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("College not found with ID: " + id));
        collegeRepository.delete(college);
    }

    @Override
    @Transactional(readOnly = true)
    public CollegeResponse getCollegeById(UUID id) {
        College college = collegeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("College not found with ID: " + id));
        return collegeMapper.toResponse(college);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollegeResponse> getAllColleges() {
        List<College> colleges = collegeRepository.findAll();
        return collegeMapper.toResponseList(colleges);
    }
}
