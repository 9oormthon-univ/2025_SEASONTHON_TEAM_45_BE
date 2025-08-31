package org.carefreepass.com.carefreepassserver.domain.chat.dto.response;

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

    private final Long sessionId;
    private final String title;
    private final ChatSessionStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final List<ChatMessageResponse> messages;
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