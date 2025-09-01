package org.carefreepass.com.carefreepassserver.domain.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.carefreepass.com.carefreepassserver.domain.chat.entity.ChatMessage;
import org.carefreepass.com.carefreepassserver.domain.chat.entity.MessageSenderType;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatMessageResponse {

    @Schema(description = "메시지 ID", example = "1")
    private final Long messageId;
    
    @Schema(description = "메시지 송신자 타입", example = "USER")
    private final MessageSenderType senderType;
    
    @Schema(description = "메시지 내용", example = "머리가 아파요")
    private final String content;
    
    @Schema(description = "메시지 순서 번호", example = "1")
    private final Integer sequenceNumber;
    
    @Schema(description = "메시지 생성 시간", example = "2024-12-31T14:30:00")
    private final LocalDateTime createdAt;

    public static ChatMessageResponse from(ChatMessage message) {
        return new ChatMessageResponse(
                message.getId(),
                message.getSenderType(),
                message.getContent(),
                message.getSequenceNumber(),
                message.getCreatedAt()
        );
    }
}