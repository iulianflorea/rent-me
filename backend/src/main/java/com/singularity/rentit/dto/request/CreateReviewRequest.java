package com.singularity.rentit.dto.request;

import jakarta.validation.constraints.*;

public record CreateReviewRequest(
        @NotNull Long rentalId,
        @NotNull @Min(1) @Max(5) Integer rating,
        @Size(max = 2000) String comment
) {}
