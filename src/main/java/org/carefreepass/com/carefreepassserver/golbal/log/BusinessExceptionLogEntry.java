package org.carefreepass.com.carefreepassserver.golbal.log;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BusinessExceptionLogEntry {

    private String timestamp;
    private String exceptionType;
    private String message;
    private String requestUri;
    private String stackTrace;

    public String toLogString() {
        return String.format("""
            
            [BUSINESS-EXCEPTION]
              Time          : %s
              ExceptionType : %s
              Message       : %s
              Request URI   : %s
            -------------------- Stack Trace -------------------
            %s
            ----------------------------------------------------
            """,
                timestamp,
                exceptionType,
                message,
                requestUri,
                stackTrace
        );
    }
}
