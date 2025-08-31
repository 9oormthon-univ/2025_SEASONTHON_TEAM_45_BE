package org.carefreepass.com.carefreepassserver.domain.chat.repository;

import org.carefreepass.com.carefreepassserver.domain.chat.entity.ChatSession;
import org.carefreepass.com.carefreepassserver.domain.chat.entity.ChatSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 채팅 세션 리포지토리
 * 채팅 세션 정보에 대한 데이터베이스 접근을 담당합니다.
 * 세션 생성, 조회, 메시지 및 증상분석 결과 포함 조회 등의 기능을 제공합니다.
 */
@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    /**
     * 회원의 모든 채팅 세션 조회 (최신순)
     * 회원의 채팅 이력을 최신 순서로 조회합니다.
     * 
     * @param memberId 회원 ID
     * @return 생성시간 역순으로 정렬된 채팅 세션 목록
     */
    List<ChatSession> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    /**
     * 회원의 특정 상태 채팅 세션 조회
     * 예: 진행 중인 세션만 조회할 때 사용
     * 
     * @param memberId 회원 ID
     * @param status 세션 상태
     * @return 해당 상태의 채팅 세션 목록
     */
    List<ChatSession> findByMemberIdAndStatus(Long memberId, ChatSessionStatus status);

    /**
     * 세션 ID와 회원 ID로 세션 조회 (메시지 포함)
     * 특정 세션의 모든 메시지를 함께 로딩하여 N+1 문제를 방지합니다.
     * 
     * @param sessionId 세션 ID
     * @param memberId 회원 ID
     * @return 메시지를 포함한 채팅 세션
     */
    @Query("SELECT cs FROM ChatSession cs LEFT JOIN FETCH cs.messages " +
           "WHERE cs.id = :sessionId AND cs.member.id = :memberId")
    Optional<ChatSession> findByIdAndMemberIdWithMessages(
            @Param("sessionId") Long sessionId, 
            @Param("memberId") Long memberId);

    /**
     * 세션 ID로 세션 조회 (증상분석 결과 포함)
     * 증상분석 결과와 함께 세션을 로딩하여 N+1 문제를 방지합니다.
     * 
     * @param sessionId 세션 ID
     * @return 증상분석 결과를 포함한 채팅 세션
     */
    @Query("SELECT cs FROM ChatSession cs LEFT JOIN FETCH cs.symptomAnalysis " +
           "WHERE cs.id = :sessionId")
    Optional<ChatSession> findByIdWithSymptomAnalysis(@Param("sessionId") Long sessionId);
}