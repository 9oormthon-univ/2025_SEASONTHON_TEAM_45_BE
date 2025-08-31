package org.carefreepass.com.carefreepassserver.domain.notification.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    
    private Long id;
    private Long appointmentId;
    private String title;
    private String message;
    private Boolean isSuccess;
    private String errorMessage;
    
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