package org.carefreepass.com.carefreepassserver.domain.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.carefreepass.com.carefreepassserver.domain.chat.entity.SymptomAnalysis;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SymptomAnalysisResponse {

    @Schema(description = "증상 분석 ID", example = "1")
    private final Long analysisId;
    
    @Schema(description = "추출된 증상 정보", example = "두통, 발열")
    private final String extractedSymptoms;
    
    @Schema(description = "추천 진료과", example = "내과")
    private final String recommendedDepartment;
    
    @Schema(description = "신뢰도 점수 (0.0-1.0)", example = "0.85")
    private final Double confidenceScore;
    
    @Schema(description = "분석 요약", example = "두통과 발열 증상을 고려할 때 내과 진료를 추천합니다.")
    private final String analysisSummary;
    
    @Schema(description = "추가 질문 사항", example = "언제부터 증상이 시작되었나요?")
    private final String additionalQuestions;

    public static SymptomAnalysisResponse from(SymptomAnalysis analysis) {
        return new SymptomAnalysisResponse(
                analysis.getId(),
                analysis.getExtractedSymptoms(),
                analysis.getRecommendedDepartment(),
                analysis.getConfidenceScore(),
                analysis.getAnalysisSummary(),
                analysis.getAdditionalQuestions()
        );
    }
}