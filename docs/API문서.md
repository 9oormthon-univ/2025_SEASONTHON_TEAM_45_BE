# API 문서

CareFreePass 시스템의 전체 REST API 명세서입니다.

## 🌐 기본 정보

- **Base URL**: `http://localhost:8080/api/v1`
- **Content-Type**: `application/json`
- **인코딩**: UTF-8

## 🔐 인증

현재는 개발 단계로 인증이 비활성화되어 있습니다. 프로덕션에서는 JWT 토큰 기반 인증을 사용합니다.

```http
Authorization: Bearer {jwt_token}
```

## 📋 공통 응답 형식

### 성공 응답
```json
{
  "status": "OK",
  "code": "SUCCESS_CODE", 
  "message": "성공 메시지",
  "data": "응답 데이터"
}
```

### 오류 응답
```json
{
  "status": "ERROR",
  "code": "ERROR_CODE",
  "message": "오류 메시지", 
  "data": null
}
```

## 👤 회원 관리 API

### 환자 회원가입
```http
POST /auth/patient/sign-up
```

**Request Body:**
```json
{
  "name": "김환자",
  "gender": "남성", 
  "birthDate": "19900315",
  "phoneNumber": "01012345678",
  "password": "password123!"
}
```

**Response:**
```json
{
  "status": "OK",
  "code": "AUTH_2001",
  "message": "회원가입이 완료되었습니다.",
  "data": {
    "memberId": 1,
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

**Error Codes:**
- `AUTH_4001`: 이미 등록된 전화번호입니다
- `AUTH_4002`: 필수 정보가 누락되었습니다
- `AUTH_5001`: 회원가입에 실패했습니다

## 🏥 예약 관리 API

### 예약 생성
```http
POST /appointments
```

**Request Body:**
```json
{
  "memberId": 1,
  "hospitalName": "서울대병원",
  "department": "내과",
  "doctorName": "김의사", 
  "appointmentDate": "2025-08-30",
  "appointmentTime": "10:30:00",
  "roomNumber": "2번 진료실"
}
```

**Response:**
```json
{
  "status": "OK",
  "code": "APPOINTMENT_2001",
  "message": "예약이 성공적으로 생성되었습니다.",
  "data": 15
}
```

### 오늘 전체 예약 조회
```http
GET /appointments/today
```

**Response:**
```json
{
  "status": "OK", 
  "code": "APPOINTMENT_2004",
  "message": "오늘 전체 예약 목록 조회가 완료되었습니다.",
  "data": [
    {
      "appointmentId": 15,
      "memberName": "김환자",
      "hospitalName": "서울대병원", 
      "department": "내과",
      "doctorName": "김의사",
      "appointmentDate": "2025-08-30",
      "appointmentTime": "10:30:00",
      "roomNumber": "2번 진료실",
      "status": "BOOKED",
      "statusDescription": "예약됨",
      "canCall": true
    }
  ]
}
```

### 오늘 대기 환자 조회
```http
GET /appointments/today/waiting  
```

대기 중인 환자 (BOOKED, ARRIVED 상태)만 조회합니다.

### 예약 수정
```http
PUT /appointments/{appointmentId}
```

**Request Body:**
```json
{
  "hospitalName": "서울대병원",
  "department": "외과",
  "doctorName": "이의사",
  "appointmentDate": "2025-08-31", 
  "appointmentTime": "14:00:00",
  "roomNumber": "3번 진료실"
}
```

**Response:**
```json
{
  "status": "OK",
  "code": "APPOINTMENT_2007", 
  "message": "예약이 성공적으로 수정되었습니다.",
  "data": "SUCCESS"
}
```

### 예약 삭제
```http
DELETE /appointments/{appointmentId}
```

### 환자 체크인
```http
PUT /appointments/checkin
```

**Request Body:**
```json
{
  "appointmentId": 15,
  "memberId": 1
}
```

### 예약 상태 변경
```http
PUT /appointments/{appointmentId}/status/{status}
```

**Parameters:**
- `status`: BOOKED, ARRIVED, CALLED, COMPLETED, CANCELLED

**Example:**
```http
PUT /appointments/15/status/COMPLETED
```

## 🔔 알림 관리 API

### FCM 토큰 등록
```http
POST /notifications/token
```

**Request Body:**
```json
{
  "memberId": 1,
  "fcmToken": "dSN96KjJ1zyWVGoDdnf2yp:APA91bFvJEKyLryZ0kJ1XXJN8wpQDrKV-tUM5oHvQbeVTa0JJ_9wMqnCkDZUH_mOOwvgZPx4OhqNt5HChK96IHUkdgQgEepJquGIpgyUInDtlB42cGw8ehE",
  "deviceType": "ANDROID"
}
```

**Response:**
```json
{
  "status": "OK",
  "code": "NOTIFICATION_2001",
  "message": "FCM 토큰이 성공적으로 등록되었습니다.", 
  "data": "SUCCESS"
}
```

### 환자 호출 (핵심 기능)
```http
POST /notifications/call
```

**Request Body:**
```json
{
  "appointmentId": 15,
  "roomNumber": "2번 진료실"
}
```

**Response:**
```json
{
  "status": "OK",
  "code": "NOTIFICATION_2004",
  "message": "환자 호출이 완료되었습니다.",
  "data": "SUCCESS"
}
```

**호출 프로세스:**
1. 예약 정보 조회 및 호출 가능 여부 확인
2. 환자의 FCM 토큰 조회
3. FCM 푸시 알림 전송
4. 성공 시 예약 상태를 CALLED로 변경
5. 알림 이력 저장

## 📊 응답 코드 정리

### 성공 코드 (2xxx)
| 코드 | 설명 |
|-----|------|
| AUTH_2001 | 회원가입 성공 |
| AUTH_2002 | 로그인 성공 |
| APPOINTMENT_2001 | 예약 생성 성공 |
| APPOINTMENT_2002 | 체크인 성공 |
| APPOINTMENT_2003 | 대기 환자 목록 조회 성공 |
| APPOINTMENT_2004 | 전체 예약 목록 조회 성공 |
| APPOINTMENT_2005 | 예약 삭제 성공 |
| APPOINTMENT_2006 | 예약 상태 변경 성공 |
| APPOINTMENT_2007 | 예약 수정 성공 |
| NOTIFICATION_2001 | FCM 토큰 등록 성공 |
| NOTIFICATION_2004 | 환자 호출 성공 |

### 클라이언트 에러 (4xxx)
| 코드 | 설명 |
|-----|------|
| AUTH_4001 | 이미 등록된 전화번호 |
| AUTH_4002 | 필수 정보 누락 |
| APPOINTMENT_4001 | 존재하지 않는 회원/이미 예약 존재 |
| APPOINTMENT_4002 | 본인 예약이 아님 |
| APPOINTMENT_4003 | 존재하지 않는 예약 |
| APPOINTMENT_4004 | 잘못된 상태값 |
| APPOINTMENT_4005 | 수정 불가능한 예약 상태 |
| NOTIFICATION_4001 | 존재하지 않는 회원 |
| NOTIFICATION_4004 | 호출 불가능한 예약 상태 |

### 서버 에러 (5xxx)
| 코드 | 설명 |
|-----|------|
| AUTH_5001 | 회원가입 실패 |
| APPOINTMENT_5001-5007 | 각종 예약 관련 서버 에러 |
| NOTIFICATION_5001 | FCM 토큰 등록 실패 |
| NOTIFICATION_5004 | FCM 토큰이 없음 |
| NOTIFICATION_5005 | 푸시 알림 전송 실패 |

## 🧪 API 테스트 가이드

### 1. 테스트 도구
- **브라우저**: `http://localhost:8080/test-fcm.html`
- **Postman**: API 컬렉션 임포트
- **curl**: 명령줄 테스트
- **HTTPie**: 사용자 친화적인 HTTP 클라이언트

### 2. 테스트 시나리오

#### 기본 워크플로우
```bash
# 1. 회원가입
curl -X POST http://localhost:8080/api/v1/auth/patient/sign-up \
  -H "Content-Type: application/json" \
  -d '{
    "name": "김환자",
    "gender": "남성", 
    "birthDate": "19900315",
    "phoneNumber": "01012345678",
    "password": "password123!"
  }'

# 2. FCM 토큰 등록 (웹 페이지에서 생성한 토큰 사용)
curl -X POST http://localhost:8080/api/v1/notifications/token \
  -H "Content-Type: application/json" \
  -d '{
    "memberId": 1,
    "fcmToken": "GENERATED_FCM_TOKEN",
    "deviceType": "ANDROID"
  }'

# 3. 예약 생성
curl -X POST http://localhost:8080/api/v1/appointments \
  -H "Content-Type: application/json" \
  -d '{
    "memberId": 1,
    "hospitalName": "서울대병원",
    "department": "내과",
    "doctorName": "김의사",
    "appointmentDate": "2025-08-30", 
    "appointmentTime": "10:30:00",
    "roomNumber": "2번 진료실"
  }'

# 4. 환자 호출
curl -X POST http://localhost:8080/api/v1/notifications/call \
  -H "Content-Type: application/json" \
  -d '{
    "appointmentId": 1,
    "roomNumber": "2번 진료실"
  }'
```

#### 상태 변경 테스트
```bash
# 체크인
curl -X PUT http://localhost:8080/api/v1/appointments/checkin \
  -H "Content-Type: application/json" \
  -d '{
    "appointmentId": 1,
    "memberId": 1
  }'

# 상태를 완료로 변경
curl -X PUT http://localhost:8080/api/v1/appointments/1/status/COMPLETED
```

### 3. 에러 상황 테스트

#### 잘못된 회원 ID
```bash
curl -X POST http://localhost:8080/api/v1/appointments \
  -H "Content-Type: application/json" \
  -d '{
    "memberId": 999,
    "hospitalName": "서울대병원",
    "department": "내과",
    "appointmentDate": "2025-08-30",
    "appointmentTime": "10:30:00"
  }'
```

#### FCM 토큰 없이 호출
```bash
curl -X POST http://localhost:8080/api/v1/notifications/call \
  -H "Content-Type: application/json" \
  -d '{
    "appointmentId": 1,
    "roomNumber": "2번 진료실"
  }'
```

## 📝 요청/응답 예시

### 완전한 예약 워크플로우

1. **회원가입**
```json
POST /auth/patient/sign-up
{
  "name": "김환자",
  "gender": "남성",
  "birthDate": "19900315", 
  "phoneNumber": "01012345678",
  "password": "password123!"
}

Response: 201 Created
{
  "status": "OK",
  "code": "AUTH_2001",
  "message": "회원가입이 완료되었습니다.",
  "data": {
    "memberId": 1
  }
}
```

2. **FCM 토큰 등록**
```json
POST /notifications/token
{
  "memberId": 1,
  "fcmToken": "dSN96KjJ1zy...",
  "deviceType": "ANDROID"
}

Response: 200 OK
{
  "status": "OK", 
  "code": "NOTIFICATION_2001",
  "message": "FCM 토큰이 성공적으로 등록되었습니다.",
  "data": "SUCCESS"
}
```

3. **예약 생성**
```json
POST /appointments
{
  "memberId": 1,
  "hospitalName": "서울대병원",
  "department": "내과",
  "doctorName": "김의사",
  "appointmentDate": "2025-08-30",
  "appointmentTime": "10:30:00", 
  "roomNumber": "2번 진료실"
}

Response: 200 OK
{
  "status": "OK",
  "code": "APPOINTMENT_2001", 
  "message": "예약이 성공적으로 생성되었습니다.",
  "data": 15
}
```

4. **환자 체크인**
```json
PUT /appointments/checkin
{
  "appointmentId": 15,
  "memberId": 1
}

Response: 200 OK
{
  "status": "OK",
  "code": "APPOINTMENT_2002",
  "message": "체크인이 완료되었습니다.",
  "data": "SUCCESS"
}
```

5. **환자 호출**
```json
POST /notifications/call
{
  "appointmentId": 15,
  "roomNumber": "2번 진료실"
}

Response: 200 OK
{
  "status": "OK",
  "code": "NOTIFICATION_2004",
  "message": "환자 호출이 완료되었습니다.",
  "data": "SUCCESS"
}
```

## 🔍 디버깅 가이드

### 서버 로그 확인
```bash
# 예약 관련 로그
grep "Appointment" logs/application.log

# 알림 관련 로그  
grep "FCM\|notification" logs/application.log

# 에러 로그
grep "ERROR" logs/application.log
```

### 데이터베이스 직접 확인
```sql
-- 회원 정보
SELECT * FROM member WHERE id = 1;

-- 예약 정보
SELECT * FROM appointment WHERE member_id = 1;

-- FCM 토큰
SELECT * FROM device_token WHERE member_id = 1;

-- 알림 이력
SELECT * FROM notification_history ORDER BY created_at DESC LIMIT 10;
```

---

API 사용 중 문제가 발생하면 서버 로그를 확인하거나 [이슈 탭](https://github.com/your-repo/issues)에 문의해주세요.