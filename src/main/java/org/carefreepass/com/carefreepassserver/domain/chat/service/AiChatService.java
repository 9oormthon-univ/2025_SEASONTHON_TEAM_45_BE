package org.carefreepass.com.carefreepassserver.domain.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.carefreepass.com.carefreepassserver.domain.appointment.service.AppointmentService;
import org.carefreepass.com.carefreepassserver.domain.chat.entity.*;
import org.carefreepass.com.carefreepassserver.domain.chat.repository.*;
import org.carefreepass.com.carefreepassserver.domain.member.entity.Member;
import org.carefreepass.com.carefreepassserver.domain.member.repository.MemberRepository;
import org.carefreepass.com.carefreepassserver.golbal.config.ChatProperties;
import org.carefreepass.com.carefreepassserver.golbal.error.BusinessException;
import org.carefreepass.com.carefreepassserver.golbal.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AiChatService {
    
    private final ChatProperties chatProperties;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SymptomAnalysisRepository symptomAnalysisRepository;
    private final MemberRepository memberRepository;
    private final OpenAIService openAIService;
    private final AppointmentBookingService appointmentBookingService;

    @Transactional
    public ChatSession startNewChatSession(Long memberId, String initialMessage) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 새로운 채팅 세션 생성
        ChatSession session = ChatSession.createSession(member, chatProperties.getDefaultSessionTitle());
        ChatSession savedSession = chatSessionRepository.save(session);

        // 첫 번째 사용자 메시지 저장
        ChatMessage userMessage = ChatMessage.createUserMessage(initialMessage, 1);
        savedSession.addMessage(userMessage);
        chatMessageRepository.save(userMessage);

        // AI 응답 생성 및 저장
        String aiResponse = generateAiResponse(savedSession, initialMessage);
        ChatMessage aiMessage = ChatMessage.createAiMessage(aiResponse, 2);
        savedSession.addMessage(aiMessage);
        chatMessageRepository.save(aiMessage);

        log.info("새로운 채팅 세션 시작: 회원 ID = {}, 세션 ID = {}", memberId, savedSession.getId());
        return savedSession;
    }

    @Transactional
    public ChatMessage sendMessage(Long sessionId, Long memberId, String messageContent) {
        // 세션 검증
        ChatSession session = chatSessionRepository.findByIdAndMemberIdWithMessages(sessionId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_SESSION_ACCESS_DENIED));

        if (session.getStatus() != ChatSessionStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.CHAT_SESSION_EXPIRED);
        }

        // 사용자 메시지 저장
        int nextSequence = getNextSequenceNumber(sessionId);
        ChatMessage userMessage = ChatMessage.createUserMessage(messageContent, nextSequence);
        session.addMessage(userMessage);
        chatMessageRepository.save(userMessage);

        // AI 응답 생성
        String aiResponse = generateAiResponse(session, messageContent);
        ChatMessage aiMessage = ChatMessage.createAiMessage(aiResponse, nextSequence + 1);
        session.addMessage(aiMessage);
        chatMessageRepository.save(aiMessage);

        log.info("메시지 전송 완료: 세션 ID = {}, 사용자 메시지 = {}", sessionId, messageContent);
        return aiMessage;
    }

    /**
     * AI 응답을 생성하는 메인 메소드
     * 예약 시도 -> 일반 채팅 응답 순서로 처리
     */
    private String generateAiResponse(ChatSession session, String userMessage) {
        List<ChatMessage> conversationHistory = getConversationHistory(session.getId());

        // 1단계: 예약 시도 확인
        String appointmentResult = tryAppointmentCreation(session, userMessage, conversationHistory);
        if (appointmentResult != null) {
            return appointmentResult;
        }

        // 2단계: 일반 채팅 응답 처리
        return handleGeneralChatResponse(session, userMessage, conversationHistory);
    }
    
    /**
     * 예약 생성 시도를 처리합니다
     */
    private String tryAppointmentCreation(ChatSession session, String userMessage, List<ChatMessage> conversationHistory) {
        return appointmentBookingService.tryCreateAppointment(session, userMessage, conversationHistory);
    }
    
    /**
     * 일반적인 채팅 응답을 처리합니다 (GPT 호출 + 진료과 분석 + 응답 강화)
     */
    private String handleGeneralChatResponse(ChatSession session, String userMessage, List<ChatMessage> conversationHistory) {
        // GPT 응답 생성
        String gptResponse = generateGptResponse(conversationHistory, userMessage);
        
        // 진료과 분석 수행 및 저장
        AnalysisResult analysis = analyzeAndSaveSymptoms(session, gptResponse, userMessage);
        
        // 응답에 예약 정보 추가
        return enhanceResponseWithAppointmentInfo(gptResponse, analysis);
    }
    
    /**
     * OpenAI GPT를 사용하여 기본 응답을 생성합니다
     */
    private String generateGptResponse(List<ChatMessage> conversationHistory, String userMessage) {
        return openAIService.generateResponse(conversationHistory, userMessage);
    }
    
    /**
     * 사용자 메시지와 GPT 응답을 분석하여 증상 분석을 수행하고 저장합니다
     */
    private AnalysisResult analyzeAndSaveSymptoms(ChatSession session, String gptResponse, String userMessage) {
        // 진료과 정보 추출
        AnalysisResult analysis = extractDepartmentFromGptResponse(gptResponse, userMessage);
        
        // 분석 결과 저장
        saveOrUpdateSymptomAnalysis(session, analysis);
        
        return analysis;
    }
    
    private List<ChatMessage> getConversationHistory(Long sessionId) {
        return chatMessageRepository.findByChatSessionIdOrderBySequenceNumber(sessionId);
    }

    @Transactional
    private void saveOrUpdateSymptomAnalysis(ChatSession session, AnalysisResult analysis) {
        Optional<SymptomAnalysis> existing = symptomAnalysisRepository.findByChatSessionId(session.getId());
        
        if (existing.isPresent()) {
            // 기존 분석 업데이트
            existing.get().updateAnalysis(
                String.join(", ", analysis.getExtractedSymptoms()),
                analysis.getRecommendedDepartment(),
                analysis.getConfidenceScore(),
                analysis.getSummary()
            );
        } else {
            // 새로운 분석 생성
            SymptomAnalysis symptomAnalysis = SymptomAnalysis.createAnalysis(
                String.join(", ", analysis.getExtractedSymptoms()),
                analysis.getRecommendedDepartment(),
                analysis.getConfidenceScore(),
                analysis.getSummary(),
                String.join("\n", analysis.getAdditionalQuestions())
            );
            session.setSymptomAnalysis(symptomAnalysis);
            symptomAnalysisRepository.save(symptomAnalysis);
        }
    }



    private int getNextSequenceNumber(Long sessionId) {
        ChatMessage lastMessage = chatMessageRepository.findFirstByChatSessionIdOrderBySequenceNumberDesc(sessionId);
        return lastMessage != null ? lastMessage.getSequenceNumber() + 1 : 1;
    }

    public List<ChatSession> getUserChatSessions(Long memberId) {
        return chatSessionRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
    }

    public ChatSession getChatSession(Long sessionId, Long memberId) {
        return chatSessionRepository.findByIdAndMemberIdWithMessages(sessionId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_SESSION_ACCESS_DENIED));
    }

    @Transactional
    public void completeChatSession(Long sessionId, Long memberId) {
        ChatSession session = chatSessionRepository.findByIdAndMemberIdWithMessages(sessionId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_SESSION_ACCESS_DENIED));
        
        session.completeSession();
        log.info("채팅 세션 완료: 세션 ID = {}", sessionId);
    }

    private String enhanceResponseWithAppointmentInfo(String gptResponse, AnalysisResult analysis) {
        StringBuilder enhancedResponse = new StringBuilder(gptResponse);
        
        // 진료과 추천이 확실한 경우 예약 정보 추가
        if (analysis.getConfidenceScore() > chatProperties.getConfidenceThreshold()) {
            String department = analysis.getRecommendedDepartment();
            
            enhancedResponse.append("\n\n🏥 ").append(department).append(" 예약이 가능합니다.\n");
            enhancedResponse.append("📅 예약을 원하시면 희망 날짜와 시간을 알려주세요!");
        }
        
        return enhancedResponse.toString();
    }


    
    /**
     * GPT 응답에서 진료과 정보를 추출합니다.
     */
    private AnalysisResult extractDepartmentFromGptResponse(String gptResponse, String userMessage) {
        // 1순위: 사용자가 직접 진료과를 명시했는지 확인
        String explicitDepartment = findExplicitDepartment(userMessage);
        if (explicitDepartment != null) {
            log.info("직접 언급된 진료과 사용: {}", explicitDepartment);
            return new AnalysisResult(
                List.of("직접 요청된 진료과"),
                explicitDepartment,
                0.95,
                "직접 " + explicitDepartment + " 진료를 요청하셨습니다.",
                List.of("예약을 도와드릴까요?")
            );
        }
        
        // 2순위: GPT 응답에서 진료과 찾기
        List<String> availableDepartments = chatProperties.getAvailableDepartments();
        
        String gptLower = gptResponse.toLowerCase();
        String foundDepartment = "내과"; // 기본값
        double confidence = 0.8;
        
        for (String dept : availableDepartments) {
            if (gptLower.contains(dept.toLowerCase())) {
                foundDepartment = dept;
                confidence = 0.9;
                break;
            }
        }
        
        List<String> symptoms = List.of("AI 분석을 통한 증상");
        List<String> questions = List.of("추가 증상이나 궁금한 점이 있으신가요?");
        
        return new AnalysisResult(
            symptoms,
            foundDepartment, 
            confidence,
            "AI가 종합적으로 분석하여 " + foundDepartment + " 진료를 추천드립니다.",
            questions
        );
    }
    
    /**
     * 사용자 메시지에서 직접적인 진료과 언급을 찾습니다.
     */
    private String findExplicitDepartment(String message) {
        String lowerMessage = message.toLowerCase().replaceAll("\\s+", "");
        
        List<String> departments = chatProperties.getAvailableDepartments();
        
        for (String department : departments) {
            String lowerDept = department.toLowerCase();
            
            // "내과 예약", "내과로", "내과 가고싶어", "내과야" 등의 패턴
            if (lowerMessage.contains(lowerDept + "예약") ||
                lowerMessage.contains(lowerDept + "로") ||
                lowerMessage.contains(lowerDept + "가고") ||
                lowerMessage.contains(lowerDept + "야") ||
                lowerMessage.contains(lowerDept + "이야") ||
                lowerMessage.contains(lowerDept + "입니다") ||
                lowerMessage.contains(lowerDept + "에서") ||
                lowerMessage.contains(lowerDept + "진료") ||
                // "내과 아니라 내과" 같은 수정 표현
                (lowerMessage.contains("아니라") && lowerMessage.contains(lowerDept)) ||
                // 단순히 진료과만 언급
                (lowerMessage.equals(lowerDept) || lowerMessage.contains(lowerDept + "요"))) {
                
                log.info("직접 진료과 언급 감지: {} -> {}", message, department);
                return department;
            }
        }
        
        return null;
    }

    /**
     * 간소화된 분석 결과 클래스 (GPT 전용)
     */
    private static class AnalysisResult {
        private final List<String> extractedSymptoms;
        private final String recommendedDepartment;
        private final double confidenceScore;
        private final String summary;
        private final List<String> additionalQuestions;

        public AnalysisResult(List<String> extractedSymptoms, String recommendedDepartment, 
                            double confidenceScore, String summary, List<String> additionalQuestions) {
            this.extractedSymptoms = extractedSymptoms;
            this.recommendedDepartment = recommendedDepartment;
            this.confidenceScore = confidenceScore;
            this.summary = summary;
            this.additionalQuestions = additionalQuestions;
        }

        public List<String> getExtractedSymptoms() { return extractedSymptoms; }
        public String getRecommendedDepartment() { return recommendedDepartment; }
        public double getConfidenceScore() { return confidenceScore; }
        public String getSummary() { return summary; }
        public List<String> getAdditionalQuestions() { return additionalQuestions; }
    }

}