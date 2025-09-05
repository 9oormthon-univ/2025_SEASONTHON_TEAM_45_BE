package org.carefreepass.com.carefreepassserver.domain.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.carefreepass.com.carefreepassserver.domain.appointment.service.AppointmentService;
import org.carefreepass.com.carefreepassserver.domain.chat.dto.AnalysisResult;
import org.carefreepass.com.carefreepassserver.domain.chat.dto.SymptomAnalysisResult;
import org.carefreepass.com.carefreepassserver.domain.chat.entity.*;
import org.carefreepass.com.carefreepassserver.domain.chat.repository.*;
import org.carefreepass.com.carefreepassserver.domain.auth.entity.Member;
import org.carefreepass.com.carefreepassserver.domain.auth.repository.MemberRepository;
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
    
    // 분석 관련 상수
    private static final String DIRECT_DEPARTMENT_REQUEST = "직접 요청된 진료과";
    private static final String STEP_BY_STEP_SYMPTOM_ANALYSIS = "단계별 증상 분석";
    private static final String SMART_SYMPTOM_ANALYSIS = "스마트 증상 분석";
    private static final String GPT_RESPONSE_ANALYSIS = "GPT 응답 분석";
    private static final String GENERAL_SYMPTOM_ANALYSIS = "증상 분석";
    
    // 예약 관련 메시지 상수
    private static final String RESERVATION_HELP_MESSAGE = "예약을 도와드릴까요?";
    private static final String RESERVATION_PROCEED_MESSAGE = "예약을 진행하시겠습니까?";
    private static final String WOULD_YOU_LIKE_RESERVATION = "예약하시겠습니까?";
    
    // 응답 강화 메시지 상수
    private static final String HOSPITAL_EMOJI = "🏥 ";
    private static final String CALENDAR_EMOJI = "📅 ";
    private static final String RESERVATION_AVAILABLE_MESSAGE = " 예약이 가능합니다.\n";
    private static final String RESERVATION_REQUEST_MESSAGE = " 예약을 원하시면 희망 날짜와 시간을 알려주세요!";
    
    // 기본 메시지 상수
    private static final String DETAILED_ANALYSIS_MESSAGE = "증상을 더 정확히 분석하기 위해 몇 가지 질문을 드릴게요.";
    private static final String AI_ANALYSIS_RECOMMENDATION_PREFIX = "AI 분석 결과 ";
    private static final String AI_ANALYSIS_RECOMMENDATION_SUFFIX = " 진료를 추천드립니다.";
    private static final String DIRECT_DEPARTMENT_REQUEST_PREFIX = "직접 ";
    private static final String DIRECT_DEPARTMENT_REQUEST_SUFFIX = " 진료를 요청하셨습니다.";
    
    // 신뢰도 점수 상수
    private static final double HIGH_CONFIDENCE_SCORE = 0.95;
    private static final double MEDIUM_CONFIDENCE_SCORE = 0.7;
    private static final double LOW_CONFIDENCE_SCORE = 0.0;
    
    private final ChatProperties chatProperties;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SymptomAnalysisRepository symptomAnalysisRepository;
    private final MemberRepository memberRepository;
    private final OpenAIService openAIService;
    private final AppointmentBookingService appointmentBookingService;
    private final SmartSymptomAnalyzer smartSymptomAnalyzer;

    /**
     * 새로운 채팅 세션을 시작하고 첫 번째 AI 응답을 생성
     *
     * @param memberId 회원 ID
     * @param initialMessage 초기 메시지
     * @return 생성된 채팅 세션
     */
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

    /**
     * 채팅 세션에 메시지를 전송하고 AI 응답을 생성
     *
     * @param sessionId 채팅 세션 ID
     * @param memberId 회원 ID
     * @param messageContent 메시지 내용
     * @return 생성된 AI 응답 메시지
     */
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

    // AI 응답 생성 메인 메소드 - 예약 시도 후 일반 채팅 응답 순서 처리
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
    
    // 예약 생성 시도 처리
    private String tryAppointmentCreation(ChatSession session, String userMessage, List<ChatMessage> conversationHistory) {
        return appointmentBookingService.tryCreateAppointment(session, userMessage, conversationHistory);
    }
    
    // 일반 채팅 응답 처리 (GPT 호출 + 진료과 분석 + 응답 강화)
    private String handleGeneralChatResponse(ChatSession session, String userMessage, List<ChatMessage> conversationHistory) {
        // GPT 응답 생성
        String gptResponse = generateGptResponse(conversationHistory, userMessage);
        
        // 진료과 분석 수행 및 저장
        AnalysisResult analysis = analyzeAndSaveSymptoms(session, gptResponse, userMessage);
        
        // 응답에 예약 정보 추가
        return enhanceResponseWithAppointmentInfo(gptResponse, analysis);
    }
    
    // OpenAI GPT 기본 응답 생성
    private String generateGptResponse(List<ChatMessage> conversationHistory, String userMessage) {
        return openAIService.generateResponse(conversationHistory, userMessage);
    }
    
    // 사용자 메시지와 GPT 응답 분석하여 증상 분석 수행 및 저장
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
            SymptomAnalysis symptomAnalysis = SymptomAnalysis.builder()
                .extractedSymptoms(String.join(", ", analysis.getExtractedSymptoms()))
                .recommendedDepartment(analysis.getRecommendedDepartment())
                .confidenceScore(analysis.getConfidenceScore())
                .analysisSummary(analysis.getSummary())
                .additionalQuestions(String.join("\n", analysis.getAdditionalQuestions()))
                .build();
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
            
            enhancedResponse.append("\n\n").append(HOSPITAL_EMOJI).append(department).append(RESERVATION_AVAILABLE_MESSAGE);
            enhancedResponse.append(CALENDAR_EMOJI).append(RESERVATION_REQUEST_MESSAGE);
        }
        
        return enhancedResponse.toString();
    }
    
    // 스마트 증상 분석 사용한 진료과 추천
    private AnalysisResult extractDepartmentFromGptResponse(String gptResponse, String userMessage) {
        // 1순위: 사용자가 직접 진료과를 명시했는지 확인
        String explicitDepartment = findExplicitDepartment(userMessage);
        if (explicitDepartment != null) {
            log.info("직접 언급된 진료과 사용: {}", explicitDepartment);
            return AnalysisResult.builder()
                .extractedSymptoms(List.of(DIRECT_DEPARTMENT_REQUEST))
                .recommendedDepartment(explicitDepartment)
                .confidenceScore(HIGH_CONFIDENCE_SCORE)
                .summary(DIRECT_DEPARTMENT_REQUEST_PREFIX + explicitDepartment + DIRECT_DEPARTMENT_REQUEST_SUFFIX)
                .additionalQuestions(List.of(RESERVATION_HELP_MESSAGE))
                .build();
        }
        
        // 2순위: 스마트 증상 분석 사용
        SymptomAnalysisResult smartResult = smartSymptomAnalyzer.analyzeSymptom(userMessage);
        
        if (smartResult.isNeedsMoreInfo()) {
            // 추가 질문이 필요한 경우
            return AnalysisResult.builder()
                .extractedSymptoms(List.of(STEP_BY_STEP_SYMPTOM_ANALYSIS))
                .recommendedDepartment(null)
                .confidenceScore(LOW_CONFIDENCE_SCORE)
                .summary(smartResult.getMessage())
                .additionalQuestions(List.of(smartResult.getFollowUpQuestion()))
                .build();
        } else if (smartResult.getDepartment() != null) {
            // 진료과가 결정된 경우
            return AnalysisResult.builder()
                .extractedSymptoms(List.of(SMART_SYMPTOM_ANALYSIS))
                .recommendedDepartment(smartResult.getDepartment())
                .confidenceScore(smartResult.getConfidence())
                .summary(smartResult.getMessage())
                .additionalQuestions(List.of(RESERVATION_PROCEED_MESSAGE))
                .build();
        }
        
        // 3순위: 기존 GPT 응답 분석 (백업)
        List<String> availableDepartments = chatProperties.getAvailableDepartments();
        String gptLower = gptResponse.toLowerCase();
        
        for (String dept : availableDepartments) {
            if (gptLower.contains(dept.toLowerCase())) {
                return AnalysisResult.builder()
                    .extractedSymptoms(List.of(GPT_RESPONSE_ANALYSIS))
                    .recommendedDepartment(dept)
                    .confidenceScore(MEDIUM_CONFIDENCE_SCORE)
                    .summary(AI_ANALYSIS_RECOMMENDATION_PREFIX + dept + AI_ANALYSIS_RECOMMENDATION_SUFFIX)
                    .additionalQuestions(List.of(WOULD_YOU_LIKE_RESERVATION))
                    .build();
            }
        }
        
        // 최종 백업: 진료과별 선택 옵션 제공
        return AnalysisResult.builder()
            .extractedSymptoms(List.of(GENERAL_SYMPTOM_ANALYSIS))
            .recommendedDepartment(null)
            .confidenceScore(LOW_CONFIDENCE_SCORE)
            .summary(DETAILED_ANALYSIS_MESSAGE)
            .additionalQuestions(List.of(smartSymptomAnalyzer.createBodyPartQuestion().getFollowUpQuestion()))
            .build();
    }
    
    // 사용자 메시지에서 직접적인 진료과 언급 찾기
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

}