package org.carefreepass.com.carefreepassserver.domain.notification.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.carefreepass.com.carefreepassserver.domain.notification.entity.NotificationHistory;

import java.time.LocalDateTime;

/**
 * 알림 이력 응답 DTO
 * 알림 전송 이력을 클라이언트에 전달하기 위한 데이터 모델
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationHistoryResponse {
    
    @Schema(description = "알림 이력 ID", example = "1")
    private Long id;
    
    @Schema(description = "예약 ID", example = "1")
    private Long appointmentId;
    
    @Schema(description = "알림 제목", example = "진료 예약 알림")
    private String title;
    
    @Schema(description = "알림 내용", example = "오늘 오후 2시에 예약이 있습니다.")
    private String message;
    
    @Schema(description = "알림 전송 성공 여부", example = "true")
    private Boolean isSuccess;
    
    @Schema(description = "오류 메시지", example = "null")
    private String errorMessage;
    
    @Schema(description = "알림 생성 시각", example = "2024-12-31 14:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * NotificationHistory 엔티티를 응답 DTO로 변환
     * @param history 알림 이력 엔티티
     * @return 응답 DTO
     */
    public static NotificationHistoryResponse from(NotificationHistory history) {
        return NotificationHistoryResponse.builder()
                .id(history.getId())
                .appointmentId(history.getAppointment() != null ? history.getAppointment().getId() : null)
                .title(history.getTitle())
                .message(history.getMessage())
                .isSuccess(history.getIsSuccess())
                .errorMessage(history.getErrorMessage())
                .createdAt(history.getCreatedAt())
                .build();
    }
}