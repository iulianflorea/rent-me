package com.singularity.rentit.dto.response;

import java.math.BigDecimal;

public record PaymentSplitPreviewResponse(
        BigDecimal totalAmount,
        BigDecimal platformFee,
        BigDecimal estimatedStripeFee,
        BigDecimal ownerReceives,
        BigDecimal guaranteeAmount,
        int days,
        BigDecimal pricePerDay
) {}
