package com.singularity.rentit.dto.request;

import com.singularity.rentit.enums.KycStatus;
import com.singularity.rentit.enums.UserRole;

public record AdminUserFilterRequest(
        String email,
        UserRole role,
        KycStatus kycStatus,
        Boolean active
) {}
