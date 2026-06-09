package com.singularity.rentit.controller;

import com.singularity.rentit.dto.request.*;
import com.singularity.rentit.dto.response.*;
import com.singularity.rentit.entity.User;
import com.singularity.rentit.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final KycService kycService;
    private final ReportService reportService;
    private final SmtpConfigService smtpConfigService;
    private final UserService userService;

    @GetMapping("/users")
    public ResponseEntity<Page<UserProfileResponse>> getUsers(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String kycStatus,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        AdminUserFilterRequest filter = new AdminUserFilterRequest(
                email,
                role != null ? com.singularity.rentit.enums.UserRole.valueOf(role) : null,
                kycStatus != null ? com.singularity.rentit.enums.KycStatus.valueOf(kycStatus) : null,
                active
        );
        return ResponseEntity.ok(adminService.getUsers(filter, pageable));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserProfileResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getProfileById(id));
    }

    @PostMapping("/users/{id}/suspend")
    public ResponseEntity<UserProfileResponse> suspendUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.suspendUser(id));
    }

    @PostMapping("/users/{id}/activate")
    public ResponseEntity<UserProfileResponse> activateUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.activateUser(id));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/{id}/kyc")
    public ResponseEntity<KycStatusResponse> getUserKyc(@PathVariable Long id) {
        return ResponseEntity.ok(kycService.getStatus(id));
    }

    @PostMapping("/users/{id}/kyc/review")
    public ResponseEntity<KycStatusResponse> reviewKyc(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody KycReviewRequest request
    ) {
        User admin = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(kycService.reviewKyc(id, request, admin.getId()));
    }

    @PostMapping("/reports")
    public ResponseEntity<AdminReportResponse> getAdminReport(
            @Valid @RequestBody ReportPeriodRequest request
    ) {
        return ResponseEntity.ok(reportService.getAdminReport(request));
    }

    @GetMapping("/smtp")
    public ResponseEntity<SmtpConfigResponse> getSmtpConfig() {
        return ResponseEntity.ok(smtpConfigService.getCurrentConfig());
    }

    @PostMapping("/smtp")
    public ResponseEntity<SmtpConfigResponse> saveSmtpConfig(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SmtpConfigRequest request
    ) {
        return ResponseEntity.ok(smtpConfigService.saveConfig(request, userDetails.getUsername()));
    }

    @PostMapping("/smtp/test")
    public ResponseEntity<String> testSmtp() {
        String status = smtpConfigService.getSmtpStatus();
        return ResponseEntity.ok(status);
    }

    @GetMapping("/smtp/status")
    public ResponseEntity<String> getSmtpStatus() {
        return ResponseEntity.ok(smtpConfigService.getSmtpStatus());
    }
}
