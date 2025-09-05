package org.carefreepass.com.carefreepassserver.domain.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.carefreepass.com.carefreepassserver.domain.chat.dto.SymptomAnalysisResult;
import org.carefreepass.com.carefreepassserver.golbal.config.ChatProperties;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import static org.carefreepass.com.carefreepassserver.domain.chat.util.SymptomKeywords.*;

// 스마트 증상 분석 서비스
@Service
@RequiredArgsConstructor
@Slf4j
public class SmartSymptomAnalyzer {
    
    private final ChatProperties chatProperties;
    
    
    // 메인 증상 분석 메서드
    public SymptomAnalysisResult analyzeSymptom(String userMessage) {
        String normalized = normalizeMessage(userMessage);
        
        // 1단계: 명확한 키워드 분석
        SymptomAnalysisResult directMatch = analyzeDirectKeywords(normalized);
        if (directMatch != null) {
            log.info("직접 키워드 매칭: {} -> {}", userMessage, directMatch.getDepartment());
            return directMatch;
        }
        
        // 2단계: 부위별 질문이 필요한 경우
        if (isVagueSymptom(normalized)) {
            return createBodyPartQuestion();
        }
        
        // 3단계: 숫자 응답 처리 (부위 선택)
        SymptomAnalysisResult numberResponse = handleBodyPartSelection(normalized);
        if (numberResponse != null) {
            return numberResponse;
        }
        
        // 4단계: 여전히 애매한 경우 옵션 제시
        return createMultipleOptions(normalized);
    }
    
    // 메시지 정규화
    private String normalizeMessage(String message) {
        return message.toLowerCase()
                .replaceAll("[^a-zA-Z0-9가-힣\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
    
    // 1단계: 직접적인 키워드 분석
    private SymptomAnalysisResult analyzeDirectKeywords(String message) {
        // 치과 관련
        if (containsAny(message, DENTAL_KEYWORDS)) {
            return new SymptomAnalysisResult("치과", 0.95, 
                "🦷 치아 관련 증상으로 보아 **치과** 진료를 추천드립니다!");
        }
        
        // 정형외과 관련
        if (containsAny(message, ORTHOPEDIC_KEYWORDS)) {
            return new SymptomAnalysisResult("정형외과", 0.9, 
                "🦴 근골격계 증상으로 보아 **정형외과** 진료를 추천드립니다!");
        }
        
        // 이비인후과 관련
        if (containsAny(message, ENT_KEYWORDS)) {
            return new SymptomAnalysisResult("이비인후과", 0.9, 
                "👂 목, 코, 귀 관련 증상으로 보아 **이비인후과** 진료를 추천드립니다!");
        }
        
        // 피부과 관련
        if (containsAny(message, DERMATOLOGY_KEYWORDS)) {
            return new SymptomAnalysisResult("피부과", 0.9, 
                "🩺 피부 관련 증상으로 보아 **피부과** 진료를 추천드립니다!");
        }
        
        // 외과 관련
        if (containsAny(message, SURGERY_KEYWORDS)) {
            return new SymptomAnalysisResult("외과", 0.9, 
                "🏥 외상이나 수술 관련 증상으로 보아 **외과** 진료를 추천드립니다!");
        }
        
        // 내과 관련 (명확한 경우만)
        if (containsAny(message, INTERNAL_MEDICINE_KEYWORDS)) {
            return new SymptomAnalysisResult("내과", 0.85, 
                "🩺 내과적 증상으로 보아 **내과** 진료를 추천드립니다!");
        }
        
        return null; // 직접 매칭되는 것이 없음
    }
    
    // 애매한 증상인지 확인
    private boolean isVagueSymptom(String message) {
        return containsAny(message, VAGUE_SYMPTOMS) && !containsSpecificBodyPart(message);
    }
    
    // 구체적인 신체 부위가 언급되었는지 확인
    private boolean containsSpecificBodyPart(String message) {
        return containsAny(message, SPECIFIC_BODY_PARTS);
    }
    
    // 2단계: 부위별 질문 생성
    public SymptomAnalysisResult createBodyPartQuestion() {
        String question = """
            어떤 부위가 불편하신가요? 번호를 선택하거나 자세히 설명해주세요:
            
            1️⃣ **머리/두통** (두통, 어지러움 등)
            2️⃣ **목/목구멍/귀/코** (목 아픔, 콧물, 귀 아픔 등)  
            3️⃣ **가슴/심장/호흡** (가슴 아픔, 숨쉬기 어려움 등)
            4️⃣ **배/소화기** (배 아픔, 소화불량, 설사 등)
            5️⃣ **팔다리/관절/뼈** (관절 아픔, 근육통, 골절 등)
            6️⃣ **피부** (발진, 가려움, 알레르기 등)
            7️⃣ **치아/구강** (치아 아픔, 잇몸 문제 등)
            8️⃣ **외상/상처** (베임, 화상, 수술 후 등)
            
            번호로 답하시거나 증상을 더 자세히 말씀해주세요! 🏥
            """;
            
        return new SymptomAnalysisResult("부위별 증상을 확인해보겠습니다.", question);
    }
    
    // 3단계: 숫자 응답 처리
    private SymptomAnalysisResult handleBodyPartSelection(String message) {
        // 1번: 머리/두통
        if (message.contains("1") || containsAny(message, HEAD_PAIN_KEYWORDS)) {
            return new SymptomAnalysisResult("내과", 0.8, 
                "🧠 두통이나 어지러움 증상은 **내과** 진료를 추천드립니다. 언제부터 증상이 있으셨나요?");
        }
        
        // 2번: 목/목구멍/귀/코  
        if (message.contains("2") || containsAny(message, THROAT_EAR_NOSE_KEYWORDS)) {
            return new SymptomAnalysisResult("이비인후과", 0.9,
                "👂 목구멍, 귀, 코 관련 증상은 **이비인후과** 진료를 추천드립니다!");
        }
        
        // 3번: 가슴/심장/호흡
        if (message.contains("3") || containsAny(message, CHEST_HEART_KEYWORDS)) {
            return new SymptomAnalysisResult("내과", 0.9,
                "❤️ 가슴이나 호흡 관련 증상은 **내과** 진료를 추천드립니다. 응급한 상황이면 119에 연락하세요!");
        }
        
        // 4번: 배/소화기
        if (message.contains("4") || containsAny(message, STOMACH_KEYWORDS)) {
            return new SymptomAnalysisResult("내과", 0.9,
                "🤢 소화기 관련 증상은 **내과** 진료를 추천드립니다!");
        }
        
        // 5번: 팔다리/관절/뼈
        if (message.contains("5") || containsAny(message, LIMBS_JOINT_KEYWORDS)) {
            return new SymptomAnalysisResult("정형외과", 0.9,
                "🦴 근골격계 증상은 **정형외과** 진료를 추천드립니다!");
        }
        
        // 6번: 피부
        if (message.contains("6") || containsAny(message, SKIN_KEYWORDS)) {
            return new SymptomAnalysisResult("피부과", 0.9,
                "🩺 피부 관련 증상은 **피부과** 진료를 추천드립니다!");
        }
        
        // 7번: 치아/구강
        if (message.contains("7") || containsAny(message, ORAL_KEYWORDS)) {
            return new SymptomAnalysisResult("치과", 0.95,
                "🦷 치아나 구강 관련 증상은 **치과** 진료를 추천드립니다!");
        }
        
        // 8번: 외상/상처
        if (message.contains("8") || containsAny(message, TRAUMA_KEYWORDS)) {
            return new SymptomAnalysisResult("외과", 0.9,
                "🏥 외상이나 상처는 **외과** 진료를 추천드립니다!");
        }
        
        return null;
    }
    
    // 4단계: 여러 옵션 제시
    private SymptomAnalysisResult createMultipleOptions(String message) {
        return new SymptomAnalysisResult("내과", 0.6,
            """
            증상을 종합해보면 다음 진료과들을 고려해볼 수 있어요:
            
            🏥 **추천 순서:**
            1. **내과** - 일반적인 내과 질환 (열, 몸살, 소화기 등)
            2. **정형외과** - 근골격계 문제가 의심되는 경우
            
            더 구체적인 증상을 말씀해주시면 정확한 진료과를 안내해드릴 수 있어요! 
            어떤 증상이 가장 주된 문제인가요? 🤔
            """);
    }
    
    // 문자열에 키워드들 중 하나라도 포함되어 있는지 확인
    private boolean containsAny(String text, List<String> keywords) {
        return keywords.stream().anyMatch(text::contains);
    }
}