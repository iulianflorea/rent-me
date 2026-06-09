package com.singularity.rentit.service;

import com.singularity.rentit.dto.request.SendMessageRequest;
import com.singularity.rentit.dto.response.ChatMessageResponse;
import com.singularity.rentit.dto.response.ChatRoomResponse;
import com.singularity.rentit.entity.ChatMessage;
import com.singularity.rentit.entity.ChatRoom;
import com.singularity.rentit.entity.User;
import com.singularity.rentit.enums.KycStatus;
import com.singularity.rentit.enums.MessageType;
import com.singularity.rentit.exception.BusinessException;
import com.singularity.rentit.exception.ResourceNotFoundException;
import com.singularity.rentit.repository.ChatMessageRepository;
import com.singularity.rentit.repository.ChatRoomRepository;
import com.singularity.rentit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;

    @Transactional(readOnly = true)
    public Page<ChatRoomResponse> getMyRooms(Long userId, Pageable pageable) {
        return chatRoomRepository.findByParticipant(userId, pageable)
                .map(room -> toRoomResponse(room, userId));
    }

    @Transactional(readOnly = true)
    public ChatRoomResponse getRoom(Long roomId, Long userId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatRoom", roomId));
        checkParticipant(room, userId);
        return toRoomResponse(room, userId);
    }

    @Transactional
    public ChatMessageResponse sendMessage(SendMessageRequest request, Long senderId) {
        ChatRoom room = chatRoomRepository.findById(request.roomId())
                .orElseThrow(() -> new ResourceNotFoundException("ChatRoom", request.roomId()));
        checkParticipant(room, senderId);

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User", senderId));

        MessageType type = request.messageType() != null ? request.messageType() : MessageType.TEXT;

        ChatMessage message = ChatMessage.builder()
                .room(room)
                .sender(sender)
                .content(request.content())
                .fileUrl(request.fileUrl())
                .messageType(type)
                .build();

        ChatMessage saved = chatMessageRepository.save(message);

        room.setLastMessageAt(LocalDateTime.now());
        chatRoomRepository.save(room);

        return toMessageResponse(saved);
    }

    @Transactional
    public ChatMessageResponse sendFileMessage(Long roomId, MultipartFile file, Long senderId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatRoom", roomId));
        checkParticipant(room, senderId);

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User", senderId));

        String url = storageService.uploadFile(file, "chat/" + roomId);
        String contentType = file.getContentType();
        MessageType type = contentType != null && contentType.startsWith("image/")
                ? MessageType.IMAGE
                : MessageType.ATTACHMENT;

        ChatMessage message = ChatMessage.builder()
                .room(room)
                .sender(sender)
                .fileUrl(url)
                .messageType(type)
                .build();

        ChatMessage saved = chatMessageRepository.save(message);
        room.setLastMessageAt(LocalDateTime.now());
        chatRoomRepository.save(room);

        return toMessageResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getMessages(Long roomId, Long userId, Pageable pageable) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatRoom", roomId));
        checkParticipant(room, userId);
        return chatMessageRepository.findByRoomIdOrderByCreatedAtAsc(roomId, pageable)
                .map(this::toMessageResponse);
    }

    @Transactional
    public void markAsRead(Long roomId, Long userId) {
        chatMessageRepository.markAllAsRead(roomId, userId);
    }

    @Transactional
    public ChatRoomResponse getOrCreateDirectRoom(Long userId, Long otherUserId) {
        return chatRoomRepository.findByParticipants(userId, otherUserId)
                .map(room -> toRoomResponse(room, userId))
                .orElseGet(() -> {
                    User user1 = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User", userId));
                    User user2 = userRepository.findById(otherUserId)
                            .orElseThrow(() -> new ResourceNotFoundException("User", otherUserId));

                    ChatRoom room = ChatRoom.builder()
                            .participant1(user1)
                            .participant2(user2)
                            .build();
                    ChatRoom saved = chatRoomRepository.save(room);
                    return toRoomResponse(saved, userId);
                });
    }

    private void checkParticipant(ChatRoom room, Long userId) {
        if (!room.getParticipant1().getId().equals(userId) && !room.getParticipant2().getId().equals(userId)) {
            throw new BusinessException("Not a participant of this chat room", HttpStatus.FORBIDDEN);
        }
    }

    private ChatRoomResponse toRoomResponse(ChatRoom room, Long currentUserId) {
        User other = room.getParticipant1().getId().equals(currentUserId)
                ? room.getParticipant2()
                : room.getParticipant1();

        long unread = chatMessageRepository.countByRoomIdAndReadFalseAndSenderIdNot(room.getId(), currentUserId);

        return new ChatRoomResponse(
                room.getId(),
                room.getRental() != null ? room.getRental().getId() : null,
                new ChatRoomResponse.ParticipantSummary(
                        other.getId(), other.getFirstName(), other.getLastName(),
                        other.getKycStatus() == KycStatus.VERIFIED
                ),
                null,
                unread,
                room.getLastMessageAt(),
                room.getCreatedAt()
        );
    }

    private ChatMessageResponse toMessageResponse(ChatMessage m) {
        return new ChatMessageResponse(
                m.getId(), m.getRoom().getId(),
                new ChatMessageResponse.SenderSummary(
                        m.getSender().getId(), m.getSender().getFirstName(), m.getSender().getLastName()
                ),
                m.getContent(), m.getFileUrl(), m.getMessageType(), m.isRead(), m.getCreatedAt()
        );
    }
}
