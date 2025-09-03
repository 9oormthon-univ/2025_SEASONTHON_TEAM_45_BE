# CareFreePass 모바일 환자용 API 명세서 📱

## 📋 개요

CareFreePass 병원 예약 시스템의 **모바일 환자용 API 명세서**입니다.  
환자들이 모바일 앱에서 사용하는 모든 API 엔드포인트를 포함합니다.

**Base URL**: `http://13.124.250.98:8080`  
**Swagger UI**: `http://13.124.250.98:8080/swagger-ui/index.html`  
**API Version**: `v1`

## 🔐 인증 정보

### JWT Bearer Token 방식
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**⚠️ 현재 보안 설정**: 개발/테스트용으로 인증 없이 접근 가능  
**🔒 운영 환경**: JWT 토큰 필수 (향후 적용 예정)

---

## 👤 1. 환자 인증 API

### 1.1 환자 회원가입 📝
```http
POST /api/v1/auth/patient/sign-up
Content-Type: application/json
```

**📝 설명**: 환자가 앱에 회원가입하고 즉시 JWT 토큰을 발급받습니다.

**🔍 Request Body (필수 필드):**
```json
{
  "name": "김환자",              // 환자 이름 (2-20자)
  "gender": "남성",             // 성별: "남성" 또는 "여성"  
  "birthDate": "19900315",      // 생년월일 (YYYYMMDD 형식)
  "phoneNumber": "01012345678", // 휴대폰 번호 (11자리, 하이픈 없이)
  "password": "password123!"    // 비밀번호 (8자 이상, 영문+숫자+특수문자)
}
```

**✅ Response (200 OK):**
```json
{
  "code": "AUTH_2001",
  "message": "환자 회원가입이 완료되었습니다.",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",  // 1시간 유효
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...", // 14일 유효
    "memberId": 1,
    "memberName": "김환자",
    "role": "PATIENT"
  }
}
```

**❌ Error Cases:**
- `400`: 필수 필드 누락 또는 유효성 검사 실패
- `409`: 이미 존재하는 휴대폰 번호
- `500`: 서버 내부 오류

**📱 앱 개발 팁:**
- 회원가입 성공 시 토큰을 안전한 저장소(Keychain/Keystore)에 저장
- 생년월일 입력 시 DatePicker 사용 권장
- 실시간 유효성 검사로 UX 개선

### 1.2 환자 로그인 🔐
```http
POST /api/v1/auth/patient/sign-in
Content-Type: application/json
```

**📝 설명**: 등록된 환자가 로그인하여 JWT 토큰을 발급받습니다.

**🔍 Request Body:**
```json
{
  "phoneNumber": "01012345678", // 등록된 휴대폰 번호
  "password": "password123!"    // 비밀번호
}
```

**✅ Response (200 OK):**
```json
{
  "code": "AUTH_2002", 
  "message": "환자 로그인에 성공했습니다.",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "memberId": 1,
    "memberName": "김환자", 
    "role": "PATIENT"
  }
}
```

**❌ Error Cases:**
- `400`: 잘못된 요청 형식
- `401`: 인증 실패 (잘못된 아이디/비밀번호)
- `404`: 존재하지 않는 사용자

---

## 🤖 2. AI 채팅 상담 API

### 2.1 AI 채팅 세션 시작 💬
```http
POST /api/v1/chat/start
Content-Type: application/json
Authorization: Bearer <patient_token>
```

**📝 설명**: 증상 상담을 위한 AI 채팅 세션을 시작합니다. 첫 메시지를 보내면 AI가 자동으로 응답합니다.

**🔍 Request Body:**
```json
{
  "memberId": 1,                      // 환자 ID (필수)
  "initialMessage": "배가 아프고 열이 나요" // 초기 증상 메시지 (필수)
}
```

**✅ Response (200 OK):**
```json
{
  "code": "CHAT_6001",
  "message": "채팅 세션이 시작되었습니다.",
  "data": {
    "sessionId": 456,
    "memberId": 1,
    "status": "ACTIVE",
    "messages": [
      {
        "id": 789,
        "sessionId": 456,
        "sender": "USER",
        "content": "배가 아프고 열이 나요",
        "timestamp": "2025-09-02T10:30:00",
        "messageType": "TEXT"
      },
      {
        "id": 790,
        "sessionId": 456,
        "sender": "AI", 
        "content": "증상을 자세히 알려주세요. 언제부터 시작되었나요?",
        "timestamp": "2025-09-02T10:30:05",
        "messageType": "TEXT"
      }
    ]
  }
}
```

**📱 앱 개발 팁:**
- 채팅 UI는 메신저 스타일로 구성
- AI 응답 대기 중 로딩 인디케이터 표시
- 메시지 타임스탬프 사용자 친화적으로 포맷팅

### 2.2 채팅 메시지 전송 📤
```http
POST /api/v1/chat/message
Content-Type: application/json
Authorization: Bearer <patient_token>
```

**📝 설명**: 진행 중인 채팅 세션에 새 메시지를 전송합니다.

**🔍 Request Body:**
```json
{
  "sessionId": 456,               // 채팅 세션 ID (필수)
  "memberId": 1,                  // 환자 ID (필수)
  "content": "어제 저녁부터 아팠어요" // 메시지 내용 (필수)
}
```

**✅ Response (200 OK):**
```json
{
  "code": "CHAT_6002",
  "message": "메시지가 전송되었습니다.", 
  "data": {
    "messageId": 791,
    "senderType": "USER",
    "content": "어제 저녁부터 아팠어요",
    "sequenceNumber": 3,
    "createdAt": "2025-09-02T10:32:00",
    "aiResponse": {
      "messageId": 792,
      "senderType": "AI",
      "content": "복통과 발열 증상이 어제부터 시작되었군요. 다른 증상은 없나요?",
      "sequenceNumber": 4,
      "createdAt": "2025-09-02T10:32:03"
    }
  }
}
```

### 2.3 사용자 채팅 세션 목록 조회 📋
```http
GET /api/v1/chat/sessions?memberId=1
Authorization: Bearer <patient_token>
```

**📝 설명**: 환자의 모든 채팅 세션 목록을 조회합니다. (최신순)

**🔍 Query Parameters:**
- `memberId`: 환자 ID (필수)

**✅ Response (200 OK):**
```json
{
  "code": "CHAT_6003",
  "message": "채팅 세션 목록 조회가 완료되었습니다.",
  "data": [
    {
      "sessionId": 456,
      "memberId": 1,
      "status": "COMPLETED",
      "startedAt": "2025-09-02T10:30:00",
      "completedAt": "2025-09-02T10:45:00",
      "symptomAnalysis": {
        "primarySymptom": "복통 및 발열",
        "recommendedDepartment": "내과",
        "urgencyLevel": "MEDIUM",
        "confidenceScore": 85
      },
      "lastMessage": "진료 예약을 도와드릴게요."
    }
  ]
}
```

### 2.4 채팅 세션 상세 조회 🔍
```http
GET /api/v1/chat/sessions/{sessionId}?memberId=1
Authorization: Bearer <patient_token>
```

**📝 설명**: 특정 채팅 세션의 전체 대화 내용을 조회합니다.

**🔗 Path Parameters:**
- `sessionId`: 채팅 세션 ID

**🔍 Query Parameters:**
- `memberId`: 환자 ID (필수)

**✅ Response (200 OK):**
```json
{
  "code": "CHAT_6004",
  "message": "채팅 세션 상세 조회가 완료되었습니다.",
  "data": {
    "sessionId": 456,
    "memberId": 1,
    "status": "ACTIVE",
    "messages": [
      // 전체 메시지 목록...
    ]
  }
}
```

### 2.5 채팅 세션 완료 ✅
```http
PUT /api/v1/chat/sessions/{sessionId}/complete?memberId=1
Authorization: Bearer <patient_token>
```

**📝 설명**: 진행 중인 채팅 세션을 완료 처리합니다.

**✅ Response (200 OK):**
```json
{
  "code": "CHAT_6005",
  "message": "채팅 세션이 완료되었습니다.",
  "data": "SUCCESS"
}
```

---

## 📅 3. 예약 시간 조회 API

### 3.1 예약 가능한 시간대 조회 🕐
```http
GET /api/v1/patient/time-slots?hospitalId=1&departmentName=내과&date=2025-09-03
```

**📝 설명**: 특정 병원의 진료과에서 지정된 날짜에 예약 가능한 모든 시간대를 조회합니다.

**🔍 Query Parameters (필수):**
- `hospitalId`: 병원 ID (Long)
- `departmentName`: 진료과명 (String) - 예: "내과", "외과", "정형외과"  
- `date`: 조회할 날짜 (LocalDate) - YYYY-MM-DD 형식

**📎 사용 예시:**
```http
GET /api/v1/patient/time-slots?hospitalId=1&departmentName=내과&date=2025-09-03
GET /api/v1/patient/time-slots?hospitalId=2&departmentName=정형외과&date=2025-09-05
```

**✅ Response (200 OK):**
```json
{
  "code": "TIME_SLOT_1001",
  "message": "예약 가능 시간 조회가 완료되었습니다.",
  "data": {
    "date": "2025-09-03",
    "departmentName": "내과",
    "availableCount": 8,         // 예약 가능한 시간대 수
    "totalSlots": 12,           // 전체 시간대 수
    "timeSlots": [
      {
        "time": "09:00",
        "available": true,       // 예약 가능
        "reason": null,
        "bookedBy": null
      },
      {
        "time": "09:30",
        "available": false,      // 이미 예약됨
        "reason": "BOOKED",
        "bookedBy": "김**"
      },
      {
        "time": "14:00",
        "available": false,      // 관리자가 차단
        "reason": "BLOCKED",
        "bookedBy": null
      }
    ]
  }
}
```

**📱 앱 개발 팁:**
- 캘린더 뷰에서 가능/불가능 시간 다른 색상으로 표시
- 불가능한 이유(이미 예약됨/차단됨)에 따라 다른 안내 메시지
- 새로고침 버튼으로 실시간 상태 업데이트

### 3.2 특정 시간 예약 가능 여부 확인 ✓
```http
GET /api/v1/patient/time-slots/check?hospitalId=1&departmentName=내과&date=2025-09-03&time=14:30
```

**📝 설명**: 특정 시간대에 예약이 가능한지 단건 확인합니다.

**🔍 Query Parameters (필수):**
- `hospitalId`: 병원 ID (Long)
- `departmentName`: 진료과명 (String)
- `date`: 조회할 날짜 (LocalDate) - YYYY-MM-DD
- `time`: 확인할 시간 (String) - HH:MM 형식

**✅ Response (200 OK) - 예약 가능:**
```json
{
  "code": "TIME_SLOT_1002",
  "message": "시간대 예약 가능 여부 확인이 완료되었습니다.",
  "data": true  // true: 예약 가능, false: 예약 불가
}
```

---

## 📝 4. 예약 관리 API

### 4.1 예약 생성 ➕
```http
POST /api/v1/appointments
Content-Type: application/json
Authorization: Bearer <patient_token>
```

**📝 설명**: 환자가 진료 예약을 생성합니다. 예약 생성 시 자동으로 **WAITING_BEFORE_ARRIVAL(내원전)** 상태로 설정됩니다.

**🔍 Request Body (필수 필드):**
```json
{
  "memberId": 1,                    // 예약하는 환자 ID (필수)
  "hospitalId": 1,                  // 병원 ID (필수)
  "departmentName": "내과",          // 진료과명 (필수)
  "appointmentDate": "2025-09-03",  // 예약일 YYYY-MM-DD (필수)
  "appointmentTime": "14:30"        // 예약시간 HH:MM (필수)
}
```

**✅ Response (200 OK):**
```json
{
  "code": "APPOINTMENT_4001",
  "message": "예약이 성공적으로 생성되었습니다.",
  "data": 123  // 생성된 예약 ID
}
```

**❌ Error Cases:**
- `400`: 필수 필드 누락 또는 잘못된 날짜/시간 형식
- `409`: 이미 예약된 시간대 또는 중복 예약  
- `404`: 존재하지 않는 환자/병원/진료과

**📱 앱 개발 팁:**
- 예약 성공 시 확인 화면으로 이동
- 예약 정보를 로컬 저장소에 캐시
- 예약 실패 시 사용자 친화적 에러 메시지

### 4.2 환자 체크인 ✅
```http
PUT /api/v1/appointments/checkin
Content-Type: application/json
Authorization: Bearer <patient_token>
```

**📝 설명**: 환자가 병원에 도착하여 체크인합니다. 예약 상태가 **ARRIVED(대기중)**로 변경되고 FCM 알림이 관리자에게 전송됩니다.

**🔍 Request Body:**
```json
{
  "appointmentId": 123, // 체크인할 예약 ID (필수)
  "memberId": 1         // 체크인하는 환자 ID (필수)
}
```

**✅ Response (200 OK):**
```json
{
  "code": "APPOINTMENT_4002",
  "message": "체크인이 성공적으로 완료되었습니다.",
  "data": "SUCCESS"
}
```

**📎 동작 순서:**
1. 예약 상태 확인 (WAITING_BEFORE_ARRIVAL 또는 BOOKED)
2. 상태를 ARRIVED로 업데이트
3. 환자에게 확인 알림: "체크인이 완료되었습니다. 잠시 대기해주세요"

**📱 앱 개발 팁:**
- QR 코드 스캔 또는 버튼 탭으로 체크인
- 체크인 성공 시 대기 화면으로 전환
- 대기 시간 예상치 표시

### 4.3 예약 정보 수정 ✏️
```http
PUT /api/v1/appointments/{appointmentId}
Content-Type: application/json  
Authorization: Bearer <patient_token>
```

**📝 설명**: 기존 예약의 진료과, 날짜, 시간을 수정합니다.

**🔗 Path Parameters:**
- `appointmentId`: 수정할 예약 ID

**🔍 Request Body:**
```json
{
  "departmentName": "정형외과",      // 변경할 진료과명 (선택)
  "appointmentDate": "2025-09-04",  // 변경할 날짜 (선택)
  "appointmentTime": "15:00"        // 변경할 시간 (선택)
}
```

**✅ Response (200 OK):**
```json
{
  "code": "APPOINTMENT_4007",
  "message": "예약이 성공적으로 수정되었습니다.", 
  "data": "SUCCESS"
}
```

**❌ Error Cases:**
- `404`: 존재하지 않는 예약 ID
- `409`: 수정하려는 시간에 다른 예약 존재
- `400`: 과거 날짜로 변경 시도

---

## 📋 5. 내 예약 조회 API

### 5.1 내 전체 예약 목록 조회 📋
```http
GET /api/v1/appointments/my?memberId=1
Authorization: Bearer <patient_token>
```

**📝 설명**: 환자가 본인의 모든 예약 내역을 조회합니다. (과거/현재/미래 예약 포함)

**🔍 Query Parameters:**
- `memberId`: 환자 ID (필수)
- `status`: 예약 상태 필터 (선택) - `WAITING_BEFORE_ARRIVAL`, `ARRIVED`, `COMPLETED` 등
- `date`: 특정 날짜 필터 (선택) - YYYY-MM-DD 형식

**📎 사용 예시:**
```http
GET /api/v1/appointments/my?memberId=1
GET /api/v1/appointments/my?memberId=1&status=COMPLETED
GET /api/v1/appointments/my?memberId=1&date=2025-09-03
```

**✅ Response (200 OK):**
```json
{
  "code": "APPOINTMENT_4008",
  "message": "내 예약 목록 조회가 완료되었습니다.",
  "data": [
    {
      "appointmentId": 123,
      "hospitalName": "서울대학교병원",
      "departmentName": "내과",
      "appointmentDate": "2025-09-03",
      "appointmentTime": "14:30:00",
      "status": "WAITING_BEFORE_ARRIVAL",
      "statusDescription": "내원전",
      "canCheckin": false,  // 체크인 가능 여부 (당일에만 true)
      "createdAt": "2025-09-02T10:30:00"
    },
    {
      "appointmentId": 124,
      "hospitalName": "서울대학교병원", 
      "departmentName": "정형외과",
      "appointmentDate": "2025-09-01",
      "appointmentTime": "10:00:00",
      "status": "COMPLETED",
      "statusDescription": "완료됨",
      "canCheckin": false,
      "createdAt": "2025-08-30T15:20:00"
    }
  ]
}
```

**📱 앱 개발 팁:**
- 예약 목록을 날짜순으로 정렬 (최신순 권장)
- 상태별로 다른 색상/아이콘으로 구분
- Pull-to-refresh로 목록 갱신 기능

### 5.2 오늘 내 예약 조회 📅
```http
GET /api/v1/appointments/my/today?memberId=1
Authorization: Bearer <patient_token>
```

**📝 설명**: 환자가 오늘 예정된 본인의 예약만 조회합니다.

**🔍 Query Parameters:**
- `memberId`: 환자 ID (필수)

**✅ Response (200 OK):**
```json
{
  "code": "APPOINTMENT_4009",
  "message": "오늘 내 예약 조회가 완료되었습니다.",
  "data": [
    {
      "appointmentId": 125,
      "hospitalName": "서울대학교병원",
      "departmentName": "내과", 
      "appointmentTime": "14:30:00",
      "status": "BOOKED",
      "statusDescription": "예약됨",
      "canCheckin": true,      // 오늘 예약이므로 체크인 가능
      "timeUntilAppointment": "2시간 30분 후",  // 예약까지 남은 시간
      "hospitalAddress": "서울시 종로구 대학로"
    }
  ]
}
```

**📱 앱 개발 팁:**
- 메인 화면에 "오늘의 예약" 카드로 표시
- 예약 시간까지 카운트다운 표시
- "체크인" 버튼 눈에 띄게 배치

### 5.3 특정 예약 상세 조회 🔍
```http
GET /api/v1/appointments/{appointmentId}?memberId=1
Authorization: Bearer <patient_token>
```

**📝 설명**: 특정 예약의 상세 정보를 조회합니다.

**🔗 Path Parameters:**
- `appointmentId`: 예약 ID (필수)

**🔍 Query Parameters:**
- `memberId`: 환자 ID (필수, 본인 확인용)

**✅ Response (200 OK):**
```json
{
  "code": "APPOINTMENT_4010",
  "message": "예약 상세 조회가 완료되었습니다.",
  "data": {
    "appointmentId": 123,
    "memberName": "김환자",
    "hospitalName": "서울대학교병원",
    "hospitalAddress": "서울시 종로구 대학로",
    "hospitalPhone": "02-1234-5678",
    "departmentName": "내과",
    "appointmentDate": "2025-09-03",
    "appointmentTime": "14:30:00",
    "status": "WAITING_BEFORE_ARRIVAL",
    "statusDescription": "내원전",
    "createdAt": "2025-09-02T10:30:00",
    "updatedAt": "2025-09-02T10:30:00",
    "canModify": true,       // 수정 가능 여부
    "canCancel": true,       // 취소 가능 여부
    "canCheckin": false      // 체크인 가능 여부
  }
}
```

**❌ Error Cases:**
- `404`: 존재하지 않는 예약 ID
- `403`: 다른 환자의 예약에 접근 시도

**📱 앱 개발 팁:**
- 예약 상세 화면에서 "수정", "취소" 버튼 제공
- 병원 전화번호 터치 시 바로 통화 연결
- 병원 주소 터치 시 지도 앱 연동

### 5.4 내 예약 통계 조회 📊
```http
GET /api/v1/appointments/my/statistics?memberId=1
Authorization: Bearer <patient_token>
```

**📝 설명**: 환자의 예약 통계 정보를 제공합니다.

**✅ Response (200 OK):**
```json
{
  "code": "APPOINTMENT_4011", 
  "message": "내 예약 통계 조회가 완료되었습니다.",
  "data": {
    "totalAppointments": 15,      // 총 예약 횟수
    "completedAppointments": 12,  // 완료된 예약
    "cancelledAppointments": 2,   // 취소된 예약
    "upcomingAppointments": 1,    // 예정된 예약
    "favoriteHospital": "서울대학교병원",  // 가장 많이 방문한 병원
    "favoriteDepartment": "내과", // 가장 많이 이용한 진료과
    "lastAppointmentDate": "2025-09-01"
  }
}
```

**📱 앱 개발 팁:**
- 사용자 프로필 화면에 통계 표시
- 차트나 그래프로 시각화
- 자주 가는 병원/진료과 바로가기 제공

---

## 🔔 6. 알림 관리 API

### 6.1 FCM 토큰 등록 📲
```http
POST /api/v1/notifications/token
Content-Type: application/json
Authorization: Bearer <patient_token>
```

**📝 설명**: 푸시 알림을 받기 위한 FCM 토큰을 등록합니다. 앱 설치/업데이트 시 필수로 호출해야 합니다.

**🔍 Request Body:**
```json
{
  "memberId": 1,                    // 환자 ID (필수)
  "fcmToken": "dQGfH7VkS0uE...",    // FCM 토큰 (필수)
  "deviceType": "ANDROID"           // 디바이스 타입: "ANDROID" 또는 "IOS" (필수)
}
```

**✅ Response (200 OK):**
```json
{
  "data": "SUCCESS"
}
```

**📱 앱 개발 팁:**
- 앱 시작시 FCM 토큰 자동 등록
- 토큰 갱신 시에도 재등록 필요
- 권한 요청 후 토큰 등록 진행

### 6.2 알림 이력 조회 📋
```http
GET /api/v1/notifications/history?appointmentId=123
Authorization: Bearer <patient_token>
```

**📝 설명**: 특정 예약에 대한 알림 수신 이력을 조회합니다.

**🔍 Query Parameters:**
- `appointmentId`: 예약 ID (선택, 없으면 전체 이력)

---

## 📊 7. 모바일 앱 권장 UI 플로우

### 🏠 메인 화면
1. **다가오는 예약 정보**
   - 예약 날짜, 시간, 병원명, 진료과
   - "체크인" 버튼 (예약 당일에만 활성화)

2. **빠른 액션**
   - "AI 상담 시작" 버튼
   - "예약하기" 버튼
   - "내 예약" 버튼 (5.1 API 활용)

### 🤖 AI 상담 화면
1. **채팅 인터페이스**
   - 메신저 스타일 말풍선
   - 전송 버튼 및 텍스트 입력
   - 세션 완료 버튼

2. **상담 결과**
   - AI 분석 결과 (추천 진료과, 긴급도)
   - "예약하기" 버튼으로 바로 연결

### 📅 예약 화면
1. **병원/진료과 선택**
2. **날짜 선택** (캘린더)
3. **시간 선택** (3.1 API 활용)
4. **예약 확인 및 생성**

### 📋 내 예약 화면
1. **전체 예약 목록** (5.1 API 활용)
   - 날짜별/상태별 필터링
   - Pull-to-refresh 갱신
2. **예약 상세** (5.3 API 활용)  
   - 병원 정보, 예약 상세
   - 수정/취소 버튼

### ✅ 체크인 화면  
1. **QR 코드 스캔** 또는 **버튼 탭**
2. **체크인 완료 확인**
3. **대기 상태 표시**
   - 예상 대기시간
   - 현재 대기 순서

---

## 🔔 푸시 알림 시나리오

### 📱 환자가 받는 알림 종류

1. **예약 30분 전 알림**
   ```
   제목: "진료 시간이 다가왔습니다"
   내용: "30분 후 내과 진료 예정입니다. 병원으로 출발하세요."
   ```

2. **체크인 완료 알림**  
   ```
   제목: "체크인 완료"
   내용: "체크인이 완료되었습니다. 잠시 대기해주세요."
   ```

3. **진료실 호출 알림**
   ```
   제목: "진료실 호출"
   내용: "진료실로 들어오세요."
   ```

---

## ⚠️ 모바일 앱 개발 주의사항

### 🔒 보안
- JWT 토큰을 안전한 저장소에 저장 (iOS: Keychain, Android: Keystore)
- 네트워크 통신 시 HTTPS 사용 필수
- 토큰 만료 시 자동 재발급 처리

### 📱 사용자 경험
- 오프라인 상태에서도 기본 정보 표시 (캐시 활용)
- 네트워크 에러 시 재시도 메커니즘
- 로딩 상태 명확히 표시

### 🎯 접근성
- VoiceOver/TalkBack 지원
- 텍스트 크기 조절 대응
- 색상 대비 접근성 준수

### 🔔 푸시 알림  
- 알림 권한 요청 타이밍 최적화
- 알림 설정 페이지 제공
- 중요도에 따른 알림 분류

---

## 📞 문의 및 지원

- **개발팀**: development@carefreepass.com
- **기술 지원**: support@carefreepass.com
- **GitHub Issues**: https://github.com/9oormthon-univ/2025_SEASONTHON_TEAM_45_BE/issues

---

**최종 업데이트**: 2025년 9월 2일  
**버전**: 1.0.0  
**대상**: 모바일 앱 개발자 (iOS/Android)