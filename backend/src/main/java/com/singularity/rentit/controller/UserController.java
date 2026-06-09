package com.singularity.rentit.controller;

import com.singularity.rentit.dto.request.GdprSignRequest;
import com.singularity.rentit.dto.request.KycSubmitRequest;
import com.singularity.rentit.dto.request.UpdateProfileRequest;
import com.singularity.rentit.dto.response.KycStatusResponse;
import com.singularity.rentit.dto.response.UserProfileResponse;
import com.singularity.rentit.entity.User;
import com.singularity.rentit.service.GdprService;
import com.singularity.rentit.service.KycService;
import com.singularity.rentit.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final KycService kycService;
    private final GdprService gdprService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getProfile(userDetails.getUsername()));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return ResponseEntity.ok(userService.updateProfile(userDetails.getUsername(), request));
    }

    @PatchMapping("/me/preferences")
    public ResponseEntity<UserProfileResponse> updatePreferences(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdateProfileRequest request
    ) {
        return ResponseEntity.ok(userService.updateProfile(userDetails.getUsername(), request));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal UserDetails userDetails) {
        userService.deleteAccount(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponse> getProfileById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getProfileById(id));
    }

    @GetMapping("/me/kyc")
    public ResponseEntity<KycStatusResponse> getKycStatus(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(kycService.getStatus(user.getId()));
    }

    @PostMapping("/me/kyc/selfie")
    public ResponseEntity<KycStatusResponse> uploadSelfie(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file
    ) {
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(kycService.uploadSelfie(user, file));
    }

    @PostMapping("/me/kyc/id-front")
    public ResponseEntity<KycStatusResponse> uploadIdFront(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file
    ) {
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(kycService.uploadIdFront(user, file));
    }

    @PostMapping("/me/kyc/id-back")
    public ResponseEntity<KycStatusResponse> uploadIdBack(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file
    ) {
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(kycService.uploadIdBack(user, file));
    }

    @PostMapping("/me/kyc/data")
    public ResponseEntity<KycStatusResponse> submitKycData(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody KycSubmitRequest request
    ) {
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(kycService.submitData(user, request));
    }

    @PostMapping("/me/gdpr")
    public ResponseEntity<Void> signGdpr(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody GdprSignRequest request,
            HttpServletRequest httpRequest
    ) {
        if (!request.accepted()) {
            return ResponseEntity.badRequest().build();
        }
        User user = userService.findByEmail(userDetails.getUsername());
        String ip = getClientIp(httpRequest);
        gdprService.signAgreement(user, ip);
        return ResponseEntity.ok().build();
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
