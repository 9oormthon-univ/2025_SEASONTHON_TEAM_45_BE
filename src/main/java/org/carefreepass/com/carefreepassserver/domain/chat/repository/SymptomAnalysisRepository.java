package org.carefreepass.com.carefreepassserver.domain.chat.repository;

import org.carefreepass.com.carefreepassserver.domain.chat.entity.SymptomAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// 증상 분석 리포지토리
@Repository
public interface SymptomAnalysisRepository extends JpaRepository<SymptomAnalysis, Long> {

    // 채팅 세션 ID로 증상 분석 결과 조회
    Optional<SymptomAnalysis> findByChatSessionId(Long sessionId);
}