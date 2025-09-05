package org.carefreepass.com.carefreepassserver.domain.chat.entity;

// 채팅 세션 상태 열거형 - 채팅 세션의 진행 상태 표시
public enum ChatSessionStatus {
    // 채팅 세션이 진행 중인 상태
    ACTIVE("진행중"),
    
    // 채팅 세션이 정상적으로 완료된 상태
    COMPLETED("완료"),
    
    // 채팅 세션이 취소된 상태
    CANCELLED("취소");

    // 상태에 대한 한국어 설명
    private final String description;

    // 채팅 세션 상태 생성자
    ChatSessionStatus(String description) {
        this.description = description;
    }

    // 상태에 대한 한국어 설명 반환
    public String getDescription() {
        return description;
    }
}