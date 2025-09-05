package org.carefreepass.com.carefreepassserver.domain.chat.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 스마트 증상 분석 결과 DTO
 * SmartSymptomAnalyzer에서 증상 분석 시 사용
 */
@Getter
@Builder
public class SymptomAnalysisResult {
    
    private final String department;
    private final double confidence;
    private final String message;
    private final boolean needsMoreInfo;
    private final String followUpQuestion;
    
    /**
     * 진료과가 결정된 경우의 생성자
     */
    public SymptomAnalysisResult(String department, double confidence, String message) {
        this.department = department;
        this.confidence = confidence;
        this.message = message;
        this.needsMoreInfo = false;
        this.followUpQuestion = null;
    }
    
    /**
     * 추가 질문이 필요한 경우의 생성자
     */
    public SymptomAnalysisResult(String message, String followUpQuestion) {
        this.department = null;
        this.confidence = 0.0;
        this.message = message;
        this.needsMoreInfo = true;
        this.followUpQuestion = followUpQuestion;
    }
    
    /**
     * Builder 패턴을 위한 생성자
     */
    public SymptomAnalysisResult(String department, double confidence, String message, 
                               boolean needsMoreInfo, String followUpQuestion) {
        this.department = department;
        this.confidence = confidence;
        this.message = message;
        this.needsMoreInfo = needsMoreInfo;
        this.followUpQuestion = followUpQuestion;
    }
}