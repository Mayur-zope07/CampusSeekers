package com.campusseekers.model;

import com.campusseekers.config.RecommendationProperties;
import com.campusseekers.dto.RecommendationRequest;
import com.campusseekers.entity.Cutoff;
import com.campusseekers.entity.Placement;
import com.campusseekers.entity.StudentProfile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record RecommendationContext(
        StudentProfile studentProfile,
        RecommendationRequest request,
        RecommendationProperties configuration,
        List<Cutoff> cutoffs,
        List<Placement> placements,
        Map<UUID, Placement> placementsMap
) {}
