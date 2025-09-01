package org.carefreepass.com.carefreepassserver.golbal.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * OpenAI API 관련 설정값들을 관리하는 Properties 클래스
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.openai")
public class OpenAIProperties {
    
    /**
     * 사용할 GPT 모델명
     */
    private String model = "gpt-3.5-turbo";
    
    /**
     * 일반 응답 생성 시 최대 토큰 수
     */
    private int maxTokensGeneral = 500;
    
    /**
     * 진료과 분석 시 최대 토큰 수
     */
    private int maxTokensAnalysis = 300;
    
    /**
     * 일반 응답 생성 시 temperature (창의성 정도)
     */
    private double temperatureGeneral = 0.7;
    
    /**
     * 진료과 분석 시 temperature (정확성 우선)
     */
    private double temperatureAnalysis = 0.5;
    
    /**
     * 시스템 프롬프트 - 일반 채팅
     */
    private String systemPromptGeneral = "당신은 병원 예약을 도와주는 친절한 AI 상담사입니다. 환자의 증상을 듣고 적절한 진료과를 추천해주세요.";
    
    /**
     * 시스템 프롬프트 - 진료과 분석
     */
    private String systemPromptAnalysis = "환자의 증상을 분석하여 가장 적절한 진료과를 추천해주세요. 응답은 간결하고 명확하게 해주세요.";
}