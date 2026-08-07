package com.campusseekers.service;

import com.campusseekers.dto.RecommendationRequest;
import com.campusseekers.entity.Category;
import com.campusseekers.entity.ExamName;
import com.campusseekers.event.RecommendationGeneratedEvent;
import com.campusseekers.event.RecommendationViewedEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RecommendationEventsTest {

    @Test
    void testRecommendationGeneratedEvent() {
        UUID recId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        RecommendationRequest request = new RecommendationRequest(
                ExamName.MHT_CET, 2025, new BigDecimal("95.00"), null, Category.OPEN,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null, null
        );
        Instant now = Instant.now();

        RecommendationGeneratedEvent event = new RecommendationGeneratedEvent(recId, studentId, request, now);

        assertNotNull(event);
        assertEquals(recId, event.recommendationId());
        assertEquals(studentId, event.studentProfileId());
        assertEquals(request, event.request());
        assertEquals(now, event.timestamp());
    }

    @Test
    void testRecommendationViewedEvent() {
        UUID recId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();

        RecommendationViewedEvent event = new RecommendationViewedEvent(recId, userId, now);

        assertNotNull(event);
        assertEquals(recId, event.recommendationId());
        assertEquals(userId, event.userId());
        assertEquals(now, event.timestamp());
    }
}
