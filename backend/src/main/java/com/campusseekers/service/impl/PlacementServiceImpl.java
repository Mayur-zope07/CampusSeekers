package com.campusseekers.service.impl;

import com.campusseekers.dto.PlacementRequest;
import com.campusseekers.dto.PlacementResponse;
import com.campusseekers.entity.College;
import com.campusseekers.entity.Placement;
import com.campusseekers.exception.DuplicateResourceException;
import com.campusseekers.exception.ResourceNotFoundException;
import com.campusseekers.mapper.PlacementMapper;
import com.campusseekers.repository.CollegeRepository;
import com.campusseekers.repository.PlacementRepository;
import com.campusseekers.service.PlacementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlacementServiceImpl implements PlacementService {

    private final PlacementRepository placementRepository;
    private final CollegeRepository collegeRepository;
    private final PlacementMapper placementMapper;

    @Override
    @Transactional
    public PlacementResponse createPlacement(PlacementRequest request) {
        College college = collegeRepository.findById(request.getCollegeId())
                .orElseThrow(() -> new ResourceNotFoundException("College not found with ID: " + request.getCollegeId()));

        if (placementRepository.existsByCollegeIdAndYear(request.getCollegeId(), request.getYear())) {
            throw new DuplicateResourceException("Placement record already exists for college '" + college.getName() + "' and year " + request.getYear());
        }

        Placement placement = placementMapper.toEntity(request);
        placement.setCollege(college);

        Placement savedPlacement = placementRepository.save(placement);
        return placementMapper.toResponse(savedPlacement);
    }

    @Override
    @Transactional
    public PlacementResponse updatePlacement(UUID id, PlacementRequest request) {
        Placement placement = placementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Placement record not found with ID: " + id));

        College college = collegeRepository.findById(request.getCollegeId())
                .orElseThrow(() -> new ResourceNotFoundException("College not found with ID: " + request.getCollegeId()));

        // If college or year changes, verify uniqueness
        if ((!placement.getCollege().getId().equals(request.getCollegeId()) || !placement.getYear().equals(request.getYear()))
                && placementRepository.existsByCollegeIdAndYear(request.getCollegeId(), request.getYear())) {
            throw new DuplicateResourceException("Placement record already exists for college '" + college.getName() + "' and year " + request.getYear());
        }

        placementMapper.updatePlacementFromRequest(request, placement);
        placement.setCollege(college);

        Placement updatedPlacement = placementRepository.save(placement);
        return placementMapper.toResponse(updatedPlacement);
    }

    @Override
    @Transactional
    public void deletePlacement(UUID id) {
        Placement placement = placementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Placement record not found with ID: " + id));
        placementRepository.delete(placement);
    }

    @Override
    @Transactional(readOnly = true)
    public PlacementResponse getPlacementById(UUID id) {
        Placement placement = placementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Placement record not found with ID: " + id));
        return placementMapper.toResponse(placement);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlacementResponse> getAllPlacements() {
        List<Placement> placements = placementRepository.findAll();
        return placementMapper.toResponseList(placements);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlacementResponse> getPlacementsByCollege(UUID collegeId) {
        List<Placement> placements = placementRepository.findByCollegeIdOrderByYearDesc(collegeId);
        return placementMapper.toResponseList(placements);
    }
}
