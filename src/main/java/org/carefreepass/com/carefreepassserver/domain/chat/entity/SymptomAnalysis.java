package org.carefreepass.com.carefreepassserver.domain.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.carefreepass.com.carefreepassserver.golbal.domain.BaseTimeEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "symptom_analyses")
public class SymptomAnalysis extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "analysis_id")
    private Long id;

    @Setter
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSession chatSession;

    @Column(name = "extracted_symptoms", columnDefinition = "TEXT")
    private String extractedSymptoms;

    @Column(name = "recommended_department", length = 100)
    private String recommendedDepartment;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "analysis_summary", columnDefinition = "TEXT")
    private String analysisSummary;

    @Column(name = "additional_questions", columnDefinition = "TEXT")
    private String additionalQuestions;

    public static SymptomAnalysis createAnalysis(
            String extractedSymptoms,
            String recommendedDepartment,
            Double confidenceScore,
            String analysisSummary,
            String additionalQuestions) {
        
        SymptomAnalysis analysis = new SymptomAnalysis();
        analysis.extractedSymptoms = extractedSymptoms;
        analysis.recommendedDepartment = recommendedDepartment;
        analysis.confidenceScore = confidenceScore;
        analysis.analysisSummary = analysisSummary;
        analysis.additionalQuestions = additionalQuestions;
        return analysis;
    }

    public void updateAnalysis(
            String extractedSymptoms,
            String recommendedDepartment,
            Double confidenceScore,
            String analysisSummary) {
        
        this.extractedSymptoms = extractedSymptoms;
        this.recommendedDepartment = recommendedDepartment;
        this.confidenceScore = confidenceScore;
        this.analysisSummary = analysisSummary;
    }
}