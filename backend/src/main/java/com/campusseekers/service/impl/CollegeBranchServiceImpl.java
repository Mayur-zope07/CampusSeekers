package com.campusseekers.service.impl;

import com.campusseekers.dto.CollegeBranchRequest;
import com.campusseekers.dto.CollegeBranchResponse;
import com.campusseekers.entity.Branch;
import com.campusseekers.entity.College;
import com.campusseekers.entity.CollegeBranch;
import com.campusseekers.exception.DuplicateResourceException;
import com.campusseekers.exception.ResourceNotFoundException;
import com.campusseekers.mapper.CollegeBranchMapper;
import com.campusseekers.repository.BranchRepository;
import com.campusseekers.repository.CollegeBranchRepository;
import com.campusseekers.repository.CollegeRepository;
import com.campusseekers.service.CollegeBranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CollegeBranchServiceImpl implements CollegeBranchService {

    private final CollegeBranchRepository collegeBranchRepository;
    private final CollegeRepository collegeRepository;
    private final BranchRepository branchRepository;
    private final CollegeBranchMapper collegeBranchMapper;

    @Override
    @Transactional
    public CollegeBranchResponse createCollegeBranch(CollegeBranchRequest request) {
        College college = collegeRepository.findById(request.getCollegeId())
                .orElseThrow(() -> new ResourceNotFoundException("College not found with ID: " + request.getCollegeId()));

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with ID: " + request.getBranchId()));

        if (collegeBranchRepository.existsByCollegeIdAndBranchId(request.getCollegeId(), request.getBranchId())) {
            throw new DuplicateResourceException("Mapping already exists for college '" + college.getName() + "' and branch '" + branch.getName() + "'");
        }

        CollegeBranch collegeBranch = collegeBranchMapper.toEntity(request);
        collegeBranch.setCollege(college);
        collegeBranch.setBranch(branch);

        CollegeBranch savedMapping = collegeBranchRepository.save(collegeBranch);
        return collegeBranchMapper.toResponse(savedMapping);
    }

    @Override
    @Transactional
    public CollegeBranchResponse updateCollegeBranch(UUID id, CollegeBranchRequest request) {
        CollegeBranch collegeBranch = collegeBranchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("College-Branch mapping not found with ID: " + id));

        College college = collegeRepository.findById(request.getCollegeId())
                .orElseThrow(() -> new ResourceNotFoundException("College not found with ID: " + request.getCollegeId()));

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with ID: " + request.getBranchId()));

        // If college or branch changes, verify uniqueness of the new combination
        if ((!collegeBranch.getCollege().getId().equals(request.getCollegeId()) || !collegeBranch.getBranch().getId().equals(request.getBranchId()))
                && collegeBranchRepository.existsByCollegeIdAndBranchId(request.getCollegeId(), request.getBranchId())) {
            throw new DuplicateResourceException("Mapping already exists for college '" + college.getName() + "' and branch '" + branch.getName() + "'");
        }

        collegeBranchMapper.updateCollegeBranchFromRequest(request, collegeBranch);
        collegeBranch.setCollege(college);
        collegeBranch.setBranch(branch);

        CollegeBranch updatedMapping = collegeBranchRepository.save(collegeBranch);
        return collegeBranchMapper.toResponse(updatedMapping);
    }

    @Override
    @Transactional
    public void deleteCollegeBranch(UUID id) {
        CollegeBranch collegeBranch = collegeBranchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("College-Branch mapping not found with ID: " + id));
        collegeBranchRepository.delete(collegeBranch);
    }

    @Override
    @Transactional(readOnly = true)
    public CollegeBranchResponse getCollegeBranchById(UUID id) {
        CollegeBranch collegeBranch = collegeBranchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("College-Branch mapping not found with ID: " + id));
        return collegeBranchMapper.toResponse(collegeBranch);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollegeBranchResponse> getAllCollegeBranches() {
        List<CollegeBranch> mappings = collegeBranchRepository.findAll();
        return collegeBranchMapper.toResponseList(mappings);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollegeBranchResponse> getCollegeBranchesByCollege(UUID collegeId) {
        List<CollegeBranch> mappings = collegeBranchRepository.findByCollegeId(collegeId);
        return collegeBranchMapper.toResponseList(mappings);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollegeBranchResponse> getCollegeBranchesByBranch(UUID branchId) {
        List<CollegeBranch> mappings = collegeBranchRepository.findByBranchId(branchId);
        return collegeBranchMapper.toResponseList(mappings);
    }
}
