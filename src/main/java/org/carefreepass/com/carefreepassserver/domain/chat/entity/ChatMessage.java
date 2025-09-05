package org.carefreepass.com.carefreepassserver.domain.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.carefreepass.com.carefreepassserver.golbal.domain.BaseTimeEntity;

// 채팅 메시지 엔티티 - 채팅 세션 내 사용자와 AI 간 개별 메시지 관리 (순서 및 발신자 타입 구분)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_messages")
public class ChatMessage extends BaseTimeEntity {

    // 메시지 고유 식별자
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long id;

    // 이 메시지가 속한 채팅 세션
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSession chatSession;

    // 메시지 발신자 타입 (USER: 사용자, AI: 인공지능)
    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", nullable = false)
    private MessageSenderType senderType;

    // 메시지 내용 (TEXT 타입으로 긴 내용 저장 가능)
    @Lob
    @Column(name = "message_content", nullable = false)
    private String content;

    // 채팅 세션 내 메시지 순서 번호
    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber;

    // 사용자 메시지 생성 (발신자 타입: USER)
    public static ChatMessage createUserMessage(String content, Integer sequenceNumber) {
        ChatMessage message = new ChatMessage();
        message.senderType = MessageSenderType.USER;
        message.content = content;
        message.sequenceNumber = sequenceNumber;
        return message;
    }

    // AI 메시지 생성 (발신자 타입: AI)
    public static ChatMessage createAiMessage(String content, Integer sequenceNumber) {
        ChatMessage message = new ChatMessage();
        message.senderType = MessageSenderType.AI;
        message.content = content;
        message.sequenceNumber = sequenceNumber;
        return message;
    }
}