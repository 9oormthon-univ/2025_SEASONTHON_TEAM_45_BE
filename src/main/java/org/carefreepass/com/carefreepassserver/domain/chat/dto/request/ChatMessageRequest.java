package org.carefreepass.com.carefreepassserver.domain.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessageRequest {

    @NotNull(message = "세션 ID는 필수입니다.")
    private Long sessionId;

    @NotNull(message = "회원 ID는 필수입니다.")
    private Long memberId;

    @NotBlank(message = "메시지 내용은 필수입니다.")
    private String content;
}