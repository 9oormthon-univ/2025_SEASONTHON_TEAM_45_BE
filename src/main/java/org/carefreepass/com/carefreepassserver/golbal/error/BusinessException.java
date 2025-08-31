package org.carefreepass.com.carefreepassserver.golbal.error;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(final ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(
            final ErrorCode errorCode,
            final String detailMessage
    ) {
        super("%s - %s".formatted(errorCode.getMessage(), detailMessage));
        this.errorCode = errorCode;
    }
}