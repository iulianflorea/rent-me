package com.singularity.rentit.dto.response;

import com.singularity.rentit.enums.RentalStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record RentalResponse(
        Long id,
        String referenceNumber,
        ListingSummary listing,
        UserSummary tenant,
        UserSummary owner,
        LocalDate startDate,
        LocalDate endDate,
        int totalDays,
        BigDecimal pricePerDay,
        BigDecimal subtotal,
        BigDecimal guaranteeAmount,
        BigDecimal totalAmount,
        RentalStatus status,
        boolean hasQrCode,
        LocalDateTime createdAt
) {
    public record ListingSummary(Long id, String title, String firstImageUrl, String city) {}
    public record UserSummary(Long id, String firstName, String lastName, boolean kycVerified) {}
}
