package com.singularity.rentit.controller;

import com.singularity.rentit.dto.request.SendMessageRequest;
import com.singularity.rentit.dto.response.ChatMessageResponse;
import com.singularity.rentit.dto.response.ChatRoomResponse;
import com.singularity.rentit.entity.User;
import com.singularity.rentit.service.ChatService;
import com.singularity.rentit.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;

    @GetMapping("/rooms")
    public ResponseEntity<Page<ChatRoomResponse>> getMyRooms(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(chatService.getMyRooms(user.getId(), pageable));
    }

    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<ChatRoomResponse> getRoom(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long roomId
    ) {
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(chatService.getRoom(roomId, user.getId()));
    }

    @PostMapping("/rooms/direct/{otherUserId}")
    public ResponseEntity<ChatRoomResponse> getOrCreateDirectRoom(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long otherUserId
    ) {
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(chatService.getOrCreateDirectRoom(user.getId(), otherUserId));
    }

    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<Page<ChatMessageResponse>> getMessages(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long roomId,
            @PageableDefault(size = 50) Pageable pageable
    ) {
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(chatService.getMessages(roomId, user.getId(), pageable));
    }

    @PostMapping("/messages")
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SendMessageRequest request
    ) {
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(chatService.sendMessage(request, user.getId()));
    }

    @PostMapping("/rooms/{roomId}/files")
    public ResponseEntity<ChatMessageResponse> sendFile(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long roomId,
            @RequestParam("file") MultipartFile file
    ) {
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(chatService.sendFileMessage(roomId, file, user.getId()));
    }

    @PostMapping("/rooms/{roomId}/read")
    public ResponseEntity<Void> markAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long roomId
    ) {
        User user = userService.findByEmail(userDetails.getUsername());
        chatService.markAsRead(roomId, user.getId());
        return ResponseEntity.ok().build();
    }
}
