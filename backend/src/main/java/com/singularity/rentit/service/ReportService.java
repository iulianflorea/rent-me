package com.singularity.rentit.service;

import com.singularity.rentit.dto.request.ReportPeriodRequest;
import com.singularity.rentit.dto.response.AdminReportResponse;
import com.singularity.rentit.dto.response.FinancialReportResponse;
import com.singularity.rentit.repository.ListingRepository;
import com.singularity.rentit.repository.RentalRepository;
import com.singularity.rentit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;
    private final ListingRepository listingRepository;

    @Transactional(readOnly = true)
    public FinancialReportResponse getUserReport(Long userId, ReportPeriodRequest request) {
        LocalDateTime from = request.from().atStartOfDay();
        LocalDateTime to = request.to().atTime(23, 59, 59);

        BigDecimal earnings = rentalRepository.sumEarningsByOwnerAndPeriod(userId, from, to)
                .orElse(BigDecimal.ZERO);
        BigDecimal spending = rentalRepository.sumSpendingByTenantAndPeriod(userId, from, to)
                .orElse(BigDecimal.ZERO);

        long rentalsAsOwner = rentalRepository.countByOwnerId(userId);
        long rentalsAsTenant = rentalRepository.countByTenantId(userId);

        return new FinancialReportResponse(
                request.from(), request.to(),
                earnings, spending, earnings.subtract(spending),
                rentalsAsOwner, rentalsAsTenant
        );
    }

    @Transactional(readOnly = true)
    public AdminReportResponse getAdminReport(ReportPeriodRequest request) {
        LocalDateTime from = request.from().atStartOfDay();
        LocalDateTime to = request.to().atTime(23, 59, 59);

        BigDecimal grossRevenue = rentalRepository.sumPlatformFeesByPeriod(from, to)
                .orElse(BigDecimal.ZERO);
        BigDecimal stripeFees = rentalRepository.sumStripeFeesByPeriod(from, to)
                .orElse(BigDecimal.ZERO);
        BigDecimal netProfit = grossRevenue.subtract(stripeFees);

        long totalUsers = userRepository.count();
        long activeListings = listingRepository.countByOwnerIdAndStatus(0L, null);

        return new AdminReportResponse(
                request.from(), request.to(),
                grossRevenue, stripeFees, netProfit,
                0L, totalUsers, activeListings
        );
    }
}
