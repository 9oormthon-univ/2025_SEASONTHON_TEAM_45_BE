package org.carefreepass.com.carefreepassserver.domain.chat.repository;

import org.carefreepass.com.carefreepassserver.domain.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 채팅 메시지 리포지토리
 * 채팅 메시지 정보에 대한 데이터베이스 접근을 담당합니다.
 * 메시지 저장, 세션별 메시지 조회, 최신 메시지 조회 등의 기능을 제공합니다.
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * 특정 세션의 모든 메시지 조회 (순서별 정렬)
     * 채팅 세션의 메시지들을 시간 순서대로 조회합니다.
     * 
     * @param sessionId 채팅 세션 ID
     * @return 순서 번호로 정렬된 메시지 목록
     */
    List<ChatMessage> findByChatSessionIdOrderBySequenceNumber(Long sessionId);

    /**
     * 특정 세션의 가장 최근 메시지 조회
     * 다음 메시지의 순서 번호를 계산하거나 마지막 메시지 내용을 확인할 때 사용합니다.
     * 
     * @param sessionId 채팅 세션 ID
     * @return 가장 최근 메시지 (순서 번호가 가장 큰 메시지)
     */
    ChatMessage findFirstByChatSessionIdOrderBySequenceNumberDesc(Long sessionId);
}