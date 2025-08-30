package org.carefreepass.com.carefreepassserver.golbal.error;

public record ErrorResponse(
        String code,
        String message
) {
    public static ErrorResponse of(final String code, final String message) {
        return new ErrorResponse(code, message);
    }
}
