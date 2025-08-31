package org.carefreepass.com.carefreepassserver.domain.chat.repository;

import org.carefreepass.com.carefreepassserver.domain.chat.entity.ChatSession;
import org.carefreepass.com.carefreepassserver.domain.chat.entity.ChatSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    List<ChatSession> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    List<ChatSession> findByMemberIdAndStatus(Long memberId, ChatSessionStatus status);

    @Query("SELECT cs FROM ChatSession cs LEFT JOIN FETCH cs.messages " +
           "WHERE cs.id = :sessionId AND cs.member.id = :memberId")
    Optional<ChatSession> findByIdAndMemberIdWithMessages(
            @Param("sessionId") Long sessionId, 
            @Param("memberId") Long memberId);

    @Query("SELECT cs FROM ChatSession cs LEFT JOIN FETCH cs.symptomAnalysis " +
           "WHERE cs.id = :sessionId")
    Optional<ChatSession> findByIdWithSymptomAnalysis(@Param("sessionId") Long sessionId);
}