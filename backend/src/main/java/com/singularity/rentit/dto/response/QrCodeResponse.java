package com.singularity.rentit.dto.response;

public record QrCodeResponse(
        String qrCodeBase64,
        String token
) {}
