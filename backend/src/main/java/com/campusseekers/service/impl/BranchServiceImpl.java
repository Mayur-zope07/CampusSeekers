package com.campusseekers.service.impl;

import com.campusseekers.dto.BranchRequest;
import com.campusseekers.dto.BranchResponse;
import com.campusseekers.entity.Branch;
import com.campusseekers.exception.DuplicateResourceException;
import com.campusseekers.exception.ResourceNotFoundException;
import com.campusseekers.mapper.BranchMapper;
import com.campusseekers.repository.BranchRepository;
import com.campusseekers.service.BranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;
    private final BranchMapper branchMapper;

    @Override
    @Transactional
    public BranchResponse createBranch(BranchRequest request) {
        if (branchRepository.existsByBranchCode(request.getBranchCode())) {
            throw new DuplicateResourceException("Branch with code '" + request.getBranchCode() + "' already exists");
        }

        Branch branch = branchMapper.toEntity(request);
        Branch savedBranch = branchRepository.save(branch);
        return branchMapper.toResponse(savedBranch);
    }

    @Override
    @Transactional
    public BranchResponse updateBranch(UUID id, BranchRequest request) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with ID: " + id));

        if (!branch.getBranchCode().equals(request.getBranchCode())
                && branchRepository.existsByBranchCode(request.getBranchCode())) {
            throw new DuplicateResourceException("Branch with code '" + request.getBranchCode() + "' already exists");
        }

        branchMapper.updateBranchFromRequest(request, branch);
        Branch updatedBranch = branchRepository.save(branch);
        return branchMapper.toResponse(updatedBranch);
    }

    @Override
    @Transactional
    public void deleteBranch(UUID id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with ID: " + id));
        branchRepository.delete(branch);
    }

    @Override
    @Transactional(readOnly = true)
    public BranchResponse getBranchById(UUID id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with ID: " + id));
        return branchMapper.toResponse(branch);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchResponse> getAllBranches() {
        List<Branch> branches = branchRepository.findAll();
        return branchMapper.toResponseList(branches);
    }
}
