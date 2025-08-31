package org.carefreepass.com.carefreepassserver.domain.chat.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.carefreepass.com.carefreepassserver.domain.chat.entity.ChatMessage;
import org.carefreepass.com.carefreepassserver.domain.chat.entity.MessageSenderType;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatMessageResponse {

    private final Long messageId;
    private final MessageSenderType senderType;
    private final String content;
    private final Integer sequenceNumber;
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