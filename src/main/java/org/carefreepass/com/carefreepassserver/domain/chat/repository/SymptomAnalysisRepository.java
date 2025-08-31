package org.carefreepass.com.carefreepassserver.domain.chat.repository;

import org.carefreepass.com.carefreepassserver.domain.chat.entity.SymptomAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SymptomAnalysisRepository extends JpaRepository<SymptomAnalysis, Long> {

    Optional<SymptomAnalysis> findByChatSessionId(Long sessionId);
}