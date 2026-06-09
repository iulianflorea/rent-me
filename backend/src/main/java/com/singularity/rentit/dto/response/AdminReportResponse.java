package com.singularity.rentit.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AdminReportResponse(
        LocalDate from,
        LocalDate to,
        BigDecimal grossRevenue,
        BigDecimal stripeFees,
        BigDecimal netProfit,
        long totalTransactions,
        long totalUsers,
        long activeListings
) {}
