package org.carefreepass.com.carefreepassserver.domain.chat.repository;

import org.carefreepass.com.carefreepassserver.domain.chat.entity.SymptomAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 증상 분석 리포지토리
 * AI가 분석한 증상 정보에 대한 데이터베이스 접근을 담당합니다.
 * 분석 결과 저장, 세션별 분석 결과 조회 등의 기능을 제공합니다.
 */
@Repository
public interface SymptomAnalysisRepository extends JpaRepository<SymptomAnalysis, Long> {

    /**
     * 채팅 세션 ID로 증상 분석 결과 조회
     * 특정 채팅 세션에 대한 AI 분석 결과를 조회합니다.
     * 
     * @param sessionId 채팅 세션 ID
     * @return 증상 분석 결과 (없을 경우 Optional.empty())
     */
    Optional<SymptomAnalysis> findByChatSessionId(Long sessionId);
}