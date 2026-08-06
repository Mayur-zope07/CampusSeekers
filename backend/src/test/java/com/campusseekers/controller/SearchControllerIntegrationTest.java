package com.campusseekers.controller;

import com.campusseekers.dto.CollegeDetailsResponse;
import com.campusseekers.dto.ComparisonResponse;
import com.campusseekers.dto.PageResponse;
import com.campusseekers.entity.Category;
import com.campusseekers.entity.CollegeStatus;
import com.campusseekers.entity.CollegeType;
import com.campusseekers.entity.ExamName;
import com.campusseekers.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SearchControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CollegeSearchService collegeSearchService;
    @MockBean
    private BranchSearchService branchSearchService;
    @MockBean
    private CutoffSearchService cutoffSearchService;
    @MockBean
    private PercentileSearchService percentileSearchService;
    @MockBean
    private CollegeComparisonService collegeComparisonService;
    @MockBean
    private PlacementSearchService placementSearchService;

    @Test
    void searchColleges_ShouldReturnUnauthorized_WhenAnonymous() throws Exception {
        mockMvc.perform(get("/api/colleges"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void searchColleges_ShouldReturnOk_WhenStudent() throws Exception {
        PageResponse<com.campusseekers.dto.CollegeListResponse> mockPage = PageResponse.<com.campusseekers.dto.CollegeListResponse>builder()
                .content(Collections.emptyList())
                .build();

        when(collegeSearchService.searchColleges(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(mockPage);

        mockMvc.perform(get("/api/colleges?name=GCOEA&type=GOVERNMENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void getCollegeDetails_ShouldReturnOk_WhenAdmin() throws Exception {
        UUID id = UUID.randomUUID();
        when(collegeSearchService.getCollegeDetails(id)).thenReturn(new CollegeDetailsResponse());

        mockMvc.perform(get("/api/colleges/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void compareColleges_ShouldReturnBadRequest_WhenInsufficientColleges() throws Exception {
        mockMvc.perform(get("/api/compare?collegeIds=" + UUID.randomUUID()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void compareColleges_ShouldReturnOk_WhenValidCollegesCount() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        when(collegeComparisonService.compareColleges(anyList())).thenReturn(new ComparisonResponse());

        mockMvc.perform(get("/api/compare?collegeIds=" + id1 + "," + id2))
                .andExpect(status().isOk());
    }
}
