package com.singularity.rentit.dto.response;

import java.time.LocalDateTime;

public record ChatRoomResponse(
        Long id,
        Long rentalId,
        ParticipantSummary otherParticipant,
        ChatMessageResponse lastMessage,
        long unreadCount,
        LocalDateTime lastMessageAt,
        LocalDateTime createdAt
) {
    public record ParticipantSummary(Long id, String firstName, String lastName, boolean kycVerified) {}
}
