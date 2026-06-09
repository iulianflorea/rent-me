package com.singularity.rentit.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record KycSubmitRequest(
        @NotBlank String idSeries,
        @NotBlank String idNumber,
        @NotBlank String cnp,
        @NotNull LocalDate birthDate,
        @NotNull LocalDate idExpiryDate
) {}
