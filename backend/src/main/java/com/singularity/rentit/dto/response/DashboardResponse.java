package com.singularity.rentit.dto.response;

import java.math.BigDecimal;

public record DashboardResponse(
        long totalListings,
        long activeListings,
        long totalRentalsAsOwner,
        long totalRentalsAsTenant,
        long pendingRentals,
        BigDecimal totalEarnings,
        BigDecimal totalSpending,
        long unreadMessages,
        long unreadNotifications,
        Double averageRating
) {}
