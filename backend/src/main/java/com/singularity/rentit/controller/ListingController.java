package com.singularity.rentit.controller;

import com.singularity.rentit.dto.request.CreateListingRequest;
import com.singularity.rentit.dto.request.UpdateListingRequest;
import com.singularity.rentit.dto.response.ListingResponse;
import com.singularity.rentit.dto.response.ListingSummaryResponse;
import com.singularity.rentit.dto.response.PaymentSplitPreviewResponse;
import com.singularity.rentit.entity.User;
import com.singularity.rentit.enums.CategoryType;
import com.singularity.rentit.service.ListingService;
import com.singularity.rentit.service.PaymentService;
import com.singularity.rentit.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/listings")
@RequiredArgsConstructor
public class ListingController {

    private final ListingService listingService;
    private final PaymentService paymentService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ListingResponse> createListing(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateListingRequest request
    ) {
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(listingService.createListing(request, user));
    }

    @GetMapping
    public ResponseEntity<Page<ListingSummaryResponse>> searchListings(
            @RequestParam(required = false) CategoryType category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String county,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(listingService.searchListings(
                category, minPrice, maxPrice, city, county, search, startDate, endDate, lat, lon, pageable
        ));
    }

    @GetMapping("/nearby")
    public ResponseEntity<Page<ListingSummaryResponse>> getNearby(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "10") double radiusKm,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(listingService.getNearby(lat, lon, radiusKm, pageable));
    }

    @GetMapping("/mine")
    public ResponseEntity<Page<ListingSummaryResponse>> getMyListings(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(listingService.getMyListings(user, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ListingResponse> getListing(
            @PathVariable Long id,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon
    ) {
        return ResponseEntity.ok(listingService.getListing(id, lat, lon));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ListingResponse> updateListing(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody UpdateListingRequest request
    ) {
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(listingService.updateListing(id, request, user));
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<ListingResponse> addImages(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestParam("files") List<MultipartFile> files
    ) {
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(listingService.addImages(id, files, user));
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<ListingResponse> publishListing(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(listingService.publishListing(id, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteListing(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        User user = userService.findByEmail(userDetails.getUsername());
        listingService.deleteListing(id, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/payment-preview")
    public ResponseEntity<PaymentSplitPreviewResponse> getPaymentPreview(
            @RequestParam BigDecimal pricePerDay,
            @RequestParam int days,
            @RequestParam CategoryType category
    ) {
        return ResponseEntity.ok(paymentService.calculateSplit(pricePerDay, days, category));
    }
}
