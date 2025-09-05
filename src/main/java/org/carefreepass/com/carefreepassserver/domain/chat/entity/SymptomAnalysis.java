package org.carefreepass.com.carefreepassserver.domain.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import org.carefreepass.com.carefreepassserver.golbal.domain.BaseTimeEntity;

// 증상 분석 엔티티 - AI가 채팅 내용 분석하여 도출한 증상 정보와 추천 진료과 저장 (신뢰도 점수 및 추가 질문 포함)
@Entity
@Getter  
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "symptom_analyses")
public class SymptomAnalysis extends BaseTimeEntity {

    // 증상 분석 결과 고유 식별자
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "analysis_id")
    private Long id;

    // 이 분석이 속한 채팅 세션
    @Setter
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSession chatSession;

    // AI가 추출한 증상 정보 (예: "두통, 발열, 메스꺼움")
    @Lob
    @Column(name = "extracted_symptoms")
    private String extractedSymptoms;

    // AI가 추천한 진료과 (예: "내과", "신경과")
    @Column(name = "recommended_department", length = 100)
    private String recommendedDepartment;

    // AI 분석의 신뢰도 점수 (0.0 ~ 1.0)
    @Column(name = "confidence_score")
    private Double confidenceScore;

    // 증상 분석 요약
    @Lob
    @Column(name = "analysis_summary")
    private String analysisSummary;

    // 더 정확한 진단을 위한 추가 질문 사항
    @Lob
    @Column(name = "additional_questions")
    private String additionalQuestions;

    // 증상 분석 결과 업데이트 메소드
    public void updateAnalysis(String extractedSymptoms, String recommendedDepartment, 
                              Double confidenceScore, String analysisSummary) {
        this.extractedSymptoms = extractedSymptoms;
        this.recommendedDepartment = recommendedDepartment;
        this.confidenceScore = confidenceScore;
        this.analysisSummary = analysisSummary;
    }
}

