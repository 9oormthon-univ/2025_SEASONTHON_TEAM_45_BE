package org.carefreepass.com.carefreepassserver.domain.chat.entity;

public enum ChatSessionStatus {
    ACTIVE("진행중"),
    COMPLETED("완료"),
    CANCELLED("취소");

    private final String description;

    ChatSessionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}