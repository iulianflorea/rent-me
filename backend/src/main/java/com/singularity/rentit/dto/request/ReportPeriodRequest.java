package com.singularity.rentit.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ReportPeriodRequest(
        @NotNull LocalDate from,
        @NotNull LocalDate to
) {}
