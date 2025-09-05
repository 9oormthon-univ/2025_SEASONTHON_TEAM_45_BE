package org.carefreepass.com.carefreepassserver.domain.chat.util;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * 예약 키워드 매칭 유틸리티 클래스
 * 사용자 메시지에서 예약 의도를 감지하는 정적 유틸리티 메서드 제공
 */
public final class AppointmentKeywordMatcher {
    
    // 예약 관련 직접 키워드
    private static final Set<String> APPOINTMENT_KEYWORDS = Set.of(
        "예약", "예약해", "예약하", "예약원", "병원"
    );
    
    // 날짜 관련 키워드
    private static final Set<String> DATE_KEYWORDS = Set.of(
        "내일", "오늘", "모레"
    );
    
    // 날짜 패턴 정규식
    private static final Pattern DATE_PATTERN = Pattern.compile(".*\\d{1,2}월\\s*\\d{1,2}일.*");
    
    // 시간 패턴 정규식
    private static final Pattern TIME_PATTERN = Pattern.compile(".*\\d{1,2}시.*");
    
    // 날짜와 시간이 함께 언급된 패턴
    private static final Pattern DATE_TIME_COMBINATION = Pattern.compile(".*(날짜.*시간|시간.*날짜).*");
    
    // 유틸리티 클래스이므로 인스턴스화 방지
    private AppointmentKeywordMatcher() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * 메시지에 예약 관련 키워드가 포함되어 있는지 확인
     *
     * @param message 확인할 메시지
     * @return 예약 키워드 포함 여부
     */
    public static boolean containsAppointmentKeywords(String message) {
        if (isNullOrEmpty(message)) {
            return false;
        }
        
        String lowerMessage = message.toLowerCase();
        
        return hasDirectAppointmentKeyword(lowerMessage) ||
               hasDateTimeReference(lowerMessage) ||
               hasDateKeyword(lowerMessage);
    }
    
    /**
     * 직접적인 예약 키워드 확인
     */
    private static boolean hasDirectAppointmentKeyword(String message) {
        return APPOINTMENT_KEYWORDS.stream()
                .anyMatch(message::contains);
    }
    
    /**
     * 날짜/시간 참조 확인
     */
    private static boolean hasDateTimeReference(String message) {
        return DATE_PATTERN.matcher(message).matches() ||
               TIME_PATTERN.matcher(message).matches() ||
               DATE_TIME_COMBINATION.matcher(message).matches();
    }
    
    /**
     * 상대적 날짜 키워드 확인
     */
    private static boolean hasDateKeyword(String message) {
        return DATE_KEYWORDS.stream()
                .anyMatch(message::contains);
    }
    
    /**
     * null이나 빈 문자열 확인 유틸리티
     */
    private static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}