package com.singularity.rentit.dto.response;

import com.singularity.rentit.enums.KycStatus;
import com.singularity.rentit.enums.PreferredTheme;
import com.singularity.rentit.enums.UserRole;

import java.time.LocalDateTime;

public record UserProfileResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        String phone,
        UserRole role,
        KycStatus kycStatus,
        String preferredLanguage,
        PreferredTheme preferredTheme,
        boolean gdprSigned,
        boolean active,
        Double averageRating,
        long reviewCount,
        long activeListingsCount,
        long totalRentalsCount,
        LocalDateTime createdAt
) {}
