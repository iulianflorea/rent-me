package com.singularity.rentit.dto.response;

import java.time.LocalDateTime;

public record WishlistResponse(
        Long id,
        ListingSummaryResponse listing,
        LocalDateTime savedAt
) {}
