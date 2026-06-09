package com.singularity.rentit.dto.request;

import com.singularity.rentit.enums.MessageType;
import jakarta.validation.constraints.NotNull;

public record SendMessageRequest(
        @NotNull Long roomId,
        String content,
        String fileUrl,
        MessageType messageType
) {}
