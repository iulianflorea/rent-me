package com.singularity.rentit.dto.response;

import com.singularity.rentit.enums.CategoryType;
import com.singularity.rentit.enums.ListingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ListingResponse(
        Long id,
        String title,
        String description,
        CategoryType category,
        ListingStatus status,
        BigDecimal pricePerDay,
        BigDecimal pricePerWeek,
        BigDecimal pricePerMonth,
        BigDecimal guaranteeAmount,
        String address,
        String city,
        String county,
        Double latitude,
        Double longitude,
        String categoryAttributes,
        List<ImageDto> images,
        String firstImageUrl,
        OwnerSummary owner,
        int viewsCount,
        Double distanceKm,
        boolean inWishlist,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record ImageDto(Long id, String url, int displayOrder) {}

    public record OwnerSummary(
            Long id,
            String firstName,
            String lastName,
            String kycStatus,
            Double averageRating,
            long reviewCount
    ) {}
}
