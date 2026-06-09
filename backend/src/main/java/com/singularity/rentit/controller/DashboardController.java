package com.singularity.rentit.controller;

import com.singularity.rentit.dto.request.ReportPeriodRequest;
import com.singularity.rentit.dto.response.DashboardResponse;
import com.singularity.rentit.dto.response.FinancialReportResponse;
import com.singularity.rentit.entity.User;
import com.singularity.rentit.enums.ListingStatus;
import com.singularity.rentit.enums.RentalStatus;
import com.singularity.rentit.repository.ListingRepository;
import com.singularity.rentit.repository.RentalRepository;
import com.singularity.rentit.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final UserService userService;
    private final ReviewService reviewService;
    private final ReportService reportService;
    private final NotificationService notificationService;
    private final ChatService chatService;
    private final RentalRepository rentalRepository;
    private final ListingRepository listingRepository;

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());

        long totalListings = listingRepository.countByOwnerId(user.getId());
        long activeListings = listingRepository.countByOwnerIdAndStatus(user.getId(), ListingStatus.ACTIVE);
        long rentalsAsOwner = rentalRepository.countByOwnerId(user.getId());
        long rentalsAsTenant = rentalRepository.countByTenantId(user.getId());
        long unreadNotifications = notificationService.getUnreadCount(user.getId());
        Double avgRating = reviewService.getAverageRating(user.getId());

        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();
        BigDecimal earnings = rentalRepository.sumEarningsByOwnerAndPeriod(user.getId(), monthStart, now)
                .orElse(BigDecimal.ZERO);
        BigDecimal spending = rentalRepository.sumSpendingByTenantAndPeriod(user.getId(), monthStart, now)
                .orElse(BigDecimal.ZERO);

        return ResponseEntity.ok(new DashboardResponse(
                totalListings, activeListings, rentalsAsOwner, rentalsAsTenant,
                0L, earnings, spending, 0L, unreadNotifications, avgRating
        ));
    }

    @PostMapping("/reports")
    public ResponseEntity<FinancialReportResponse> getReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ReportPeriodRequest request
    ) {
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(reportService.getUserReport(user.getId(), request));
    }

    @GetMapping("/notifications")
    public ResponseEntity<?> getNotifications(
            @AuthenticationPrincipal UserDetails userDetails,
            org.springframework.data.domain.Pageable pageable
    ) {
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(notificationService.getNotifications(user.getId(), pageable));
    }

    @GetMapping("/notifications/unread-count")
    public ResponseEntity<Long> getUnreadNotificationsCount(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(notificationService.getUnreadCount(user.getId()));
    }

    @PostMapping("/notifications/read-all")
    public ResponseEntity<Void> markAllNotificationsRead(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        notificationService.markAllAsRead(user.getId());
        return ResponseEntity.ok().build();
    }
}
