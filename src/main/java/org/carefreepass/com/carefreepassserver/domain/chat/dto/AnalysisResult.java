package org.carefreepass.com.carefreepassserver.domain.chat.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * AI 채팅 증상 분석 결과 DTO
 * AiChatService에서 진료과 추천 시 사용
 */
@Getter
@Builder
public class AnalysisResult {
    
    private final List<String> extractedSymptoms;
    private final String recommendedDepartment;
    private final double confidenceScore;
    private final String summary;
    private final List<String> additionalQuestions;

    public AnalysisResult(List<String> extractedSymptoms, 
                         String recommendedDepartment,
                         double confidenceScore, 
                         String summary, 
                         List<String> additionalQuestions) {
        this.extractedSymptoms = extractedSymptoms;
        this.recommendedDepartment = recommendedDepartment;
        this.confidenceScore = confidenceScore;
        this.summary = summary;
        this.additionalQuestions = additionalQuestions;
    }
}