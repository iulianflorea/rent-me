package com.singularity.rentit.dto.request;

import com.singularity.rentit.enums.ListingStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateListingRequest(
        @Size(max = 255) String title,
        String description,
        @DecimalMin("0.01") BigDecimal pricePerDay,
        BigDecimal pricePerWeek,
        BigDecimal pricePerMonth,
        String address,
        String city,
        String county,
        Double latitude,
        Double longitude,
        String categoryAttributes,
        ListingStatus status
) {}
