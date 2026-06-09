package com.singularity.rentit.dto.request;

import jakarta.validation.constraints.NotNull;

public record GdprSignRequest(
        @NotNull Boolean accepted
) {}
