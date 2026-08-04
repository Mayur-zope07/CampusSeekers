package com.campusseekers.service;

import com.campusseekers.dto.PlacementRequest;
import com.campusseekers.dto.PlacementResponse;

import java.util.List;
import java.util.UUID;

public interface PlacementService {
    PlacementResponse createPlacement(PlacementRequest request);
    PlacementResponse updatePlacement(UUID id, PlacementRequest request);
    void deletePlacement(UUID id);
    PlacementResponse getPlacementById(UUID id);
    List<PlacementResponse> getAllPlacements();
    List<PlacementResponse> getPlacementsByCollege(UUID collegeId);
}
