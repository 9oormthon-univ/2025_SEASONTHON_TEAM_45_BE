package org.carefreepass.com.carefreepassserver.domain.chat.service;

import java.util.Set;
import java.util.regex.Pattern;

public class AppointmentKeywordMatcher {
    
    private static final Set<String> APPOINTMENT_KEYWORDS = Set.of(
        "예약", "예약해", "예약하", "예약원", "병원"
    );
    
    private static final Set<String> DATE_KEYWORDS = Set.of(
        "내일", "오늘", "모레"
    );
    
    private static final Pattern DATE_PATTERN = Pattern.compile(".*\\d{1,2}월\\s*\\d{1,2}일.*");
    private static final Pattern TIME_PATTERN = Pattern.compile(".*\\d{1,2}시.*");
    private static final Pattern DATE_TIME_COMBINATION = Pattern.compile(".*(날짜.*시간|시간.*날짜).*");
    
    public static boolean containsAppointmentKeywords(String message) {
        if (message == null || message.trim().isEmpty()) {
            return false;
        }
        
        String lowerMessage = message.toLowerCase();
        
        return hasDirectAppointmentKeyword(lowerMessage) ||
               hasDateTimeReference(lowerMessage) ||
               hasDateKeyword(lowerMessage);
    }
    
    private static boolean hasDirectAppointmentKeyword(String message) {
        return APPOINTMENT_KEYWORDS.stream()
                .anyMatch(message::contains);
    }
    
    private static boolean hasDateTimeReference(String message) {
        return DATE_PATTERN.matcher(message).matches() ||
               TIME_PATTERN.matcher(message).matches() ||
               DATE_TIME_COMBINATION.matcher(message).matches();
    }
    
    private static boolean hasDateKeyword(String message) {
        return DATE_KEYWORDS.stream()
                .anyMatch(message::contains);
    }
}