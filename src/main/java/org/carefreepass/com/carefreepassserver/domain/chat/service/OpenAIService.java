package org.carefreepass.com.carefreepassserver.domain.chat.service;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAIService {

    private final OpenAiService openAiService;

    private static final String SYSTEM_PROMPT = """
        당신은 병원 예약을 도와주는 전문 의료 상담 AI입니다.
        
        역할:
        1. 환자의 증상을 듣고 분석합니다
        2. 적절한 진료과를 추천합니다
        3. 예약 과정을 안내합니다
        
        규칙:
        - 의학적 진단은 하지 마세요
        - 진료과 추천에 그치세요
        - 친근하고 전문적으로 응답하세요
        - 응급상황시 응급실 방문을 권하세요
        
        진료과 목록:
        - 내과: 감기, 발열, 복통, 소화불량, 고혈압, 당뇨 등
        - 외과: 상처, 수술, 외상 등
        - 정형외과: 관절, 뼈, 근육, 허리, 목 통증 등
        - 피부과: 피부 질환, 알레르기, 여드름 등
        - 이비인후과: 목, 귀, 코 관련 증상
        - 안과: 눈 관련 증상
        - 산부인과: 여성 질환
        - 소아과: 어린이 관련
        - 정신과: 우울, 불안, 스트레스
        - 치과: 치아, 잇몸 관련
        
        항상 한국어로 응답하세요.
        """;

    public String generateResponse(List<org.carefreepass.com.carefreepassserver.domain.chat.entity.ChatMessage> conversationHistory, String userMessage) {
        try {
            List<ChatMessage> messages = new ArrayList<>();
            
            // 시스템 프롬프트 추가
            messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), SYSTEM_PROMPT));
            
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
                    .model("gpt-3.5-turbo")
                    .messages(messages)
                    .maxTokens(500)
                    .temperature(0.7)
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
                new ChatMessage(ChatMessageRole.SYSTEM.value(), SYSTEM_PROMPT),
                new ChatMessage(ChatMessageRole.USER.value(), symptomAnalysisPrompt)
            );
            
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model("gpt-3.5-turbo")
                    .messages(messages)
                    .maxTokens(300)
                    .temperature(0.5)
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