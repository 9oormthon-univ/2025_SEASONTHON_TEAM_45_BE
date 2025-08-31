package org.carefreepass.com.carefreepassserver.domain.chat.service;

import lombok.extern.slf4j.Slf4j;
import org.carefreepass.com.carefreepassserver.domain.chat.entity.ChatMessage;
import org.carefreepass.com.carefreepassserver.domain.chat.entity.MessageSenderType;
import org.carefreepass.com.carefreepassserver.domain.chat.entity.SymptomAnalysis;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class AppointmentInfoExtractor {
    
    private static final int CONVERSATION_HISTORY_LIMIT = 3;
    private static final Pattern DATE_PATTERN = Pattern.compile(
        "(\\d{1,2})월\\s*(\\d{1,2})일|(\\d{4})-(\\d{1,2})-(\\d{1,2})|(\\d{1,2})/(\\d{1,2})|내일|오늘|모레"
    );
    private static final Pattern TIME_PATTERN = Pattern.compile(
        "(\\d{1,2})시\\s*(\\d{1,2}분)?|(\\d{1,2}):(\\d{2})|오전\\s*(\\d{1,2})시|오후\\s*(\\d{1,2})시"
    );
    
    public AppointmentInfo extractAppointmentInfo(String userMessage, List<ChatMessage> history, SymptomAnalysis analysis) {
        AppointmentInfo info = new AppointmentInfo();
        info.setDepartment(analysis.getRecommendedDepartment());
        
        extractDateTimeFromMessage(userMessage, info);
        extractDateTimeFromHistory(history, info);
        
        return info;
    }
    
    private void extractDateTimeFromMessage(String message, AppointmentInfo info) {
        extractDate(message, info);
        extractTime(message, info);
    }
    
    private void extractDate(String message, AppointmentInfo info) {
        Matcher dateMatcher = DATE_PATTERN.matcher(message);
        if (dateMatcher.find()) {
            String match = dateMatcher.group();
            LocalDate date = parseDate(match);
            if (date != null) {
                info.setAppointmentDate(date);
            }
        }
    }
    
    private void extractTime(String message, AppointmentInfo info) {
        Matcher timeMatcher = TIME_PATTERN.matcher(message);
        if (timeMatcher.find()) {
            String match = timeMatcher.group();
            LocalTime time = parseTime(match);
            if (time != null) {
                info.setAppointmentTime(time);
            }
        }
    }
    
    private void extractDateTimeFromHistory(List<ChatMessage> history, AppointmentInfo info) {
        int startIndex = Math.max(0, history.size() - CONVERSATION_HISTORY_LIMIT);
        for (int i = startIndex; i < history.size(); i++) {
            ChatMessage message = history.get(i);
            if (message.getSenderType() == MessageSenderType.USER) {
                extractDateTimeFromMessage(message.getContent(), info);
            }
        }
    }
    
    private LocalDate parseDate(String dateStr) {
        try {
            LocalDate now = LocalDate.now();
            
            return switch (dateStr) {
                case String s when s.contains("오늘") -> now;
                case String s when s.contains("내일") -> now.plusDays(1);
                case String s when s.contains("모레") -> now.plusDays(2);
                default -> parseComplexDate(dateStr, now);
            };
            
        } catch (Exception e) {
            log.warn("날짜 파싱 실패: {}", dateStr, e);
            return null;
        }
    }
    
    private LocalDate parseComplexDate(String dateStr, LocalDate now) {
        Pattern monthDay = Pattern.compile("(\\d{1,2})월\\s*(\\d{1,2})일");
        Matcher matcher = monthDay.matcher(dateStr);
        if (matcher.find()) {
            int month = Integer.parseInt(matcher.group(1));
            int day = Integer.parseInt(matcher.group(2));
            return LocalDate.of(now.getYear(), month, day);
        }
        return null;
    }
    
    private LocalTime parseTime(String timeStr) {
        try {
            Pattern hourPattern = Pattern.compile("(오전|오후)?\\s*(\\d{1,2})시");
            Matcher matcher = hourPattern.matcher(timeStr);
            
            if (matcher.find()) {
                return parseHourTime(matcher);
            }
            
            return parseClockTime(timeStr);
            
        } catch (Exception e) {
            log.warn("시간 파싱 실패: {}", timeStr, e);
            return null;
        }
    }
    
    private LocalTime parseHourTime(Matcher matcher) {
        String ampm = matcher.group(1);
        int hour = Integer.parseInt(matcher.group(2));
        
        if ("오후".equals(ampm) && hour < 12) {
            hour += 12;
        } else if ("오전".equals(ampm) && hour == 12) {
            hour = 0;
        }
        
        return LocalTime.of(hour, 0);
    }
    
    private LocalTime parseClockTime(String timeStr) {
        Pattern timePattern = Pattern.compile("(\\d{1,2}):(\\d{2})");
        Matcher matcher = timePattern.matcher(timeStr);
        if (matcher.find()) {
            int hour = Integer.parseInt(matcher.group(1));
            int minute = Integer.parseInt(matcher.group(2));
            return LocalTime.of(hour, minute);
        }
        return null;
    }
}