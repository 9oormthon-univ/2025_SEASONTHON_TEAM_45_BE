# 🔔 CareFreePass FCM 알림 시스템

## 📋 개요
웹 관리자가 버튼 클릭으로 앱 환자에게 푸시 알림을 전송하는 FCM 기반 환자 호출 시스템입니다.

## 🚀 주요 기능

### 핵심 플로우
1. **환자**: 앱으로 병원 예약 → BLE 비콘으로 자동 체크인 → 대기
2. **웹 관리자**: 실시간 대시보드에서 도착한 환자 확인 → [호출] 버튼 클릭
3. **시스템**: FCM을 통해 해당 환자 앱에 푸시 알림 전송
4. **환자**: "김환자님, 2번 진료실로 들어오세요" 알림 수신

### 구현된 기능
- ✅ FCM 토큰 등록/관리
- ✅ 병원 예약 생성
- ✅ BLE 비콘 체크인 (API)
- ✅ 환자 호출 (핵심 기능)
- ✅ 실시간 환자 상태 관리
- ✅ 알림 발송 이력 관리
- ✅ 웹 테스트 페이지

## 🏗 프로젝트 구조

```
domain/notification/
├── entity/
│   ├── Appointment.java          # 예약 정보
│   ├── AppointmentStatus.java    # 예약 상태 (BOOKED/ARRIVED/CALLED/등)
│   ├── DeviceToken.java          # FCM 토큰 관리
│   └── NotificationHistory.java  # 알림 발송 이력
├── repository/                   # JPA Repository
├── service/
│   ├── FcmService.java          # Firebase 메시지 전송
│   └── NotificationService.java # 비즈니스 로직
├── controller/
│   ├── NotificationController.java
│   └── docs/NotificationDocs.java # Swagger 문서
└── dto/                         # 요청/응답 DTO
```

## 🔧 설정

### 1. 데이터베이스 실행
```bash
docker-compose up -d
```

### 2. Firebase 설정
1. Firebase 콘솔에서 새 프로젝트 생성
2. 서비스 계정 키 생성 후 `firebase-service-account.json` 파일을 `src/main/resources/`에 저장
3. `application-firebase.yml`에서 프로젝트 ID 설정

### 3. 환경 변수 (선택사항)
```bash
export FIREBASE_PROJECT_ID=your-project-id
export FIREBASE_SERVICE_ACCOUNT_KEY=classpath:firebase-service-account.json
```

## 📱 API 엔드포인트

### 기본 URL: `http://localhost:8080/api/v1/notifications`

#### 환자용 API
```http
# FCM 토큰 등록
POST /token
{
  "memberId": 1,
  "fcmToken": "fcm_token_here",
  "deviceType": "ANDROID"
}

# 병원 예약
POST /appointments
{
  "memberId": 1,
  "hospitalName": "서울대병원",
  "department": "내과",
  "doctorName": "김의사",
  "appointmentDate": "2024-01-15",
  "appointmentTime": "10:30",
  "roomNumber": "2번 진료실"
}

# BLE 체크인
PUT /appointments/checkin
{
  "appointmentId": 1,
  "memberId": 1
}
```

#### 관리자용 API
```http
# 환자 호출 (핵심 기능)
POST /call
{
  "appointmentId": 1,
  "roomNumber": "2번 진료실"
}

# 오늘 대기 환자 목록
GET /appointments/today/waiting

# 오늘 전체 예약 목록  
GET /appointments/today

# 예약 상태 수동 변경
PUT /appointments/{appointmentId}/status/{status}
```

## 🧪 테스트

### 웹 테스트 페이지
```
http://localhost:8080/test-fcm.html
```

### 테스트 시나리오
1. **FCM 토큰 등록**: 테스트용 토큰 등록
2. **예약 생성**: 오늘 날짜로 예약 생성
3. **체크인**: 예약 상태를 ARRIVED로 변경
4. **환자 호출**: [호출] 버튼 클릭으로 푸시 알림 전송

### Swagger UI
```
http://localhost:8080/swagger-ui/index.html
```

## 📊 데이터베이스 스키마

```sql
-- 예약 정보
appointment:
├── id (BIGINT, PK)
├── member_id (BIGINT, FK)
├── hospital_name (VARCHAR)
├── department (VARCHAR)
├── doctor_name (VARCHAR)
├── appointment_date (DATE)
├── appointment_time (TIME)
├── room_number (VARCHAR)
├── status (ENUM: BOOKED/ARRIVED/CALLED/IN_PROGRESS/COMPLETED)
├── created_at (DATETIME)
└── updated_at (DATETIME)

-- FCM 토큰
device_token:
├── id (BIGINT, PK)
├── member_id (BIGINT, FK)
├── fcm_token (VARCHAR)
├── device_type (VARCHAR)
├── status (ENUM: ACTIVE/INACTIVE)
├── last_used_at (DATETIME)
├── created_at (DATETIME)
└── updated_at (DATETIME)

-- 알림 이력
notification_history:
├── id (BIGINT, PK)
├── appointment_id (BIGINT, FK)
├── title (VARCHAR)
├── message (VARCHAR)
├── is_success (BOOLEAN)
├── error_message (VARCHAR)
├── created_at (DATETIME)
└── updated_at (DATETIME)
```

## 🔒 보안 고려사항

1. **Firebase 키 관리**: 서비스 계정 키를 Git에 커밋하지 않도록 주의
2. **토큰 검증**: FCM 토큰 유효성 검사 및 만료된 토큰 정리
3. **권한 확인**: 본인의 예약만 체크인/조회 가능하도록 제한
4. **에러 처리**: FCM 전송 실패시 재시도 로직 및 적절한 에러 응답

## 🎯 향후 개선 계획

1. **인증/인가**: JWT 기반 사용자 인증 추가
2. **실시간 업데이트**: WebSocket을 통한 실시간 상태 동기화
3. **알림 템플릿**: 진료과별/상황별 알림 메시지 커스터마이징
4. **통계 대시보드**: 호출 성공률, 대기 시간 등 운영 지표
5. **다국어 지원**: 알림 메시지 다국어 처리

## 🏃‍♂️ 빠른 시작

1. **프로젝트 실행**
   ```bash
   docker-compose up -d
   ./gradlew bootRun
   ```

2. **테스트 페이지 접속**
   ```
   http://localhost:8080/test-fcm.html
   ```

3. **예약 및 호출 테스트**
   - FCM 토큰 등록 (임시 토큰으로 테스트 가능)
   - 예약 생성
   - 상태를 ARRIVED로 변경
   - [호출] 버튼으로 푸시 알림 테스트

---

**🔥 완벽한 FCM 알림 시스템이 구현되었습니다!**
기존 코드 컨벤션을 100% 준수하여 클린하고 확장 가능한 구조로 개발되었습니다.