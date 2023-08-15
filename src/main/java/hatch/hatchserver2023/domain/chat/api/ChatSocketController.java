package hatch.hatchserver2023.domain.chat.api;

import hatch.hatchserver2023.domain.chat.application.ChatService;
import hatch.hatchserver2023.domain.chat.domain.ChatMessage;
import hatch.hatchserver2023.domain.chat.dto.ChatRequestDto;
import hatch.hatchserver2023.domain.chat.dto.ChatResponseDto;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.global.common.response.CommonResponse;
import hatch.hatchserver2023.global.common.response.socket.SocketResponseType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Slf4j
@Controller
public class ChatSocketController {
    private final String CHAT_WS_SEND_URL_PREFIX = "/topic/chats/rooms/"; // + chatRoomId

    private final ChatService chatService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public ChatSocketController(ChatService chatService, SimpMessagingTemplate simpMessagingTemplate) {
        this.chatService = chatService;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @MessageMapping("/chats/messages")
    public void sendChatMessgae(@Valid ChatRequestDto.SendChatMessage requestDto, @AuthenticationPrincipal @NotNull User user) {
        log.info("[WS] /app/chats/messages");
        ChatMessage chatMessage = chatService.sendChatMessage(requestDto.getChatRoomId(), requestDto.getContent(), user);

        simpMessagingTemplate.convertAndSend(CHAT_WS_SEND_URL_PREFIX + requestDto.getChatRoomId(),
                CommonResponse.toSocketResponse(SocketResponseType.CHAT_MESSAGE, ChatResponseDto.SendChatMessage.toDto(chatMessage)));
    }
}
