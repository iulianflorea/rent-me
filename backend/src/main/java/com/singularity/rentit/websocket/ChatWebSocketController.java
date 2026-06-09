package com.singularity.rentit.websocket;

import com.singularity.rentit.dto.request.SendMessageRequest;
import com.singularity.rentit.dto.response.ChatMessageResponse;
import com.singularity.rentit.service.ChatService;
import com.singularity.rentit.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final ChatService chatService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload SendMessageRequest request, Principal principal) {
        try {
            var user = userService.findByEmail(principal.getName());
            ChatMessageResponse message = chatService.sendMessage(request, user.getId());

            messagingTemplate.convertAndSend("/topic/chat/" + request.roomId(), message);
            log.info("WebSocket message sent to room {}", request.roomId());
        } catch (Exception e) {
            log.error("Error sending WebSocket message: {}", e.getMessage(), e);
        }
    }

    @MessageMapping("/chat.typing")
    public void sendTypingIndicator(@Payload TypingIndicator indicator, Principal principal) {
        indicator = new TypingIndicator(principal.getName(), indicator.roomId(), true);
        messagingTemplate.convertAndSend("/topic/chat/" + indicator.roomId() + "/typing", indicator);
    }

    public record TypingIndicator(String userEmail, Long roomId, boolean typing) {}
}
