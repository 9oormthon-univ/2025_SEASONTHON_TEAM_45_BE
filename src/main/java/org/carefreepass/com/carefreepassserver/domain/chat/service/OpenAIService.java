package org.carefreepass.com.carefreepassserver.domain.chat.service;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.carefreepass.com.carefreepassserver.golbal.config.OpenAIProperties;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * OpenAI GPT API 클라이언트 서비스
 * ChatGPT API를 통한 AI 채팅 응답 생성
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAIService {

    private final OpenAiService openAiService;
    private final OpenAIProperties openAIProperties;

    /**
     * 대화 히스토리와 사용자 메시지를 기반으로 AI 응답 생성
     *
     * @param conversationHistory 기존 대화 히스토리
     * @param userMessage 현재 사용자 메시지
     * @return AI 생성 응답
     */
    public String generateResponse(List<org.carefreepass.com.carefreepassserver.domain.chat.entity.ChatMessage> conversationHistory, String userMessage) {
        try {
            log.info("OpenAI API 호출 시작: 사용자 메시지 = {}", userMessage);
            
            List<ChatMessage> messages = new ArrayList<>();
            
            // 시스템 프롬프트 추가
            messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), openAIProperties.getSystemPromptGeneral()));
            
            // 대화 히스토리 추가
            for (org.carefreepass.com.carefreepassserver.domain.chat.entity.ChatMessage msg : conversationHistory) {
                String role = msg.getSenderType().name().equals("USER") ? 
                    ChatMessageRole.USER.value() : ChatMessageRole.ASSISTANT.value();
                messages.add(new ChatMessage(role, msg.getContent()));
            }
            
            // 현재 사용자 메시지 추가
            messages.add(new ChatMessage(ChatMessageRole.USER.value(), userMessage));
            
            log.info("OpenAI API 요청 준비 완료: 메시지 수 = {}", messages.size());
            
            // ChatGPT API 호출
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(openAIProperties.getModel())
                    .messages(messages)
                    .maxTokens(openAIProperties.getMaxTokensGeneral())
                    .temperature(openAIProperties.getTemperatureGeneral())
                    .build();
                    
            ChatCompletionResult result = openAiService.createChatCompletion(request);
            
            String response = result.getChoices().get(0).getMessage().getContent();
            log.info("OpenAI 응답 생성 완료: 토큰 사용량 = {}", result.getUsage().getTotalTokens());
            
            return response;
            
        } catch (Exception e) {
            log.error("OpenAI API 호출 실패: {}", e.getMessage(), e);
            String fallbackResponse = generateFallbackResponse(userMessage);
            log.info("Fallback 응답 반환: {}", fallbackResponse);
            return fallbackResponse;
        }
    }

    /**
     * API 호출 실패 시 사용할 기본 응답 생성
     *
     * @param userMessage 사용자 메시지
     * @return 기본 응답
     */
    private String generateFallbackResponse(String userMessage) {
        String lowerMessage = userMessage.toLowerCase();
        
        // 증상 관련 키워드가 포함된 경우
        if (lowerMessage.contains("아프") || lowerMessage.contains("통증") || 
            lowerMessage.contains("아픈") || lowerMessage.contains("불편")) {
            return "증상을 알려주셔서 감사합니다. 정확한 진단을 위해 더 자세한 정보가 필요해요. " +
                   "어떤 부위가 아프신지, 언제부터 아프셨는지 더 자세히 알려주시겠어요?";
        }
        
        // 예약 관련 키워드가 포함된 경우
        if (lowerMessage.contains("예약") || lowerMessage.contains("병원")) {
            return "예약 관련 문의해주셔서 감사합니다. 어떤 진료과 예약을 원하시나요? " +
                   "먼저 증상을 알려주시면 적절한 진료과를 안내해드릴게요.";
        }
        
        return "안녕하세요! 어떤 증상으로 문의주셨나요? 자세히 말씀해주시면 적절한 진료과를 안내해드리겠습니다.";
    }
}