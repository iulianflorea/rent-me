package com.singularity.rentit.controller;

import com.singularity.rentit.dto.request.CreateReviewRequest;
import com.singularity.rentit.dto.response.ReviewResponse;
import com.singularity.rentit.entity.User;
import com.singularity.rentit.service.ReviewService;
import com.singularity.rentit.service.UserService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateReviewRequest request
    ) {
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.createReview(request, user.getId()));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<ReviewResponse>> getReviewsForUser(
            @PathVariable Long userId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(reviewService.getReviewsForUser(userId, pageable));
    }
}
