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
public class ChatStartRequest {

    @Schema(description = "회원 ID", example = "1")
    @NotNull(message = "회원 ID는 필수입니다.")
    private Long memberId;

    @Schema(description = "채팅 시작 메시지", example = "머리가 아파서 병원에 가야 할 것 같아요")
    @NotBlank(message = "초기 메시지는 필수입니다.")
    private String initialMessage;
}