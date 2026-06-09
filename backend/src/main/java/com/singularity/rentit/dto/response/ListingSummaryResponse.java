package com.singularity.rentit.dto.response;

import com.singularity.rentit.enums.CategoryType;
import com.singularity.rentit.enums.ListingStatus;

import java.math.BigDecimal;

public record ListingSummaryResponse(
        Long id,
        String title,
        CategoryType category,
        ListingStatus status,
        BigDecimal pricePerDay,
        String city,
        String county,
        Double latitude,
        Double longitude,
        String firstImageUrl,
        OwnerSummary owner,
        Double distanceKm
) {
    public record OwnerSummary(
            Long id,
            String firstName,
            String lastName,
            String kycStatus,
            Double averageRating,
            long reviewCount
    ) {}
}
