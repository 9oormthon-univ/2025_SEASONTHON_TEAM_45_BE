package org.carefreepass.com.carefreepassserver.domain.chat.repository;

import org.carefreepass.com.carefreepassserver.domain.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// 채팅 메시지 리포지토리
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 특정 세션의 모든 메시지 조회 (순서별 정렬)
    List<ChatMessage> findByChatSessionIdOrderBySequenceNumber(Long sessionId);

    // 특정 세션의 가장 최근 메시지 조회
    ChatMessage findFirstByChatSessionIdOrderBySequenceNumberDesc(Long sessionId);
}