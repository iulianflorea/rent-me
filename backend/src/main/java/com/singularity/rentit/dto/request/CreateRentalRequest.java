package com.singularity.rentit.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateRentalRequest(
        @NotNull Long listingId,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
) {}
