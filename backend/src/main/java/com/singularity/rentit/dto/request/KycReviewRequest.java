package com.singularity.rentit.dto.request;

import com.singularity.rentit.enums.KycStatus;
import jakarta.validation.constraints.NotNull;

public record KycReviewRequest(
        @NotNull KycStatus status,
        String rejectionReason
) {}
