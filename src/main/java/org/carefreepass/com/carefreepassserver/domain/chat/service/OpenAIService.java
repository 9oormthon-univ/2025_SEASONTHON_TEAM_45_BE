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

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAIService {

    private final OpenAiService openAiService;
    private final OpenAIProperties openAIProperties;


    public String generateResponse(List<org.carefreepass.com.carefreepassserver.domain.chat.entity.ChatMessage> conversationHistory, String userMessage) {
        try {
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
            return generateFallbackResponse(userMessage);
        }
    }

    public String analyzeSymptoms(String userMessage) {
        try {
            String symptomAnalysisPrompt = """
                다음 증상을 분석하고 적절한 진료과를 추천해주세요:
                
                증상: %s
                
                응답 형식:
                - 추천 진료과: [진료과명]
                - 이유: [간단한 설명]
                - 추가 질문: [필요한 경우]
                """.formatted(userMessage);
                
            List<ChatMessage> messages = List.of(
                new ChatMessage(ChatMessageRole.SYSTEM.value(), openAIProperties.getSystemPromptAnalysis()),
                new ChatMessage(ChatMessageRole.USER.value(), symptomAnalysisPrompt)
            );
            
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(openAIProperties.getModel())
                    .messages(messages)
                    .maxTokens(openAIProperties.getMaxTokensAnalysis())
                    .temperature(openAIProperties.getTemperatureAnalysis())
                    .build();
                    
            ChatCompletionResult result = openAiService.createChatCompletion(request);
            return result.getChoices().get(0).getMessage().getContent();
            
        } catch (Exception e) {
            log.error("증상 분석 실패: {}", e.getMessage());
            return "죄송합니다. 증상 분석 중 오류가 발생했습니다. 내과 진료를 권합니다.";
        }
    }

    private String generateFallbackResponse(String userMessage) {
        // OpenAI API 실패 시 기본 응답
        if (userMessage.contains("아프") || userMessage.contains("통증")) {
            return "증상을 알려주셔서 감사합니다. 정확한 진단을 위해 내과 진료를 받으시기를 권합니다. " +
                   "언제부터 아프셨는지, 어떤 종류의 통증인지 더 자세히 알려주시겠어요?";
        }
        
        return "안녕하세요! 어떤 증상으로 문의주셨나요? 자세히 말씀해주시면 적절한 진료과를 안내해드리겠습니다.";
    }
}