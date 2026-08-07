package com.campusseekers.service;

import com.campusseekers.dto.WishlistRequest;
import com.campusseekers.dto.WishlistResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface WishlistService {
    WishlistResponse addToWishlist(WishlistRequest request);
    void removeFromWishlist(UUID id);
    WishlistResponse restoreWishlist(UUID id);
    Page<WishlistResponse> searchWishlist(String keyword, String naac, Pageable pageable);
}
