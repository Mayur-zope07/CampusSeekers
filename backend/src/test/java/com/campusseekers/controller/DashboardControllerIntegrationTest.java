package com.campusseekers.controller;

import com.campusseekers.dto.ApiResponse;
import com.campusseekers.dto.DashboardResponse;
import com.campusseekers.dto.DashboardStatisticsResponse;
import com.campusseekers.service.DashboardService;
import com.campusseekers.service.DashboardStatisticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DashboardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @MockBean
    private DashboardStatisticsService dashboardStatisticsService;

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void getDashboard_ShouldReturnDashboard_WhenAuthenticated() throws Exception {
        DashboardStatisticsResponse mockStats = new DashboardStatisticsResponse(
                5, 2, 10, 4, 3, 3, 1, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN, new HashMap<>()
        );
        DashboardResponse mockResponse = new DashboardResponse(
                mockStats, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList()
        );

        when(dashboardService.getDashboard()).thenReturn(mockResponse);

        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.statistics.wishlistCount").value(5));
    }
}
