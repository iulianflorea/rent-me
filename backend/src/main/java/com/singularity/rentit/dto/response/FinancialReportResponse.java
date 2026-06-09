package com.singularity.rentit.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FinancialReportResponse(
        LocalDate from,
        LocalDate to,
        BigDecimal totalEarnings,
        BigDecimal totalSpending,
        BigDecimal netBalance,
        long completedRentalsAsOwner,
        long completedRentalsAsTenant
) {}
