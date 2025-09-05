# 🤖 AI 챗봇 연결 가이드

## 📋 개요

CareFreePass 시스템에 AI 챗봇을 연결하여 환자의 증상 상담 및 자동 예약 기능을 구현하는 방법을 설명합니다.

## 🎯 AI 챗봇 기능

### 1. 증상 상담
- 환자가 증상을 입력하면 AI가 분석
- 적절한 진료과 추천
- 증상의 심각도 평가

### 2. 자동 예약 도움
- AI가 추천한 진료과로 예약 안내
- 예약 가능한 날짜/시간 제안
- 예약 확정 프로세스 가이드

## 🔧 연결 방법

### 1단계: AI 서비스 선택

#### Option A: OpenAI GPT API
```bash
# 필요한 것
- OpenAI API Key
- GPT-4 또는 GPT-3.5-turbo 모델
- 월 사용량에 따른 비용 발생
```

#### Option B: Google Gemini API  
```bash
# 필요한 것  
- Google Cloud Project
- Gemini API Key
- 무료 할당량 제공
```

#### Option C: 로컬 LLM (무료)
```bash
# 필요한 것
- Ollama 설치
- 로컬에서 실행되는 LLM 모델
- 서버 리소스 필요
```

### 2단계: 환경 설정

#### .env 파일에 API Key 추가
```env
# OpenAI 사용 시
OPENAI_API_KEY=your_openai_api_key_here

# Google Gemini 사용 시  
GOOGLE_GEMINI_API_KEY=your_gemini_api_key_here

# 로컬 LLM 사용 시
OLLAMA_BASE_URL=http://localhost:11434
```

### 3단계: 의존성 추가

#### build.gradle에 HTTP 클라이언트 추가
```gradle
dependencies {
    // HTTP 통신용 (AI API 호출)
    implementation 'org.springframework.boot:spring-boot-starter-webflux'
    
    // JSON 처리용
    implementation 'com.fasterxml.jackson.core:jackson-databind'
}
```

### 4단계: AI 서비스 구현

#### AI 연결 서비스 생성 필요
```java
// 구현해야 할 클래스들
@Service
public class AiChatService {
    // AI API 호출 로직
    // 증상 분석 및 진료과 추천
    // 대화 내용 저장
}

@RestController
public class ChatController {  
    // 채팅 시작/메시지 전송 API
    // 프론트엔드와 통신
}
```

## 🏗️ 시스템 구조

### 데이터 흐름
```
환자 앱 → 채팅 API → AI 서비스 → 의료 DB 분석 → 진료과 추천 → 예약 시스템 연동
```

### 필요한 컴포넌트
1. **ChatSession**: 채팅 세션 관리
2. **ChatMessage**: 메시지 저장  
3. **SymptomAnalysis**: 증상 분석 결과
4. **DepartmentRecommendation**: 진료과 추천

## 📱 프론트엔드 연동

### 채팅 UI 구현 필요
```javascript
// 채팅 인터페이스
- 메시지 입력창
- 대화 내역 표시
- AI 응답 실시간 표시
- 예약 버튼 연동
```

### API 엔드포인트
```bash
POST /api/v1/chat/start        # 채팅 시작
POST /api/v1/chat/message      # 메시지 전송  
GET  /api/v1/chat/history      # 대화 내역
POST /api/v1/chat/book         # AI 추천 후 예약
```

## ⚙️ 설정 방법

### 1. AI 모델 프롬프트 설정
```text
시스템 프롬프트 예시:
"당신은 병원의 AI 상담사입니다. 
환자의 증상을 듣고 적절한 진료과를 추천해주세요.
가능한 진료과: 내과, 외과, 정형외과, 소아과, 산부인과..."
```

### 2. 응답 포맷 정의
```json
{
  "message": "AI 응답 메시지",
  "recommendedDepartment": "추천 진료과",  
  "severity": "LOW|MEDIUM|HIGH",
  "needsImmediateAttention": false
}
```

### 3. 에러 처리 설정
```yaml
ai:
  timeout: 30s
  retry: 3
  fallback-message: "죄송합니다. 잠시 후 다시 시도해주세요."
```

## 🔒 보안 고려사항

### 1. 개인정보 보호
- 환자 증상 데이터 암호화 저장
- AI 서비스 전송 시 개인정보 제거
- 대화 내역 보존 기간 설정

### 2. 의료 책임 한계
- AI 진단은 참고용임을 명시
- 응급상황 시 119 안내
- 정확한 진단은 의사 상담 필요 안내

## 📊 모니터링

### 추적해야 할 지표
- AI API 응답 시간
- 추천 정확도
- 사용자 만족도  
- 예약 전환율
- 시스템 에러율

## 🚀 구현 단계별 가이드

### Phase 1: 기본 채팅
1. 채팅 UI 구현
2. 메시지 저장 시스템
3. 간단한 AI 응답

### Phase 2: 증상 분석
1. AI 모델 연동
2. 증상 분석 로직  
3. 진료과 추천 시스템

### Phase 3: 예약 연동
1. AI 추천 → 예약 플로우
2. 예약 가능 시간 조회
3. 원클릭 예약 기능

### Phase 4: 고도화
1. 개인화된 추천
2. 과거 진료 이력 반영
3. 다국어 지원

## 💡 개발 팁

### 1. 테스트 방법
```bash
# AI 응답 테스트용 더미 데이터
증상: "머리가 아파요"  
기대 응답: 신경과 또는 내과 추천
```

### 2. 성능 최적화
- AI API 응답 캐싱
- 자주 묻는 질문 미리 준비
- 배치 요청으로 비용 절약

### 3. 사용자 경험
- 타이핑 인디케이터
- 빠른 답변 버튼
- 이전 대화 이어하기

## 🆘 문제해결

### 자주 발생하는 이슈
1. **AI API 응답 지연**: 타임아웃 설정 조정
2. **비용 초과**: 캐싱 및 사용량 제한 설정  
3. **부정확한 추천**: 프롬프트 개선 필요
4. **개인정보 누출**: 데이터 마스킹 강화

## 📞 추가 지원

AI 챗봇 구현 관련 기술 지원이나 상세한 코드 예시가 필요하면 개발팀에 문의하세요.

---

**최종 업데이트**: 2025년 9월 4일  
**작성자**: CareFreePass 개발팀  
**난이도**: 중급  
**예상 개발 기간**: 2-4주