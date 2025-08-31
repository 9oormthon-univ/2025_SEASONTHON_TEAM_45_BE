package org.carefreepass.com.carefreepassserver.domain.chat.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.carefreepass.com.carefreepassserver.domain.chat.dto.request.ChatMessageRequest;
import org.carefreepass.com.carefreepassserver.domain.chat.dto.request.ChatStartRequest;
import org.carefreepass.com.carefreepassserver.domain.chat.dto.response.ChatMessageResponse;
import org.carefreepass.com.carefreepassserver.domain.chat.dto.response.ChatSessionResponse;
import org.carefreepass.com.carefreepassserver.golbal.response.ApiResponseTemplate;

import java.util.List;

@Tag(name = "Chat", description = "AI 챗봇 예약 상담 API")
public interface ChatDocs {

    @Operation(
        summary = "AI 채팅 세션 시작",
        description = "사용자가 증상을 입력하여 AI와의 채팅 세션을 시작합니다. " +
                     "AI가 증상을 분석하고 적절한 진료과를 추천합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "채팅 세션 시작 성공",
            content = @Content(schema = @Schema(implementation = ChatSessionResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 데이터",
            content = @Content(schema = @Schema(implementation = ApiResponseTemplate.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "존재하지 않는 회원",
            content = @Content(schema = @Schema(implementation = ApiResponseTemplate.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(schema = @Schema(implementation = ApiResponseTemplate.class))
        )
    })
    ApiResponseTemplate<ChatSessionResponse> startChatSession(ChatStartRequest request);

    @Operation(
        summary = "채팅 메시지 전송",
        description = "진행 중인 채팅 세션에 메시지를 전송하고 AI 응답을 받습니다. " +
                     "AI는 사용자의 추가 증상 정보를 바탕으로 더 정확한 진단과 예약 안내를 제공합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "메시지 전송 성공",
            content = @Content(schema = @Schema(implementation = ChatMessageResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 데이터 또는 비활성 세션",
            content = @Content(schema = @Schema(implementation = ApiResponseTemplate.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "존재하지 않는 채팅 세션",
            content = @Content(schema = @Schema(implementation = ApiResponseTemplate.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(schema = @Schema(implementation = ApiResponseTemplate.class))
        )
    })
    ApiResponseTemplate<ChatMessageResponse> sendMessage(ChatMessageRequest request);

    @Operation(
        summary = "사용자 채팅 세션 목록 조회",
        description = "특정 사용자의 모든 채팅 세션 목록을 조회합니다. " +
                     "최신 세션부터 내림차순으로 정렬되며, 세션별 기본 정보만 포함됩니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "채팅 세션 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = ApiResponseTemplate.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(schema = @Schema(implementation = ApiResponseTemplate.class))
        )
    })
    ApiResponseTemplate<List<ChatSessionResponse>> getUserChatSessions(
        @Parameter(description = "회원 ID", required = true) Long memberId
    );

    @Operation(
        summary = "채팅 세션 상세 조회",
        description = "특정 채팅 세션의 상세 정보를 조회합니다. " +
                     "모든 메시지 히스토리와 증상 분석 결과를 포함합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "채팅 세션 상세 조회 성공",
            content = @Content(schema = @Schema(implementation = ChatSessionResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "접근할 수 없는 채팅 세션",
            content = @Content(schema = @Schema(implementation = ApiResponseTemplate.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "존재하지 않는 채팅 세션",
            content = @Content(schema = @Schema(implementation = ApiResponseTemplate.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(schema = @Schema(implementation = ApiResponseTemplate.class))
        )
    })
    ApiResponseTemplate<ChatSessionResponse> getChatSession(
        @Parameter(description = "채팅 세션 ID", required = true) Long sessionId,
        @Parameter(description = "회원 ID", required = true) Long memberId
    );

    @Operation(
        summary = "채팅 세션 완료",
        description = "진행 중인 채팅 세션을 완료 처리합니다. " +
                     "완료된 세션은 더 이상 메시지를 주고받을 수 없습니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "채팅 세션 완료 성공",
            content = @Content(schema = @Schema(implementation = ApiResponseTemplate.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "접근할 수 없는 채팅 세션",
            content = @Content(schema = @Schema(implementation = ApiResponseTemplate.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "존재하지 않는 채팅 세션",
            content = @Content(schema = @Schema(implementation = ApiResponseTemplate.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(schema = @Schema(implementation = ApiResponseTemplate.class))
        )
    })
    ApiResponseTemplate<String> completeChatSession(
        @Parameter(description = "채팅 세션 ID", required = true) Long sessionId,
        @Parameter(description = "회원 ID", required = true) Long memberId
    );
}