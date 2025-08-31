package org.carefreepass.com.carefreepassserver.domain.chat.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.carefreepass.com.carefreepassserver.domain.chat.entity.SymptomAnalysis;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SymptomAnalysisResponse {

    private final Long analysisId;
    private final String extractedSymptoms;
    private final String recommendedDepartment;
    private final Double confidenceScore;
    private final String analysisSummary;
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