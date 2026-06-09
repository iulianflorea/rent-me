package com.singularity.rentit.dto.request;

import com.singularity.rentit.enums.RentalStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateRentalStatusRequest(
        @NotNull RentalStatus status,
        String reason
) {}
