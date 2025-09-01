package org.carefreepass.com.carefreepassserver.domain.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.carefreepass.com.carefreepassserver.domain.appointment.dto.request.AppointmentCreateRequest;
import org.carefreepass.com.carefreepassserver.domain.appointment.dto.response.TimeSlotResponse;
import org.carefreepass.com.carefreepassserver.domain.appointment.service.AppointmentService;
import org.carefreepass.com.carefreepassserver.domain.appointment.service.TimeSlotService;
import org.carefreepass.com.carefreepassserver.domain.chat.entity.ChatMessage;
import org.carefreepass.com.carefreepassserver.domain.chat.entity.ChatSession;
import org.carefreepass.com.carefreepassserver.domain.chat.entity.SymptomAnalysis;
import org.carefreepass.com.carefreepassserver.domain.chat.repository.SymptomAnalysisRepository;
import org.carefreepass.com.carefreepassserver.golbal.config.ChatProperties;
import org.carefreepass.com.carefreepassserver.golbal.error.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentBookingService {
    
    private final ChatProperties chatProperties;
    private final AppointmentService appointmentService;
    private final TimeSlotService timeSlotService;
    private final SymptomAnalysisRepository symptomAnalysisRepository;
    private final AppointmentInfoExtractor appointmentInfoExtractor;
    
    @Transactional
    public String tryCreateAppointment(ChatSession session, String userMessage, List<ChatMessage> conversationHistory) {
        if (!AppointmentKeywordMatcher.containsAppointmentKeywords(userMessage)) {
            return null;
        }
        
        try {
            Optional<SymptomAnalysis> analysisOpt = symptomAnalysisRepository.findByChatSessionId(session.getId());
            if (analysisOpt.isEmpty()) {
                return "먼저 증상을 알려주시면 적절한 진료과를 추천해드리겠습니다.";
            }
            
            SymptomAnalysis analysis = analysisOpt.get();
            AppointmentInfo appointmentInfo = appointmentInfoExtractor.extractAppointmentInfo(userMessage, conversationHistory, analysis);
            
            if (!appointmentInfo.isValid()) {
                return generateAppointmentInfoRequest(appointmentInfo, analysis);
            }
            
            // 예약 생성 전에 해당 시간이 실제로 예약 가능한지 확인
            if (!isTimeSlotActuallyAvailable(analysis.getRecommendedDepartment(), 
                    appointmentInfo.getAppointmentDate(), appointmentInfo.getAppointmentTime())) {
                return generateTimeNotAvailableMessage(analysis.getRecommendedDepartment(), 
                        appointmentInfo.getAppointmentDate(), appointmentInfo.getAppointmentTime());
            }
            
            Long appointmentId = createAppointment(session, appointmentInfo);
            log.info("AI 챗봇을 통한 예약 생성 성공: 예약 ID = {}, 회원 ID = {}", 
                    appointmentId, session.getMember().getId());
            
            return generateAppointmentSuccessMessage(appointmentInfo, appointmentId);
            
        } catch (BusinessException e) {
            return handleBusinessLogicError(e);
        } catch (Exception e) {
            return handleUnexpectedError(e);
        }
    }
    
    private Long createAppointment(ChatSession session, AppointmentInfo info) {
        // 구름대병원 ID를 1로 가정 (실제 환경에서는 설정으로 관리)
        Long hospitalId = 1L;
        
        AppointmentCreateRequest request = new AppointmentCreateRequest(
            session.getMember().getId(),
            hospitalId,
            info.getDepartment(),
            info.getAppointmentDate(),
            info.getAppointmentTime()
        );
        
        return appointmentService.createAppointment(request);
    }
    
    private String handleBusinessLogicError(BusinessException e) {
        return "죄송합니다. " + e.getErrorCode().getMessage() + "\n다른 날짜나 시간을 선택해주시겠어요?";
    }
    
    private String handleUnexpectedError(Exception e) {
        log.error("예약 생성 중 오류 발생: {}", e.getMessage(), e);
        return "예약 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
    }
    
    private String generateAppointmentInfoRequest(AppointmentInfo info, SymptomAnalysis analysis) {
        StringBuilder response = new StringBuilder();
        response.append("🏥 ").append(analysis.getRecommendedDepartment()).append(" 예약을 도와드리겠습니다!\n\n");
        
        // 날짜가 있는 경우 해당 날짜의 가능한 시간 표시
        if (info.getAppointmentDate() != null && info.getAppointmentTime() == null) {
            String availableTimes = getAvailableTimesMessage(analysis.getRecommendedDepartment(), info.getAppointmentDate());
            response.append(availableTimes);
        }
        // 날짜가 없는 경우 날짜 입력 요청
        else if (info.getAppointmentDate() == null) {
            response.append("📅 원하시는 예약 날짜를 알려주세요.\n");
            response.append("예) 내일, 9월 2일, 9/2\n\n");
        }
        
        // 시간이 없는 경우 시간 입력 요청 (날짜가 없을 때만)
        if (info.getAppointmentTime() == null && info.getAppointmentDate() == null) {
            response.append("🕐 원하시는 예약 시간을 알려주세요.\n");
            response.append("예) 오후 2시, 14:00, 2시\n\n");
        }
        
        if (info.getAppointmentDate() == null || info.getAppointmentTime() == null) {
            response.append("날짜와 시간을 함께 말씀해주시면 더 빠르게 도와드릴 수 있습니다!");
        }
        
        return response.toString();
    }

    /**
     * 특정 날짜의 예약 가능한 시간을 안내하는 메시지 생성
     */
    private String getAvailableTimesMessage(String departmentName, LocalDate date) {
        try {
            // 구름대병원 ID를 1로 가정
            Long hospitalId = 1L;
            List<TimeSlotResponse> timeSlots = timeSlotService.getAvailableTimeSlots(hospitalId, departmentName, date);
            
            List<TimeSlotResponse> availableSlots = timeSlots.stream()
                    .filter(TimeSlotResponse::getAvailable)
                    .toList();
                    
            StringBuilder message = new StringBuilder();
            message.append("📅 ").append(date.getMonthValue()).append("월 ").append(date.getDayOfMonth()).append("일 예약 가능 시간:\n\n");
            
            if (availableSlots.isEmpty()) {
                message.append("❌ 해당 날짜에는 예약 가능한 시간이 없습니다.\n");
                message.append("다른 날짜를 선택해 주세요.\n\n");
            } else {
                message.append("✅ 예약 가능한 시간:\n");
                for (int i = 0; i < availableSlots.size() && i < 8; i++) { // 최대 8개만 표시
                    TimeSlotResponse slot = availableSlots.get(i);
                    message.append("• ").append(formatTimeForUser(slot.getTime())).append("\n");
                }
                
                if (availableSlots.size() > 8) {
                    message.append("• 그 외 ").append(availableSlots.size() - 8).append("개 시간대\n");
                }
                message.append("\n원하시는 시간을 말씀해 주세요!\n\n");
            }
            
            return message.toString();
        } catch (Exception e) {
            log.error("예약 가능 시간 조회 실패: {}", e.getMessage());
            return "🕐 원하시는 예약 시간을 알려주세요.\n예) 오후 2시, 14:00, 2시\n\n";
        }
    }

    /**
     * 시간을 사용자 친화적으로 포맷
     */
    private String formatTimeForUser(LocalTime time) {
        int hour = time.getHour();
        int minute = time.getMinute();
        
        if (hour < 12) {
            if (hour == 0) {
                return String.format("오전 12:%02d", minute);
            } else {
                return String.format("오전 %d:%02d", hour, minute);
            }
        } else {
            if (hour == 12) {
                return String.format("오후 12:%02d", minute);
            } else {
                return String.format("오후 %d:%02d", hour - 12, minute);
            }
        }
    }

    /**
     * 실제로 해당 시간이 예약 가능한지 확인
     */
    private boolean isTimeSlotActuallyAvailable(String departmentName, LocalDate date, LocalTime time) {
        try {
            Long hospitalId = 1L; // 구름대병원
            return timeSlotService.isTimeSlotAvailable(hospitalId, departmentName, date, time);
        } catch (Exception e) {
            log.error("시간 가용성 확인 실패: {}", e.getMessage());
            return false; // 확인 실패 시 안전하게 불가능으로 처리
        }
    }

    /**
     * 요청한 시간이 예약 불가능할 때의 메시지 생성
     */
    private String generateTimeNotAvailableMessage(String departmentName, LocalDate date, LocalTime requestedTime) {
        StringBuilder response = new StringBuilder();
        response.append("😔 죄송합니다. ").append(formatTimeForUser(requestedTime))
                .append("은 이미 예약되었거나 예약이 불가능한 시간입니다.\n\n");
        
        // 해당 날짜의 다른 가능한 시간들 제안
        String alternativeTimes = getAvailableTimesMessage(departmentName, date);
        response.append(alternativeTimes);
        
        return response.toString();
    }
    
    private String generateAppointmentSuccessMessage(AppointmentInfo info, Long appointmentId) {
        return String.format(
            "✅ 예약이 성공적으로 완료되었습니다!\n\n" +
            "📋 예약 정보:\n" +
            "• 병원: %s\n" +
            "• 진료과: %s\n" +
            "• 날짜: %s\n" +
            "• 시간: %s\n" +
            "• 예약번호: #%d\n\n" +
            "🔔 예약 확인 알림이 전송되었습니다.\n" +
            "진료 당일 접수 후 대기해주세요. 감사합니다!",
            info.getHospitalName(),
            info.getDepartment(), 
            info.getAppointmentDate().format(DateTimeFormatter.ofPattern("yyyy년 M월 d일")),
            info.getAppointmentTime().format(DateTimeFormatter.ofPattern("HH:mm")),
            appointmentId
        );
    }
}