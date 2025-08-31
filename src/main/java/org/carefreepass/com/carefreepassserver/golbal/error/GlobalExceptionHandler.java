package org.carefreepass.com.carefreepassserver.golbal.error;


import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.carefreepass.com.carefreepassserver.golbal.log.BusinessExceptionLogEntry;
import org.carefreepass.com.carefreepassserver.golbal.log.ExceptionLogEntry;
import org.carefreepass.com.carefreepassserver.golbal.response.ApiResponseTemplate;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private static final String BUSINESS_LOG_MARKER = "BUSINESS-EXCEPTION-LOG";
    private static final String SYSTEM_LOG_MARKER = "EXCEPTION-LOG";

    @ExceptionHandler(BusinessException.class)
    public ApiResponseTemplate<Void> handleBusinessException(
            final BusinessException ex,
            final HttpServletRequest request
    ) {
        doBusinessLog(ex, request);

        final ErrorCode errorCode = ex.getErrorCode();
        final String detail = nullSafe(ex.getMessage(), errorCode.getMessage());

        return ApiResponseTemplate.error()
                .code(errorCode.getCode())
                .message(detail)
                .body(null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ApiResponseTemplate<Void> handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        FieldError fieldError = Objects.requireNonNull(e.getFieldError());

        log.error("Validation error for field {}: {}", fieldError.getField(), fieldError.getDefaultMessage());
        return ApiResponseTemplate.error()
                .code(ErrorCode.REQUIRED_FIELD_MISSING.getCode())
                .message(String.format("%s. (%s)", fieldError.getDefaultMessage(), fieldError.getField()))
                .body(null);

    }

    @ExceptionHandler(Exception.class)
    public ApiResponseTemplate<Void> handleException(
            final Exception ex,
            final HttpServletRequest request
    ) {
        doSystemLog(ex, request);

        return ApiResponseTemplate.error()
                .code(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                .message(ErrorCode.INTERNAL_SERVER_ERROR.getMessage())
                .body(null);
    }

    private void doBusinessLog(final RuntimeException runtimeException, final HttpServletRequest request) {
        final BusinessExceptionLogEntry entry = BusinessExceptionLogEntry.builder()
                .timestamp(OffsetDateTime.now().toString())
                .exceptionType(runtimeException.getClass().getName())
                .message(runtimeException.getMessage())
                .requestUri(request.getRequestURI())
                .build();
        final Marker marker = MarkerFactory.getMarker(BUSINESS_LOG_MARKER);
        log.warn(marker, entry.toLogString());
    }

    private void doSystemLog(final Exception exception, final HttpServletRequest request) {
        // favicon.ico 요청은 로그를 찍지 않음
        if (request.getRequestURI().equals("/favicon.ico")) {
            return;
        }
        
        final ExceptionLogEntry entry = ExceptionLogEntry.builder()
                .timestamp(OffsetDateTime.now().toString())
                .exceptionType(exception.getClass().getName())
                .message(exception.getMessage())
                .requestUri(request.getRequestURI())
                .stackTrace(toStackTraceLog(exception))
                .build();
        final Marker marker = MarkerFactory.getMarker(SYSTEM_LOG_MARKER);
        log.error(marker, entry.toLogString());
    }

    private static String nullSafe(final String primary, final String fallback) {
        return (primary == null || primary.isBlank()) ? fallback : primary;
    }

    private String toStackTraceLog(final Exception exception) {
        return Arrays.stream(exception.getStackTrace())
                .map(StackTraceElement::toString)
                .collect(Collectors.joining("\n"));
    }
}
