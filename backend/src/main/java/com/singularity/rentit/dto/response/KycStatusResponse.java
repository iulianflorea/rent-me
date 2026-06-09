package com.singularity.rentit.dto.response;

import com.singularity.rentit.enums.KycStatus;

import java.time.LocalDateTime;

public record KycStatusResponse(
        Long id,
        KycStatus status,
        String rejectionReason,
        LocalDateTime submittedAt,
        LocalDateTime reviewedAt,
        boolean selfieUploaded,
        boolean idFrontUploaded,
        boolean idBackUploaded,
        boolean dataSubmitted
) {}
