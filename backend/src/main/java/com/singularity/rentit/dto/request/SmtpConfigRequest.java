package com.singularity.rentit.dto.request;

import com.singularity.rentit.enums.SmtpSecurity;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SmtpConfigRequest(
        @NotBlank String host,
        @NotNull @Min(1) @Max(65535) Integer port,
        @NotNull SmtpSecurity security,
        @NotBlank String username,
        String password,
        @NotBlank String displayName
) {}
