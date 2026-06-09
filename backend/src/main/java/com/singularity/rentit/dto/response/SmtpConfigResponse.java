package com.singularity.rentit.dto.response;

import com.singularity.rentit.enums.SmtpSecurity;

import java.time.LocalDateTime;

public record SmtpConfigResponse(
        Long id,
        String host,
        Integer port,
        SmtpSecurity security,
        String username,
        String displayName,
        boolean active,
        LocalDateTime updatedAt,
        String updatedBy
) {}
