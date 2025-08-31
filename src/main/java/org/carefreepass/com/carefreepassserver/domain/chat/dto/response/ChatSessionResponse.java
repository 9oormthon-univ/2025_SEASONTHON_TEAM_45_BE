package org.carefreepass.com.carefreepassserver.domain.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.carefreepass.com.carefreepassserver.domain.chat.entity.ChatSession;
import org.carefreepass.com.carefreepassserver.domain.chat.entity.ChatSessionStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatSessionResponse {

    @Schema(description = "채팅 세션 ID", example = "1")
    private final Long sessionId;
    
    @Schema(description = "채팅 세션 제목", example = "증상 상담")
    private final String title;
    
    @Schema(description = "채팅 세션 상태", example = "ACTIVE")
    private final ChatSessionStatus status;
    
    @Schema(description = "세션 생성 시간", example = "2024-12-31T14:30:00")
    private final LocalDateTime createdAt;
    
    @Schema(description = "세션 마지막 업데이트 시간", example = "2024-12-31T14:35:00")
    private final LocalDateTime updatedAt;
    
    @Schema(description = "채팅 메시지 목록")
    private final List<ChatMessageResponse> messages;
    
    @Schema(description = "증상 분석 결과")
    private final SymptomAnalysisResponse symptomAnalysis;

    public static ChatSessionResponse from(ChatSession session) {
        List<ChatMessageResponse> messageResponses = session.getMessages().stream()
                .map(ChatMessageResponse::from)
                .toList();

        SymptomAnalysisResponse analysisResponse = session.getSymptomAnalysis() != null
                ? SymptomAnalysisResponse.from(session.getSymptomAnalysis())
                : null;

        return new ChatSessionResponse(
                session.getId(),
                session.getTitle(),
                session.getStatus(),
                session.getCreatedAt(),
                session.getUpdatedAt(),
                messageResponses,
                analysisResponse
        );
    }

    public static ChatSessionResponse withoutMessages(ChatSession session) {
        SymptomAnalysisResponse analysisResponse = session.getSymptomAnalysis() != null
                ? SymptomAnalysisResponse.from(session.getSymptomAnalysis())
                : null;

        return new ChatSessionResponse(
                session.getId(),
                session.getTitle(),
                session.getStatus(),
                session.getCreatedAt(),
                session.getUpdatedAt(),
                List.of(),
                analysisResponse
        );
    }
}