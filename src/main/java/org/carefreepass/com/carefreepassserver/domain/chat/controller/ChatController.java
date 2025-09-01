package org.carefreepass.com.carefreepassserver.domain.chat.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.carefreepass.com.carefreepassserver.domain.chat.dto.request.ChatMessageRequest;
import org.carefreepass.com.carefreepassserver.domain.chat.dto.request.ChatStartRequest;
import org.carefreepass.com.carefreepassserver.domain.chat.dto.response.ChatMessageResponse;
import org.carefreepass.com.carefreepassserver.domain.chat.dto.response.ChatSessionResponse;
import org.carefreepass.com.carefreepassserver.domain.chat.entity.ChatMessage;
import org.carefreepass.com.carefreepassserver.domain.chat.entity.ChatSession;
import org.carefreepass.com.carefreepassserver.domain.chat.service.AiChatService;
import org.carefreepass.com.carefreepassserver.golbal.error.BusinessException;
import org.carefreepass.com.carefreepassserver.golbal.response.ApiResponseTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
@Slf4j
@CrossOrigin(origins = "*")
public class ChatController implements org.carefreepass.com.carefreepassserver.domain.chat.controller.docs.ChatDocs {

    private final AiChatService aiChatService;

    @PostMapping("/start")
    public ApiResponseTemplate<ChatSessionResponse> startChatSession(@Valid @RequestBody ChatStartRequest request) {
        try {
            ChatSession session = aiChatService.startNewChatSession(request.getMemberId(), request.getInitialMessage());
            ChatSessionResponse response = ChatSessionResponse.from(session);

            return ApiResponseTemplate.ok()
                    .body(response);

        } catch (BusinessException e) {
            return ApiResponseTemplate.error()
                    .code(e.getErrorCode().getCode())
                    .message(e.getErrorCode().getMessage())
                    .build();
        } catch (Exception e) {
            log.error("채팅 세션 시작 실패", e);
            return ApiResponseTemplate.error()
                    .code("CHAT_5001")
                    .message("채팅 세션 시작에 실패했습니다.")
                    .build();
        }
    }

    @PostMapping("/message")
    public ApiResponseTemplate<ChatMessageResponse> sendMessage(@Valid @RequestBody ChatMessageRequest request) {
        try {
            ChatMessage aiMessage = aiChatService.sendMessage(
                    request.getSessionId(),
                    request.getMemberId(),
                    request.getContent()
            );

            ChatMessageResponse response = ChatMessageResponse.from(aiMessage);

            return ApiResponseTemplate.ok()
                    .body(response);

        } catch (BusinessException e) {
            return ApiResponseTemplate.error()
                    .code(e.getErrorCode().getCode())
                    .message(e.getErrorCode().getMessage())
                    .build();
        } catch (Exception e) {
            log.error("메시지 전송 실패", e);
            return ApiResponseTemplate.error()
                    .code("CHAT_5002")
                    .message("메시지 전송에 실패했습니다.")
                    .build();
        }
    }

    @GetMapping("/sessions")
    public ApiResponseTemplate<List<ChatSessionResponse>> getUserChatSessions(@RequestParam Long memberId) {
        try {
            List<ChatSession> sessions = aiChatService.getUserChatSessions(memberId);
            List<ChatSessionResponse> responses = sessions.stream()
                    .map(ChatSessionResponse::withoutMessages)
                    .toList();

            return ApiResponseTemplate.ok()
                    .body(responses);

        } catch (Exception e) {
            log.error("채팅 세션 목록 조회 실패", e);
            return ApiResponseTemplate.error()
                    .code("CHAT_5003")
                    .message("채팅 세션 목록 조회에 실패했습니다.")
                    .build();
        }
    }

    @GetMapping("/sessions/{sessionId}")
    public ApiResponseTemplate<ChatSessionResponse> getChatSession(
            @PathVariable Long sessionId,
            @RequestParam Long memberId) {
        
        try {
            ChatSession session = aiChatService.getChatSession(sessionId, memberId);
            ChatSessionResponse response = ChatSessionResponse.from(session);

            return ApiResponseTemplate.ok()
                    .body(response);

        } catch (BusinessException e) {
            return ApiResponseTemplate.error()
                    .code(e.getErrorCode().getCode())
                    .message(e.getErrorCode().getMessage())
                    .build();
        } catch (Exception e) {
            log.error("채팅 세션 상세 조회 실패", e);
            return ApiResponseTemplate.error()
                    .code("CHAT_5004")
                    .message("채팅 세션 상세 조회에 실패했습니다.")
                    .build();
        }
    }

    @PutMapping("/sessions/{sessionId}/complete")
    public ApiResponseTemplate<String> completeChatSession(
            @PathVariable Long sessionId,
            @RequestParam Long memberId) {
        
        try {
            aiChatService.completeChatSession(sessionId, memberId);

            return ApiResponseTemplate.ok()
                    .body("SUCCESS");

        } catch (BusinessException e) {
            return ApiResponseTemplate.error()
                    .code(e.getErrorCode().getCode())
                    .message(e.getErrorCode().getMessage())
                    .build();
        } catch (Exception e) {
            log.error("채팅 세션 완료 실패", e);
            return ApiResponseTemplate.error()
                    .code("CHAT_5005")
                    .message("채팅 세션 완료에 실패했습니다.")
                    .build();
        }
    }

    // WebSocket을 통한 실시간 메시징 (선택사항)
    @MessageMapping("/chat.send")
    @SendTo("/topic/chat")
    public ChatMessageResponse sendMessageViaWebSocket(ChatMessageRequest request) {
        try {
            ChatMessage aiMessage = aiChatService.sendMessage(
                    request.getSessionId(),
                    request.getMemberId(),
                    request.getContent()
            );
            return ChatMessageResponse.from(aiMessage);
        } catch (Exception e) {
            log.error("WebSocket 메시지 전송 실패", e);
            throw new RuntimeException("메시지 전송 실패");
        }
    }
}