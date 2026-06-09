package com.singularity.rentit.controller;

import com.singularity.rentit.dto.response.WishlistResponse;
import com.singularity.rentit.entity.User;
import com.singularity.rentit.service.UserService;
import com.singularity.rentit.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<WishlistResponse>> getWishlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(wishlistService.getWishlist(user.getId(), pageable));
    }

    @PostMapping("/{listingId}")
    public ResponseEntity<Void> addToWishlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long listingId
    ) {
        User user = userService.findByEmail(userDetails.getUsername());
        wishlistService.addToWishlist(user.getId(), listingId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{listingId}")
    public ResponseEntity<Void> removeFromWishlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long listingId
    ) {
        User user = userService.findByEmail(userDetails.getUsername());
        wishlistService.removeFromWishlist(user.getId(), listingId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{listingId}/check")
    public ResponseEntity<Boolean> checkWishlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long listingId
    ) {
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(wishlistService.isInWishlist(user.getId(), listingId));
    }
}
