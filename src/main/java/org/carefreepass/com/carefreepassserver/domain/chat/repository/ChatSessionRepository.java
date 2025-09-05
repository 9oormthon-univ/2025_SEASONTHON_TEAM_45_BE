package org.carefreepass.com.carefreepassserver.domain.chat.repository;

import org.carefreepass.com.carefreepassserver.domain.chat.entity.ChatSession;
import org.carefreepass.com.carefreepassserver.domain.chat.entity.ChatSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// 채팅 세션 리포지토리
@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    // 회원의 모든 채팅 세션 조회 (최신순)
    List<ChatSession> findByMemberIdOrderByCreatedAtDesc(Long memberId);


    // 세션 ID와 회원 ID로 세션 조회 (메시지 포함)
    @Query("SELECT cs FROM ChatSession cs LEFT JOIN FETCH cs.messages " +
           "WHERE cs.id = :sessionId AND cs.member.id = :memberId")
    Optional<ChatSession> findByIdAndMemberIdWithMessages(
            @Param("sessionId") Long sessionId, 
            @Param("memberId") Long memberId);

}