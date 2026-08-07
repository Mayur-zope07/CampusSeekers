package com.campusseekers.service;

import com.campusseekers.config.RecommendationProperties;
import com.campusseekers.dto.RecommendationRequest;
import com.campusseekers.entity.Category;
import com.campusseekers.entity.ExamName;
import com.campusseekers.entity.Placement;
import com.campusseekers.entity.StudentProfile;
import com.campusseekers.model.RecommendationContext;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RecommendationContextTest {

    @Test
    void testContextCreationAndAccessors() {
        StudentProfile profile = new StudentProfile();
        RecommendationRequest request = new RecommendationRequest(
                ExamName.MHT_CET, 2025, new BigDecimal("95.50"), 1234, Category.OPEN,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), "A", BigDecimal.ZERO
        );
        RecommendationProperties props = new RecommendationProperties();
        Map<UUID, Placement> placementsMap = new HashMap<>();

        RecommendationContext context = new RecommendationContext(
                profile, request, props, Collections.emptyList(), Collections.emptyList(), placementsMap
        );

        assertNotNull(context);
        assertEquals(profile, context.studentProfile());
        assertEquals(request, context.request());
        assertEquals(props, context.configuration());
        assertEquals(placementsMap, context.placementsMap());
    }
}
