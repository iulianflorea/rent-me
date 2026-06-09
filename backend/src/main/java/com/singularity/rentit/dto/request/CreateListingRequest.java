package com.singularity.rentit.dto.request;

import com.singularity.rentit.enums.CategoryType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateListingRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank String description,
        @NotNull CategoryType category,
        @NotNull @DecimalMin("0.01") BigDecimal pricePerDay,
        BigDecimal pricePerWeek,
        BigDecimal pricePerMonth,
        String address,
        String city,
        String county,
        Double latitude,
        Double longitude,
        String categoryAttributes
) {}
