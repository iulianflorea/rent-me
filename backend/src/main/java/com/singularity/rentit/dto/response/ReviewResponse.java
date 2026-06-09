package com.singularity.rentit.dto.response;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        Long rentalId,
        ReviewerSummary reviewer,
        int rating,
        String comment,
        LocalDateTime createdAt
) {
    public record ReviewerSummary(Long id, String firstName, String lastName, boolean kycVerified) {}
}
