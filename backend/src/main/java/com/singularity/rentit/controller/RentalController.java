package com.singularity.rentit.controller;

import com.singularity.rentit.dto.request.CreateRentalRequest;
import com.singularity.rentit.dto.response.QrCodeResponse;
import com.singularity.rentit.dto.response.RentalResponse;
import com.singularity.rentit.entity.User;
import com.singularity.rentit.service.RentalService;
import com.singularity.rentit.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<RentalResponse> createRental(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateRentalRequest request
    ) {
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(rentalService.createRental(request, user));
    }

    @GetMapping("/as-tenant")
    public ResponseEntity<Page<RentalResponse>> getMyRentalsAsTenant(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(rentalService.getMyRentalsAsTenant(user.getId(), pageable));
    }

    @GetMapping("/as-owner")
    public ResponseEntity<Page<RentalResponse>> getMyRentalsAsOwner(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(rentalService.getMyRentalsAsOwner(user.getId(), pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RentalResponse> getRental(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(rentalService.getRental(id, user.getId()));
    }

    @PostMapping("/{id}/ready-to-pickup")
    public ResponseEntity<RentalResponse> markReadyToPickup(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(rentalService.markReadyToPickup(id, user));
    }

    @PostMapping("/{id}/confirm-pickup")
    public ResponseEntity<RentalResponse> confirmPickup(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(rentalService.confirmPickup(id, user));
    }

    @PostMapping("/return")
    public ResponseEntity<RentalResponse> returnByQr(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String qrToken
    ) {
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(rentalService.returnByQr(qrToken, user));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<RentalResponse> cancelRental(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestParam(required = false) String reason
    ) {
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(rentalService.cancelRental(id, user, reason));
    }

    @GetMapping("/{id}/qr")
    public ResponseEntity<QrCodeResponse> getQrCode(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(rentalService.getQrCode(id, user));
    }
}
