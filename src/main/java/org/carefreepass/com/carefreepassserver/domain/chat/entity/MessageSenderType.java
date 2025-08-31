package org.carefreepass.com.carefreepassserver.domain.chat.entity;

/**
 * 메시지 발신자 타입 열거형
 * 채팅 메시지를 보낸 주체를 구분합니다.
 */
public enum MessageSenderType {
    /** 환자(사용자)가 보낸 메시지 */
    USER("사용자"),
    
    /** AI 어시스턴트가 보낸 메시지 */
    AI("AI 어시스턴트");

    /** 발신자 타입에 대한 한국어 설명 */
    private final String description;

    /**
     * 메시지 발신자 타입 생성자
     * @param description 발신자 타입에 대한 한국어 설명
     */
    MessageSenderType(String description) {
        this.description = description;
    }

    /**
     * 발신자 타입에 대한 한국어 설명을 반환합니다.
     * @return 발신자 타입 설명
     */
    public String getDescription() {
        return description;
    }
}