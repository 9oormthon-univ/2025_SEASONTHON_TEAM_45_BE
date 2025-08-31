package org.carefreepass.com.carefreepassserver.domain.chat.entity;

public enum MessageSenderType {
    USER("사용자"),
    AI("AI 어시스턴트");

    private final String description;

    MessageSenderType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}