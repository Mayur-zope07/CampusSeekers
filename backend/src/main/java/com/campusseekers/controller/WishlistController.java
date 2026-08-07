package com.campusseekers.controller;

import com.campusseekers.dto.ApiResponse;
import com.campusseekers.dto.PageResponse;
import com.campusseekers.dto.WishlistRequest;
import com.campusseekers.dto.WishlistResponse;
import com.campusseekers.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Wishlist Management", description = "Endpoints for managing student favorite colleges (wishlist)")
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Add college to wishlist", description = "Saves a college to the authenticated student's favorites.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Added to wishlist successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid payload"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "College already in wishlist")
    })
    public ResponseEntity<ApiResponse<WishlistResponse>> addToWishlist(@Valid @RequestBody WishlistRequest request) {
        WishlistResponse response = wishlistService.addToWishlist(request);
        return new ResponseEntity<>(ApiResponse.<WishlistResponse>builder()
                .success(true)
                .message("Added to wishlist successfully")
                .data(response)
                .build(), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Search and list wishlist", description = "Retrieves a paginated, filterable list of active wishlist items.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Wishlist retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<PageResponse<WishlistResponse>>> getWishlist(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String naac,
            Pageable pageable) {
        Page<WishlistResponse> page = wishlistService.searchWishlist(keyword, naac, pageable);
        return ResponseEntity.ok(ApiResponse.<PageResponse<WishlistResponse>>builder()
                .success(true)
                .message("Wishlist retrieved successfully")
                .data(PageResponse.from(page))
                .build());
    }

    @PutMapping("/{id}/restore")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Restore deleted wishlist entry", description = "Restores a soft-deleted wishlist entry back to active status.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Wishlist entry restored successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Wishlist entry not found")
    })
    public ResponseEntity<ApiResponse<WishlistResponse>> restoreWishlist(@PathVariable UUID id) {
        WishlistResponse response = wishlistService.restoreWishlist(id);
        return ResponseEntity.ok(ApiResponse.<WishlistResponse>builder()
                .success(true)
                .message("Wishlist entry restored successfully")
                .data(response)
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Soft-delete wishlist entry", description = "Soft-deletes a wishlist entry from the student's dashboard.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Wishlist entry deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Wishlist entry not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteWishlist(@PathVariable UUID id) {
        wishlistService.removeFromWishlist(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Wishlist entry deleted successfully")
                .build());
    }
}
