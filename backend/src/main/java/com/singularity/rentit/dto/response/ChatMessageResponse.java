package com.singularity.rentit.dto.response;

import com.singularity.rentit.enums.MessageType;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        Long id,
        Long roomId,
        SenderSummary sender,
        String content,
        String fileUrl,
        MessageType messageType,
        boolean read,
        LocalDateTime createdAt
) {
    public record SenderSummary(Long id, String firstName, String lastName) {}
}
