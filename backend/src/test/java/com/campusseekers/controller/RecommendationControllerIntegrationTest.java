package com.campusseekers.controller;

import com.campusseekers.dto.*;
import com.campusseekers.entity.Category;
import com.campusseekers.entity.ExamName;
import com.campusseekers.service.RecommendationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RecommendationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RecommendationService recommendationService;

    @Test
    void endpoints_ShouldReturnUnauthorized_WhenTokenIsMissing() throws Exception {
        mockMvc.perform(post("/api/recommendations"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/recommendations/history"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void generateRecommendations_ShouldReturnCreated_WhenPayloadIsValid() throws Exception {
        RecommendationRequest request = new RecommendationRequest(
                ExamName.MHT_CET, 2025, new BigDecimal("95.00"), 123, Category.OPEN,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null, null
        );

        RecommendationResponse mockResponse = new RecommendationResponse(
                UUID.randomUUID(), UUID.randomUUID(), ExamName.MHT_CET, 2025,
                new BigDecimal("95.00"), 123, Category.OPEN,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                null, null, 15, 100, 20, 20, 10, 5, 5,
                "1.0", "historical-cutoff-v1",
                new BigDecimal("3.0"), new BigDecimal("1.5"), new BigDecimal("0.0"),
                false, Instant.now(), Collections.emptyList()
        );

        when(recommendationService.generateRecommendations(any(RecommendationRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Recommendations generated successfully"))
                .andExpect(jsonPath("$.data.examName").value("MHT_CET"));
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void generateRecommendations_ShouldReturnBadRequest_WhenPercentileIsInvalid() throws Exception {
        RecommendationRequest request = new RecommendationRequest(
                ExamName.MHT_CET, 2025, new BigDecimal("105.00"), 123, Category.OPEN,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null, null
        );

        mockMvc.perform(post("/api/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.validationErrors.percentile").exists());
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void getRecommendationHistory_ShouldReturnHistory_WhenAuthenticated() throws Exception {
        RecommendationHistoryResponse historyItem = new RecommendationHistoryResponse(
                UUID.randomUUID(), ExamName.MHT_CET, 2025, new BigDecimal("95.00"), 123, Category.OPEN,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null, null,
                15, 10, false, Instant.now()
        );

        when(recommendationService.getRecommendationHistory(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(historyItem)));

        mockMvc.perform(get("/api/recommendations/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].examName").value("MHT_CET"));
    }
}
