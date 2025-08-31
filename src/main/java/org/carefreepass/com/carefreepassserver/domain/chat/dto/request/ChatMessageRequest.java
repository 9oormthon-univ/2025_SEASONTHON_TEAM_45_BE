package org.carefreepass.com.carefreepassserver.domain.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ChatMessageRequest {

    @Schema(description = "채팅 세션 ID", example = "1")
    @NotNull(message = "세션 ID는 필수입니다.")
    private Long sessionId;

    @Schema(description = "회원 ID", example = "1")
    @NotNull(message = "회원 ID는 필수입니다.")
    private Long memberId;

    @Schema(description = "채팅 메시지 내용", example = "머리가 아파요")
    @NotBlank(message = "메시지 내용은 필수입니다.")
    private String content;
}