package com.singularity.rentit.dto.response;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        String title,
        String message,
        String type,
        Long referenceId,
        boolean read,
        LocalDateTime createdAt
) {}
