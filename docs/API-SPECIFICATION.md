# CareFreePass API 명세서 📚

## 📋 개요

CareFreePass 병원 예약 시스템의 **완전한 API 명세서**입니다.  
모든 API 엔드포인트의 상세한 사용법, 요청/응답 예시, 에러 처리 방법을 포함합니다.

**Base URL**: `http://13.124.250.98:8080`  
**Swagger UI**: `http://13.124.250.98:8080/swagger-ui/index.html`  
**API Version**: `v1`

## 🎯 새로운 분리 명세서

**더 편리한 개발을 위해 용도별로 분리된 명세서를 제공합니다:**

- 📱 **[모바일 환자용 API](./명세서/모바일_환자용_API_명세서.md)**: 환자 앱 개발자용
- 🖥️ **[웹 관리자용 API](./명세서/웹_관리자용_API_명세서.md)**: 관리자 대시보드 개발자용  
- 🔗 **[공통 API](./명세서/공통_API_명세서.md)**: 웹/모바일 공통 개발자용
- 📋 **[명세서 가이드](./명세서/README.md)**: 전체 명세서 사용법

> 💡 **권장**: 본인의 개발 목적에 맞는 분리 명세서를 우선 참고하세요!

## 🔐 인증

### JWT Bearer Token 방식
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**⚠️ 현재 보안 설정**: 대부분의 API가 인증 없이 접근 가능 (개발/테스트용)  
**🔒 운영 환경**: JWT 토큰 필수 (향후 적용 예정)

## 📚 API 그룹별 목록

### 1. 🔑 인증 API (`/api/v1/auth`)
- 환자/병원 회원가입, 로그인
- JWT 토큰 재발급

### 2. 🏥 예약 관리 API (`/api/v1/appointments`)
- 예약 생성, 수정, 조회, 삭제
- 체크인, 상태 변경
- 오늘 예약/대기환자 목록

### 3. 🕒 환자용 시간대 조회 API (`/api/v1/patient/time-slots`)
- 예약 가능한 시간대 조회
- 특정 시간 예약 가능 여부 확인

### 4. 🔔 알림 API (`/api/v1/notifications`)
- FCM 토큰 등록
- 환자 호출 알림
- 알림 이력 조회

### 5. 💬 채팅 API (`/api/v1/chat`)
- AI 채팅 세션 시작/완료
- 메시지 송수신
- 채팅 세션 조회

### 6. 🏥 병원 관리자 API (`/api/v1/admin`)
- 진료과 관리
- 시간대 차단 관리

### 7. 🧪 테스트 API (`/api/v1/test`)
- 서버 상태 확인
- Echo 테스트

---

## 📖 상세 API 명세

### 🔑 1. 인증 API

#### 1.1 환자 회원가입 👤
```http
POST /api/v1/auth/patient/sign-up
Content-Type: application/json
```

**📝 설명**: 환자가 시스템에 회원가입하고 즉시 JWT 토큰을 발급받습니다.

**🔍 Request Body (필수 필드):**
```json
{
  "name": "김환자",           // 환자 이름 (2-20자)
  "gender": "남성",          // 성별: "남성" 또는 "여성"
  "birthDate": "19900315",   // 생년월일 (YYYYMMDD 형식)
  "phoneNumber": "01012345678", // 휴대폰 번호 (11자리)
  "password": "password123!"   // 비밀번호 (8자 이상, 특수문자 포함)
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

#### 1.2 환자 로그인 🔐
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

#### 1.3 병원 관리자 회원가입 🏥
```http
POST /api/v1/auth/hospital/sign-up
Content-Type: application/json
```

**📝 설명**: 병원 관리자가 시스템에 등록하고 관리자 권한을 부여받습니다.

**🔍 Request Body:**
```json
{
  "adminName": "관리자",              // 관리자 이름
  "adminEmail": "admin@hospital.com", // 관리자 이메일 주소
  "adminPassword": "password123!",    // 비밀번호 (8자 이상)
  "hospitalName": "서울대학교병원",    // 병원명
  "hospitalAddress": "서울시 종로구"   // 병원 주소
}
```

**✅ Response (200 OK):**
```json
{
  "code": "AUTH_2004",
  "message": "병원 회원가입이 완료되었습니다.",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "memberId": 100,
    "memberName": "관리자",
    "role": "HOSPITAL"
  }
}
```

#### 1.4 병원 관리자 로그인 👨‍⚕️
```http
POST /api/v1/auth/hospital/sign-in
Content-Type: application/json
```

**📝 설명**: 등록된 병원 관리자가 로그인합니다.

**🔍 Request Body:**
```json
{
  "adminEmail": "admin@hospital.com", // 등록된 이메일 주소
  "adminPassword": "password123!"     // 비밀번호
}
```

**✅ Response (200 OK):**
```json
{
  "code": "AUTH_2005",
  "message": "병원 로그인에 성공했습니다.",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "memberId": 100,
    "memberName": "관리자",
    "role": "HOSPITAL"
  }
}
```

#### 1.5 토큰 재발급 🔄
```http
POST /api/v1/auth/reissue
Content-Type: application/json
```

**📝 설명**: Access Token이 만료된 경우 Refresh Token으로 새 토큰 쌍을 발급받습니다.

**🔍 Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..." // 유효한 Refresh Token
}
```

**✅ Response (200 OK):**
```json
{
  "code": "AUTH_2003",
  "message": "토큰 재발급에 성공했습니다.",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",  // 새 Access Token
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...", // 새 Refresh Token
    "memberId": 1,
    "memberName": "김환자",
    "role": "PATIENT"
  }
}
```

**❌ Error Cases:**
- `400`: 잘못된 Refresh Token 형식
- `401`: 만료되거나 유효하지 않은 Refresh Token
- `404`: 토큰에 해당하는 사용자 없음

---

### 🏥 2. 예약 관리 API

#### 2.1 예약 생성 📝 (환자도 가능)
```http
POST /api/v1/appointments
Content-Type: application/json
Authorization: Bearer <token> # 운영환경에서 필수
```

**📝 설명**: 환자가 진료 예약을 생성합니다. 예약 생성 시 자동으로 **WAITING_BEFORE_ARRIVAL(내원전)** 상태로 설정됩니다.

**🔍 Request Body (필수 필드):**
```json
{
  "memberId": 1,                    // 예약하는 환자 ID
  "hospitalId": 1,                  // 병원 ID
  "departmentName": "내과",           // 진료과명
  "appointmentDate": "2025-09-03",   // 예약일 (YYYY-MM-DD)
  "appointmentTime": "14:30"         // 예약시간 (HH:MM)
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

#### 2.2 환자 체크인 ✅ (환자도 가능)
```http
PUT /api/v1/appointments/checkin
Content-Type: application/json
Authorization: Bearer <token> # 운영환경에서 필수
```

**📝 설명**: 환자가 병원에 도착하여 체크인합니다. 예약 상태가 **ARRIVED(대기중)**로 변경되고 FCM 알림이 전송됩니다.

**🔍 Request Body:**
```json
{
  "appointmentId": 123, // 체크인할 예약 ID
  "memberId": 1        // 체크인하는 환자 ID
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
3. FCM 알림 전송: "체크인이 완료되었습니다. 잠시 대기해주세요"

#### 2.3 오늘 대기 환자 조회 📋 (관리자 전용)
```http
GET /api/v1/appointments/today/waiting
Authorization: Bearer <hospital_admin_token> # 관리자 권한 필요
```

**📝 설명**: 병원 관리자가 오늘 체크인한 대기 환자 목록을 조회합니다. ARRIVED 상태의 환자들만 표시됩니다.

**✅ Response (200 OK):**
```json
{
  "code": "APPOINTMENT_4003",
  "message": "오늘 대기 환자 목록 조회가 완료되었습니다.",
  "data": [
    {
      "appointmentId": 1,
      "memberName": "김환자",
      "hospitalName": "구름대병원",
      "department": "내과",
      "appointmentDate": "2025-09-02",
      "appointmentTime": "12:00:00",
      "status": "ARRIVED",
      "statusDescription": "대기중",
      "canCall": true  // 호출 가능 여부
    },
    {
      "appointmentId": 2,
      "memberName": "이환자",
      "hospitalName": "구름대병원",
      "department": "정형외과",
      "appointmentDate": "2025-09-02",
      "appointmentTime": "14:30:00",
      "status": "ARRIVED",
      "statusDescription": "대기중",
      "canCall": true
    }
  ]
}
```

**📎 사용 팔:**
- 관리자 대시보드에서 대기중인 환자 목록 표시
- 환자 호출 버튼 활성화 여부 결정
- 대기 순서 및 예상 소요 시간 계산

#### 2.4 오늘 전체 예약 조회 📅 (관리자 전용)
```http
GET /api/v1/appointments/today
Authorization: Bearer <hospital_admin_token>
```

**📝 설명**: 오늘 날짜의 전체 예약 목록을 모든 상태별로 조회합니다.

**✅ Response (200 OK):**
```json
{
  "code": "APPOINTMENT_4004",
  "message": "오늘 예약 목록 조회가 완료되었습니다.",
  "data": [
    {
      "appointmentId": 1,
      "memberName": "김환자",
      "department": "내과",
      "appointmentTime": "09:00:00",
      "status": "COMPLETED",
      "statusDescription": "완료됨"
    },
    {
      "appointmentId": 2,
      "memberName": "이환자",
      "department": "정형외과",
      "appointmentTime": "14:30:00",
      "status": "ARRIVED",
      "statusDescription": "대기중"
    },
    {
      "appointmentId": 3,
      "memberName": "박환자",
      "department": "내과",
      "appointmentTime": "16:00:00",
      "status": "WAITING_BEFORE_ARRIVAL",
      "statusDescription": "내원전"
    }
  ]
}
```

#### 2.5 예약 상태 변경 🔄 (관리자 전용)
```http
PUT /api/v1/appointments/{appointmentId}/status/{status}
Authorization: Bearer <hospital_admin_token>
```

**📝 설명**: 관리자가 예약 상태를 수동으로 변경합니다.

**🔗 Path Parameters:**
- `appointmentId`: 예약 ID (Long)
- `status`: 변경할 상태 이름 (String)

**📄 가능한 상태 목록:**
- `WAITING_BEFORE_ARRIVAL`: 내원전
- `BOOKED`: 예약됨
- `ARRIVED`: 대기중
- `CALLED`: 호출됨
- `COMPLETED`: 완료됨
- `CANCELLED`: 취소됨

**📎 사용 예시:**
```http
PUT /api/v1/appointments/123/status/CALLED
```

**✅ Response (200 OK):**
```json
{
  "code": "APPOINTMENT_4006",
  "message": "예약 상태가 성공적으로 변경되었습니다.",
  "data": "SUCCESS"
}
```

**⚠️ 주의사항:**
- CALLED 로 변경 시 자동으로 FCM 호출 알림 전송
- COMPLETED 로 변경 시 예약 완료 처리
- 잘못된 상태 이름 입력 시 400 오류 반환

#### 2.6 예약 정보 수정 ✏️ (환자도 가능)
```http
PUT /api/v1/appointments/{appointmentId}
Content-Type: application/json
Authorization: Bearer <token>
```

**📝 설명**: 기존 예약의 진료과, 날짜, 시간을 수정합니다.

**🔗 Path Parameters:**
- `appointmentId`: 수정할 예약 ID

**🔍 Request Body:**
```json
{
  "departmentName": "정형외과",      // 변경할 진료과명
  "appointmentDate": "2025-09-04",   // 변경할 날짜 (YYYY-MM-DD)
  "appointmentTime": "15:00"         // 변경할 시간 (HH:MM)
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

#### 2.7 예약 삭제 🗑️ (관리자 전용)
```http
DELETE /api/v1/appointments/{appointmentId}
Authorization: Bearer <hospital_admin_token>
```

**📝 설명**: 관리자가 예약을 완전히 삭제합니다. (취소와 다름)

**🔗 Path Parameters:**
- `appointmentId`: 삭제할 예약 ID

**✅ Response (200 OK):**
```json
{
  "code": "APPOINTMENT_4005",
  "message": "예약이 성공적으로 삭제되었습니다.",
  "data": "SUCCESS"
}
```

**⚠️ 주의사항:**
- 삭제된 예약은 복구할 수 없습니다
- 삭제 대신 CANCELLED 상태로 변경을 권장합니다

---

### 🕒 3. 환자용 시간대 조회 API

#### 3.1 예약 가능한 시간대 조회 🔍
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
    "timeSlots": [
      {
        "time": "09:00",
        "available": true   // 예약 가능
      },
      {
        "time": "09:30", 
        "available": false  // 이미 예약됨 또는 차단됨
      },
      {
        "time": "10:00",
        "available": true
      },
      {
        "time": "10:30",
        "available": true
      },
      {
        "time": "14:00",
        "available": false  // 점심시간대 차단
      },
      {
        "time": "14:30",
        "available": true
      }
    ]
  }
}
```

**📎 사용 팔:**
- 환자 예약 생성 전 시간대 선택
- 캐린더 UI에서 가능/불가능 시간 표시
- 예약 가능 여부 실시간 확인

#### 3.2 특정 시간 예약 가능 여부 확인 ✓
```http
GET /api/v1/patient/time-slots/check?hospitalId=1&departmentName=내과&date=2025-09-03&time=14:30
```

**📝 설명**: 특정 시간대에 예약이 가능한지 단건 확인합니다.

**🔍 Query Parameters (필수):**
- `hospitalId`: 병원 ID (Long)
- `departmentName`: 진료과명 (String)
- `date`: 조회할 날짜 (LocalDate) - YYYY-MM-DD
- `time`: 확인할 시간 (String) - HH:MM 형식

**📎 사용 예시:**
```http
GET /api/v1/patient/time-slots/check?hospitalId=1&departmentName=내과&date=2025-09-03&time=14:30
GET /api/v1/patient/time-slots/check?hospitalId=1&departmentName=정형외과&date=2025-09-05&time=09:00
```

**✅ Response (200 OK) - 예약 가능:**
```json
{
  "code": "TIME_SLOT_1002",
  "message": "시간대 예약 가능 여부 확인이 완료되었습니다.",
  "data": true  // true: 예약 가능, false: 예약 불가
}
```

**✅ Response (200 OK) - 예약 불가:**
```json
{
  "code": "TIME_SLOT_1002",
  "message": "시간대 예약 가능 여부 확인이 완료되었습니다.",
  "data": false
}
```

**📎 사용 팔:**
- 예약 생성 전 최종 유효성 검사
- 실시간 시간대 상태 확인
- 예약 버튼 활성화 여부 결정

---

### 🔔 4. 알림 API

#### 4.1 FCM 토큰 등록
```http
POST /api/v1/notifications/token
```

**Request Body:**
```json
{
  "memberId": 1,
  "fcmToken": "dQGfH7VkS0uE...",
  "deviceType": "ANDROID"
}
```

#### 4.2 환자 호출
```http
POST /api/v1/notifications/call
```

**Request Body:**
```json
{
  "appointmentId": 123
}
```

**📝 Note**: 진료실 번호는 자동으로 "진료실"로 설정됩니다.

**Response (200):**
```json
{
  "code": "SUCCESS",
  "message": "환자 호출이 성공했습니다.",
  "data": "SUCCESS"
}
```

#### 4.3 알림 이력 조회
```http
GET /api/v1/notifications/history?appointmentId=123
```

#### 4.4 특정 예약의 알림 이력 조회
```http
GET /api/v1/notifications/history/appointment/{appointmentId}
```

---

### 💬 5. 채팅 API

#### 5.1 AI 채팅 세션 시작
```http
POST /api/v1/chat/start
```

**Request Body:**
```json
{
  "memberId": 1,
  "initialMessage": "배가 아프고 열이 나요"
}
```

**Response (200):**
```json
{
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
```

#### 5.2 채팅 메시지 전송
```http
POST /api/v1/chat/message
```

**Request Body:**
```json
{
  "sessionId": 456,
  "memberId": 1,
  "content": "어제 저녁부터 아팠어요"
}
```

#### 5.3 사용자 채팅 세션 목록 조회
```http
GET /api/v1/chat/sessions?memberId=1
```

#### 5.4 채팅 세션 상세 조회
```http
GET /api/v1/chat/sessions/{sessionId}?memberId=1
```

#### 5.5 채팅 세션 완료
```http
PUT /api/v1/chat/sessions/{sessionId}/complete?memberId=1
```

---

### 🏥 6. 병원 관리자 API

#### 6.1 진료과 관리

##### 6.1.1 진료과 생성
```http
POST /api/v1/admin/hospitals/{hospitalId}/departments
```

**Request Body:**
```json
{
  "name": "정형외과",
  "description": "뼈, 관절, 근육 질환 전문"
}
```

##### 6.1.2 병원별 진료과 목록 조회
```http
GET /api/v1/admin/hospitals/{hospitalId}/departments
```

##### 6.1.3 진료과 상세 조회
```http
GET /api/v1/admin/hospitals/{hospitalId}/departments/{departmentId}
```

##### 6.1.4 진료과 정보 수정
```http
PUT /api/v1/admin/hospitals/{hospitalId}/departments/{departmentId}
```

##### 6.1.5 진료과 삭제
```http
DELETE /api/v1/admin/hospitals/{hospitalId}/departments/{departmentId}
```

#### 6.2 시간대 차단 관리

##### 6.2.1 시간대 차단
```http
POST /api/v1/admin/time-slots/block
```

**Request Body:**
```json
{
  "departmentId": 1,
  "date": "2025-09-03",
  "startTime": "14:00",
  "endTime": "15:00",
  "reason": "정기점검"
}
```

##### 6.2.2 차단된 시간대 조회
```http
GET /api/v1/admin/time-slots/blocked?departmentId=1
```

##### 6.2.3 특정 날짜 차단 시간대 조회
```http
GET /api/v1/admin/time-slots/blocked/date?departmentId=1&date=2025-09-03
```

##### 6.2.4 시간대 차단 수정
```http
PUT /api/v1/admin/time-slots/{exceptionId}
```

##### 6.2.5 시간대 차단 해제
```http
DELETE /api/v1/admin/time-slots/{exceptionId}
```

---

### 🧪 7. 테스트 API

#### 7.1 서버 상태 확인
```http
GET /api/v1/test/health
```

**Response (200):**
```json
{
  "code": "TEST_2001",
  "message": "서버가 정상 작동 중입니다.",
  "data": "OK"
}
```

#### 7.2 Echo 테스트
```http
POST /api/v1/test/echo
```

**Request Body:**
```json
"Hello World"
```

---

## 📊 예약 상태 플로우

### 예약 상태 변화
```
WAITING_BEFORE_ARRIVAL → BOOKED → ARRIVED → CALLED → COMPLETED
       내원전           예약됨     대기중     호출됨     완료됨
         ↓                                              ↑
     CANCELLED ←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←← 취소됨
```

### 상태별 설명
- **WAITING_BEFORE_ARRIVAL**: 예약 생성 후 초기 상태 (내원전)
- **BOOKED**: 예약 시간 접근 시 (예약됨)  
- **ARRIVED**: 환자 체크인 완료 시 (대기중)
- **CALLED**: 진료실 호출 시 (호출됨)
- **COMPLETED**: 진료 완료 시 (완료됨)
- **CANCELLED**: 예약 취소 시 (취소됨)

### 각 상태별 알림
1. **WAITING_BEFORE_ARRIVAL**: "30분 후 진료 시간입니다. 병원으로 출발하세요"
2. **ARRIVED**: "체크인이 완료되었습니다. 잠시 대기해주세요"
3. **CALLED**: "진료실로 들어오세요"

---

## 🔒 보안 정보

### ⚠️ 현재 보안 상태
**대부분의 API가 인증 없이 접근 가능** (개발/테스트 환경)

### 인증이 필요한 API (향후 적용 예정)
- 🔒 관리자 전용: 대기환자 조회, 예약 삭제, 상태 변경, 진료과 관리
- 🔐 환자 인증: 본인 예약만 수정/조회 가능
- 🔑 공개: 회원가입, 로그인, 시간대 조회

### 권한별 API 접근 표

| API 그룹 | 환자 | 관리자 | 공개 |
|---------|------|--------|------|
| 인증 | ✅ | ✅ | ✅ |
| 예약 생성/수정 | ✅ | ✅ | ❌ |
| 예약 조회 (전체) | ❌ | ✅ | ❌ |
| 환자 호출 | ❌ | ✅ | ❌ |
| 시간대 조회 | ✅ | ✅ | ✅ |
| 채팅 | ✅ | ❌ | ❌ |
| 진료과 관리 | ❌ | ✅ | ❌ |

---

## 📱 클라이언트 개발 가이드

### 1. 기본 사용 흐름

#### 환자 앱
```
1. 회원가입/로그인
2. FCM 토큰 등록
3. AI 채팅으로 증상 상담
4. 예약 생성
5. 체크인 (병원 도착 시)
6. 호출 알림 수신
```

#### 관리자 웹
```
1. 관리자 로그인
2. 오늘 대기 환자 목록 조회
3. 환자 호출
4. 예약 상태 관리
5. 진료과/시간 관리
```

### 2. 에러 처리

#### 공통 에러 응답 형식
```json
{
  "code": "ERROR_CODE",
  "message": "에러 메시지",
  "data": null
}
```

#### 주요 에러 코드
- `MEMBER_NOT_FOUND`: 존재하지 않는 회원
- `APPOINTMENT_NOT_FOUND`: 존재하지 않는 예약
- `APPOINTMENT_DUPLICATE_DATE`: 중복 예약
- `APPOINTMENT_TIME_UNAVAILABLE`: 예약 불가능한 시간
- `DEVICE_TOKEN_NOT_FOUND`: FCM 토큰 미등록
- `CHAT_SESSION_NOT_FOUND`: 존재하지 않는 채팅 세션

### 3. WebSocket 연결 (실시간 채팅)

**연결 엔드포인트**: `/ws/chat`  
**프로토콜**: STOMP over SockJS  
**상세 가이드**: `docs/chat/websocket-guide.md` 참조

---

## 🔗 관련 문서

- **Swagger UI**: http://13.124.250.98:8080/swagger-ui/index.html
- **WebSocket 가이드**: `docs/chat/websocket-guide.md`
- **프로젝트 개요**: `docs/프로젝트개요.md`
- **알림 시스템**: `docs/알림시스템가이드.md`

---

## 📞 문의 및 지원

- **개발팀**: development@carefreepass.com
- **기술 지원**: support@carefreepass.com
- **GitHub Issues**: https://github.com/9oormthon-univ/2025_SEASONTHON_TEAM_45_BE/issues

---

**최종 업데이트**: 2025년 9월 2일  
**버전**: 1.0.0  
**작성자**: CareFreePass 개발팀