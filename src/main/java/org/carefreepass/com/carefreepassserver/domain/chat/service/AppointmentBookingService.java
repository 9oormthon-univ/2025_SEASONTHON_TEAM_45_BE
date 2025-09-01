package org.carefreepass.com.carefreepassserver.domain.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.carefreepass.com.carefreepassserver.domain.appointment.dto.request.AppointmentCreateRequest;
import org.carefreepass.com.carefreepassserver.domain.appointment.service.AppointmentService;
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
        String hospitalName = info.getHospitalName() != null ? 
            info.getHospitalName() : chatProperties.getDefaultHospitalName();
            
        AppointmentCreateRequest request = new AppointmentCreateRequest(
            session.getMember().getId(),
            hospitalName,
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
        
        if (info.getAppointmentDate() == null) {
            response.append("📅 원하시는 예약 날짜를 알려주세요.\n");
            response.append("예) 8월 31일, 내일, 9/1\n\n");
        }
        
        if (info.getAppointmentTime() == null) {
            response.append("🕐 원하시는 예약 시간을 알려주세요.\n");
            response.append("예) 오후 2시, 14:00, 2시\n\n");
        }
        
        if (info.getAppointmentDate() == null || info.getAppointmentTime() == null) {
            response.append("날짜와 시간을 함께 말씀해주시면 더 빠르게 도와드릴 수 있습니다!");
        }
        
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