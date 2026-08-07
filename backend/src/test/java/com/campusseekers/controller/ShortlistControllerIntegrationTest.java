package com.campusseekers.controller;

import com.campusseekers.dto.ApiResponse;
import com.campusseekers.dto.ShortlistRequest;
import com.campusseekers.dto.ShortlistResponse;
import com.campusseekers.service.ShortlistService;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ShortlistControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ShortlistService shortlistService;

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void addToShortlist_ShouldReturnCreated_WhenPayloadIsValid() throws Exception {
        UUID cbId = UUID.randomUUID();
        ShortlistRequest request = new ShortlistRequest(cbId, 1, "priority notes");

        ShortlistResponse mockResponse = new ShortlistResponse(
                UUID.randomUUID(), UUID.randomUUID(), cbId, UUID.randomUUID(), "1002", "COEP",
                UUID.randomUUID(), "CO", "Computers", "Pune", "MH", "A++",
                new BigDecimal("120000.00"), 1, "priority notes", false, Instant.now(), null
        );

        when(shortlistService.addToShortlist(any(ShortlistRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/shortlists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Shortlisted successfully"))
                .andExpect(jsonPath("$.data.branchName").value("Computers"));
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void importRecommendation_ShouldReturnCreated_WhenValid() throws Exception {
        UUID recItemId = UUID.randomUUID();

        ShortlistResponse mockResponse = new ShortlistResponse(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "1002", "COEP",
                UUID.randomUUID(), "CO", "Computers", "Pune", "MH", "A++",
                new BigDecimal("120000.00"), 1, "imported", false, Instant.now(), null
        );

        when(shortlistService.importRecommendationToShortlist(recItemId)).thenReturn(mockResponse);

        mockMvc.perform(post("/api/recommendations/" + recItemId + "/shortlist"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Recommendation imported successfully"));
    }
}
