package org.carefreepass.com.carefreepassserver.domain.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.carefreepass.com.carefreepassserver.golbal.domain.BaseTimeEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_messages")
public class ChatMessage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSession chatSession;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", nullable = false)
    private MessageSenderType senderType;

    @Column(name = "message_content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber;

    public static ChatMessage createUserMessage(String content, Integer sequenceNumber) {
        ChatMessage message = new ChatMessage();
        message.senderType = MessageSenderType.USER;
        message.content = content;
        message.sequenceNumber = sequenceNumber;
        return message;
    }

    public static ChatMessage createAiMessage(String content, Integer sequenceNumber) {
        ChatMessage message = new ChatMessage();
        message.senderType = MessageSenderType.AI;
        message.content = content;
        message.sequenceNumber = sequenceNumber;
        return message;
    }
}