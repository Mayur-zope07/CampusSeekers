package com.campusseekers.controller;

import com.campusseekers.dto.ApiResponse;
import com.campusseekers.dto.WishlistRequest;
import com.campusseekers.dto.WishlistResponse;
import com.campusseekers.service.WishlistService;
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
class WishlistControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WishlistService wishlistService;

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void addToWishlist_ShouldReturnCreated_WhenPayloadIsValid() throws Exception {
        UUID collegeId = UUID.randomUUID();
        WishlistRequest request = new WishlistRequest(collegeId, "my notes");

        WishlistResponse mockResponse = new WishlistResponse(
                UUID.randomUUID(), UUID.randomUUID(), collegeId, "1002", "COEP", "Pune", "MH", "A++", Instant.now(), false
        );

        when(wishlistService.addToWishlist(any(WishlistRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/wishlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Added to wishlist successfully"))
                .andExpect(jsonPath("$.data.collegeName").value("COEP"));
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void getWishlist_ShouldReturnList_WhenAuthenticated() throws Exception {
        WishlistResponse mockItem = new WishlistResponse(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "1002", "COEP", "Pune", "MH", "A++", Instant.now(), false
        );

        when(wishlistService.searchWishlist(any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(mockItem)));

        mockMvc.perform(get("/api/wishlist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].collegeName").value("COEP"));
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void deleteWishlist_ShouldReturnOk_WhenAuthorized() throws Exception {
        UUID itemId = UUID.randomUUID();
        doNothing().when(wishlistService).removeFromWishlist(itemId);

        mockMvc.perform(delete("/api/wishlist/" + itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Wishlist entry deleted successfully"));
    }
}
