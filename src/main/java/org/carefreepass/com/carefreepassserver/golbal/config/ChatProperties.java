package org.carefreepass.com.carefreepassserver.golbal.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * AI 채팅 관련 설정값들을 관리하는 Properties 클래스
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.chat")
public class ChatProperties {
    
    /**
     * 기본 병원명
     */
    private String defaultHospitalName = "서울대병원";
    
    /**
     * 기본 세션 제목
     */
    private String defaultSessionTitle = "AI 예약 상담";
    
    /**
     * 진료과 추천 신뢰도 임계값
     */
    private double confidenceThreshold = 0.7;
    
    /**
     * 대화 히스토리 제한 개수
     */
    private int conversationHistoryLimit = 3;
    
    
    /**
     * 사용 가능한 진료과 목록
     */
    private List<String> availableDepartments = List.of(
        "내과", "외과", "정형외과", "피부과", "이비인후과", 
        "안과", "산부인과", "소아과", "정신과", "치과"
    );
}