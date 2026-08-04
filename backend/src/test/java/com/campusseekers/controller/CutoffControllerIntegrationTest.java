package com.campusseekers.controller;

import com.campusseekers.dto.CutoffRequest;
import com.campusseekers.dto.CutoffResponse;
import com.campusseekers.entity.Category;
import com.campusseekers.entity.ExamName;
import com.campusseekers.entity.SeatType;
import com.campusseekers.service.CutoffService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CutoffControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CutoffService cutoffService;

    @Test
    void getCutoffs_ShouldReturnUnauthorized_WhenTokenIsMissing() throws Exception {
        mockMvc.perform(get("/api/cutoffs"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void getCutoffs_ShouldReturnList_WhenStudent() throws Exception {
        CutoffResponse mockResponse = CutoffResponse.builder()
                .id(UUID.randomUUID())
                .collegeBranchId(UUID.randomUUID())
                .collegeName("COEP")
                .branchName("CS")
                .examName(ExamName.MHT_CET)
                .year(2026)
                .round(1)
                .category(Category.OPEN)
                .seatType(SeatType.GOPENS)
                .closingRank(1250)
                .closingPercentile(new BigDecimal("98.45"))
                .build();

        when(cutoffService.getAllCutoffs()).thenReturn(List.of(mockResponse));

        mockMvc.perform(get("/api/cutoffs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].examName").value("MHT_CET"));
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void postCutoff_ShouldReturnForbidden_WhenStudent() throws Exception {
        CutoffRequest request = CutoffRequest.builder()
                .collegeBranchId(UUID.randomUUID())
                .examName(ExamName.MHT_CET)
                .year(2026)
                .round(1)
                .category(Category.OPEN)
                .seatType(SeatType.GOPENS)
                .closingRank(1250)
                .closingPercentile(new BigDecimal("98.45"))
                .build();

        mockMvc.perform(post("/api/admin/cutoffs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void postCutoff_ShouldReturnCreated_WhenAdminAndPayloadIsValid() throws Exception {
        CutoffRequest request = CutoffRequest.builder()
                .collegeBranchId(UUID.randomUUID())
                .examName(ExamName.MHT_CET)
                .year(2026)
                .round(1)
                .category(Category.OPEN)
                .seatType(SeatType.GOPENS)
                .closingRank(1250)
                .closingPercentile(new BigDecimal("98.45"))
                .build();

        CutoffResponse response = CutoffResponse.builder()
                .examName(ExamName.MHT_CET)
                .build();

        when(cutoffService.createCutoff(any(CutoffRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/admin/cutoffs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cutoff record created successfully"));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void deleteCutoff_ShouldReturnOk_WhenAdmin() throws Exception {
        UUID cutoffId = UUID.randomUUID();
        doNothing().when(cutoffService).deleteCutoff(cutoffId);

        mockMvc.perform(delete("/api/admin/cutoffs/" + cutoffId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cutoff record deleted successfully"));

        verify(cutoffService, times(1)).deleteCutoff(cutoffId);
    }
}
