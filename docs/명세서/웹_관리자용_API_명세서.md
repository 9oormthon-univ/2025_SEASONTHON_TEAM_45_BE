# CareFreePass 웹 관리자용 API 명세서 🖥️

## 📋 개요

CareFreePass 병원 예약 시스템의 **웹 관리자용 API 명세서**입니다.  
병원 관리자가 웹 대시보드에서 사용하는 모든 API 엔드포인트를 포함합니다.

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

## 🏥 1. 병원 관리자 인증 API

### 1.1 관리자 회원가입 📝
```http
POST /api/v1/auth/hospital/sign-up
Content-Type: application/json
```

**📝 설명**: 병원 관리자가 시스템에 등록하고 관리자 권한을 부여받습니다.

**🔍 Request Body:**
```json
{
  "adminName": "김관리자",              // 관리자 이름 (필수)
  "adminEmail": "admin@hospital.com",   // 관리자 이메일 주소 (필수)
  "adminPassword": "password123!",      // 비밀번호 8자 이상 (필수)
  "hospitalName": "서울대학교병원",      // 병원명 (필수)
  "hospitalAddress": "서울시 종로구"     // 병원 주소 (필수)
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
    "memberName": "김관리자",
    "role": "HOSPITAL"
  }
}
```

### 1.2 관리자 로그인 🔐
```http
POST /api/v1/auth/hospital/sign-in
Content-Type: application/json
```

**📝 설명**: 등록된 병원 관리자가 로그인합니다.

**🔍 Request Body:**
```json
{
  "adminEmail": "admin@hospital.com",  // 등록된 이메일 주소
  "adminPassword": "password123!"      // 비밀번호
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
    "memberName": "김관리자",
    "role": "HOSPITAL"
  }
}
```

---

## 👥 2. 환자 관리 API

### 2.1 오늘 대기 환자 목록 조회 📋
```http
GET /api/v1/appointments/today/waiting
Authorization: Bearer <hospital_admin_token>
```

**📝 설명**: 오늘 체크인한 대기 환자 목록을 조회합니다. 관리자 대시보드의 핵심 기능입니다.

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
    }
  ]
}
```

### 2.2 오늘 전체 예약 목록 조회 📅
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
    }
  ]
}
```

### 2.3 환자 호출 📞
```http
POST /api/v1/notifications/call
Content-Type: application/json
Authorization: Bearer <hospital_admin_token>
```

**📝 설명**: 대기중인 환자를 진료실로 호출합니다. FCM 푸시 알림이 환자 앱으로 전송됩니다.

**🔍 Request Body:**
```json
{
  "appointmentId": 123  // 호출할 예약 ID
}
```

**✅ Response (200 OK):**
```json
{
  "code": "SUCCESS",
  "message": "환자 호출이 성공했습니다.",
  "data": "SUCCESS"
}
```

**📎 동작 과정:**
1. 예약 상태를 `CALLED(호출됨)`로 변경
2. 환자 앱으로 "진료실로 들어오세요" FCM 푸시 알림 전송
3. 알림 이력에 호출 기록 저장

### 2.4 예약 상태 수동 변경 🔄
```http
PUT /api/v1/appointments/{appointmentId}/status/{status}
Authorization: Bearer <hospital_admin_token>
```

**📝 설명**: 관리자가 예약 상태를 수동으로 변경합니다.

**🔗 Path Parameters:**
- `appointmentId`: 예약 ID (Long)
- `status`: 변경할 상태 이름 (String)

**📄 가능한 상태:**
- `WAITING_BEFORE_ARRIVAL`: 내원전
- `BOOKED`: 예약됨
- `ARRIVED`: 대기중
- `CALLED`: 호출됨
- `COMPLETED`: 완료됨
- `CANCELLED`: 취소됨

**📎 사용 예시:**
```http
PUT /api/v1/appointments/123/status/COMPLETED
```

**✅ Response (200 OK):**
```json
{
  "code": "APPOINTMENT_4006",
  "message": "예약 상태가 성공적으로 변경되었습니다.",
  "data": "SUCCESS"
}
```

### 2.5 예약 강제 삭제 🗑️
```http
DELETE /api/v1/appointments/{appointmentId}
Authorization: Bearer <hospital_admin_token>
```

**📝 설명**: 관리자가 예약을 완전히 삭제합니다. (복구 불가능)

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
- 일반적으로는 `CANCELLED` 상태로 변경을 권장합니다

---

## 🏥 3. 진료과 관리 API

### 3.1 진료과 생성 ➕
```http
POST /api/v1/admin/hospitals/{hospitalId}/departments
Content-Type: application/json
Authorization: Bearer <hospital_admin_token>
```

**📝 설명**: 병원에 새로운 진료과를 추가합니다.

**🔗 Path Parameters:**
- `hospitalId`: 병원 ID

**🔍 Request Body:**
```json
{
  "name": "정형외과",                    // 진료과명 (필수)
  "description": "뼈, 관절, 근육 질환 전문"  // 진료과 설명 (선택)
}
```

**✅ Response (200 OK):**
```json
{
  "code": "SUCCESS",
  "message": "진료과가 성공적으로 생성되었습니다.",
  "data": 5  // 생성된 진료과 ID
}
```

### 3.2 병원별 진료과 목록 조회 📋
```http
GET /api/v1/admin/hospitals/{hospitalId}/departments
Authorization: Bearer <hospital_admin_token>
```

**📝 설명**: 특정 병원의 모든 진료과 목록을 조회합니다.

**✅ Response (200 OK):**
```json
{
  "code": "SUCCESS",
  "message": "진료과 목록 조회가 완료되었습니다.",
  "data": [
    {
      "id": 1,
      "name": "내과",
      "description": "내과 질환 전문",
      "isActive": true,
      "createdAt": "2025-09-01T10:00:00"
    },
    {
      "id": 2,
      "name": "정형외과",
      "description": "뼈, 관절, 근육 질환 전문",
      "isActive": true,
      "createdAt": "2025-09-02T14:30:00"
    }
  ]
}
```

### 3.3 진료과 상세 조회 🔍
```http
GET /api/v1/admin/hospitals/{hospitalId}/departments/{departmentId}
Authorization: Bearer <hospital_admin_token>
```

### 3.4 진료과 정보 수정 ✏️
```http
PUT /api/v1/admin/hospitals/{hospitalId}/departments/{departmentId}
Content-Type: application/json
Authorization: Bearer <hospital_admin_token>
```

**🔍 Request Body:**
```json
{
  "name": "정형외과",                    // 수정할 진료과명
  "description": "뼈, 관절, 근육, 인대 질환 전문"  // 수정할 설명
}
```

### 3.5 진료과 비활성화 ❌
```http
DELETE /api/v1/admin/hospitals/{hospitalId}/departments/{departmentId}
Authorization: Bearer <hospital_admin_token>
```

**📝 설명**: 진료과를 비활성화합니다. (완전 삭제가 아닌 숨김 처리)

---

## ⏰ 4. 시간대 차단 관리 API

### 4.1 시간대 차단 🚫
```http
POST /api/v1/admin/time-slots/block
Content-Type: application/json
Authorization: Bearer <hospital_admin_token>
```

**📝 설명**: 특정 날짜의 시간대를 예약 불가능하도록 차단합니다.

**🔍 Request Body:**
```json
{
  "departmentId": 1,               // 진료과 ID (필수)
  "date": "2025-09-03",            // 차단 날짜 (필수)
  "startTime": "14:00",            // 시작 시간 (필수) 
  "endTime": "15:00",              // 종료 시간 (필수)
  "reason": "정기점검"              // 차단 사유 (선택)
}
```

**✅ Response (200 OK):**
```json
{
  "code": "SUCCESS",
  "message": "시간대가 성공적으로 차단되었습니다.",
  "data": 10  // 생성된 차단 규칙 ID
}
```

### 4.2 차단된 시간대 조회 📋
```http
GET /api/v1/admin/time-slots/blocked?departmentId=1
Authorization: Bearer <hospital_admin_token>
```

**🔍 Query Parameters:**
- `departmentId`: 진료과 ID (필수)

**✅ Response (200 OK):**
```json
{
  "code": "SUCCESS",
  "message": "차단된 시간대 조회가 완료되었습니다.",
  "data": [
    {
      "id": 10,
      "departmentId": 1,
      "departmentName": "내과",
      "date": "2025-09-03",
      "startTime": "14:00:00",
      "endTime": "15:00:00",
      "reason": "정기점검",
      "createdAt": "2025-09-02T10:30:00"
    }
  ]
}
```

### 4.3 특정 날짜 차단 시간대 조회 📅
```http
GET /api/v1/admin/time-slots/blocked/date?departmentId=1&date=2025-09-03
Authorization: Bearer <hospital_admin_token>
```

**🔍 Query Parameters:**
- `departmentId`: 진료과 ID (필수)
- `date`: 조회할 날짜 YYYY-MM-DD (필수)

### 4.4 시간대 차단 정보 수정 ✏️
```http
PUT /api/v1/admin/time-slots/{exceptionId}
Content-Type: application/json
Authorization: Bearer <hospital_admin_token>
```

### 4.5 시간대 차단 해제 🔓
```http
DELETE /api/v1/admin/time-slots/{exceptionId}
Authorization: Bearer <hospital_admin_token>
```

**📝 설명**: 차단된 시간대를 다시 예약 가능하도록 해제합니다.

---

## 📊 5. 알림 이력 관리 API

### 5.1 전체 알림 이력 조회 📋
```http
GET /api/v1/notifications/history
Authorization: Bearer <hospital_admin_token>
```

**📝 설명**: 병원의 모든 푸시 알림 전송 이력을 조회합니다.

### 5.2 특정 예약 알림 이력 조회 🔍
```http
GET /api/v1/notifications/history/appointment/{appointmentId}
Authorization: Bearer <hospital_admin_token>
```

**📝 설명**: 특정 예약에 대한 모든 알림 전송 이력을 조회합니다.

**✅ Response (200 OK):**
```json
{
  "code": "SUCCESS",
  "message": "알림 이력 조회가 완료되었습니다.",
  "data": [
    {
      "id": 1,
      "appointmentId": 123,
      "memberName": "김환자",
      "notificationType": "PATIENT_CALL",
      "message": "진료실로 들어오세요",
      "sentAt": "2025-09-02T14:30:00",
      "isSuccess": true,
      "errorMessage": null
    }
  ]
}
```

---

## 🎯 6. 웹 대시보드 권장 UI 플로우

### 📋 메인 대시보드
1. **실시간 대기 환자 목록** (2.1 API 사용)
   - 환자명, 진료과, 예약시간, 대기시간 표시
   - 각 환자별 "호출" 버튼 제공

2. **빠른 액션 버튼**
   - 환자 호출 (2.3 API)
   - 상태 변경 드롭다운 (2.4 API)

### 📅 예약 관리 페이지
1. **오늘 전체 예약 목록** (2.2 API 사용)
   - 상태별 필터링 (완료됨, 대기중, 취소됨 등)
   - 예약 상세 정보 모달

### ⚙️ 설정 페이지  
1. **진료과 관리** (3.1~3.5 API 사용)
   - 진료과 추가/수정/비활성화
   
2. **시간대 관리** (4.1~4.5 API 사용)
   - 휴진/점검 시간 설정
   - 차단된 시간대 달력 뷰

---

## ⚠️ 주의사항

### 🔒 보안
- 모든 API는 관리자 권한(`HOSPITAL` role) 필요
- JWT 토큰 만료 시 자동 재발급 처리 구현 권장

### 🎯 사용자 경험
- 실시간 데이터 갱신을 위한 폴링 또는 WebSocket 고려
- 환자 호출 후 자동 상태 변경 구현
- 에러 발생 시 사용자 친화적 메시지 표시

### 📱 반응형 대응
- 태블릿에서도 사용 가능한 반응형 디자인 권장
- 모바일 브라우저에서의 기본 기능 지원

---

## 📞 문의 및 지원

- **개발팀**: development@carefreepass.com
- **기술 지원**: support@carefreepass.com  
- **GitHub Issues**: https://github.com/9oormthon-univ/2025_SEASONTHON_TEAM_45_BE/issues

---

**최종 업데이트**: 2025년 9월 2일  
**버전**: 1.0.0  
**대상**: 웹 관리자 대시보드 개발자