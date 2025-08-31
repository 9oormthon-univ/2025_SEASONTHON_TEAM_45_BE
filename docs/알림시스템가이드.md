# 알림 시스템 가이드

CareFreePass의 FCM 기반 푸시 알림 시스템 사용법을 상세히 안내합니다.

## 🔔 알림 시스템 개요

Firebase Cloud Messaging(FCM)을 사용하여 웹 관리자가 환자의 모바일 앱으로 실시간 푸시 알림을 전송하는 시스템입니다.

### 지원하는 알림 타입
- **환자 호출 알림**: 진료실로 호출하는 알림 (핵심 기능)
- **예약 확인 알림**: 예약 생성 시 자동 발송되는 확인 알림

### 알림 흐름
```
웹 관리자 호출 → FCM 서버 → 환자 앱 → 푸시 알림 표시
```

## 📱 FCM 토큰 관리

### 1. FCM 토큰 등록

#### API 요청
```http
POST /api/v1/notifications/token
Content-Type: application/json

{
  "memberId": 1,
  "fcmToken": "dSN96KjJ1zyWVGoDdnf2yp:APA91bFvJEKyLryZ0kJ1XXJN8wpQDrKV-tUM5oHvQbeVTa0JJ_9wMqnCkDZUH_mOOwvgZPx4OhqNt5HChK96IHUkdgQgEepJquGIpgyUInDtlB42cGw8ehE",
  "deviceType": "ANDROID"
}
```

#### 요청 파라미터
| 필드 | 타입 | 필수 | 설명 |
|-----|------|------|------|
| memberId | Long | ✅ | 환자 회원 ID |
| fcmToken | String | ✅ | 앱에서 생성한 FCM 토큰 |
| deviceType | String | ✅ | 기기 타입 (ANDROID/IOS) |

#### 응답 예시
```json
{
  "status": "OK",
  "code": "NOTIFICATION_2001",
  "message": "FCM 토큰이 성공적으로 등록되었습니다.",
  "data": "SUCCESS"
}
```

### 2. 토큰 관리 규칙
- 한 환자당 하나의 활성 토큰만 유지
- 새 토큰 등록 시 기존 토큰은 비활성화
- 토큰 등록 시 마지막 사용 시간 업데이트

## 📢 환자 호출 알림

### 1. API 요청
```http
POST /api/v1/notifications/call
Content-Type: application/json

{
  "appointmentId": 15,
  "roomNumber": "2번 진료실"
}
```

### 2. 요청 파라미터
| 필드 | 타입 | 필수 | 설명 |
|-----|------|------|------|
| appointmentId | Long | ✅ | 예약 ID |
| roomNumber | String | ❌ | 진료실 번호 (없으면 예약의 진료실 사용) |

### 3. 호출 프로세스
1. **예약 정보 조회**: 예약 ID로 환자 정보 확인
2. **호출 가능 여부 확인**: 완료/취소 상태가 아닌지 검증
3. **FCM 토큰 조회**: 환자의 활성 토큰 확인
4. **FCM 알림 전송**: Firebase 서버로 푸시 알림 요청
5. **상태 업데이트**: 성공 시 예약 상태를 `CALLED`로 변경
6. **이력 저장**: 알림 전송 결과를 NotificationHistory에 저장

### 4. 알림 메시지 형식
```json
{
  "title": "진료 호출",
  "body": "김환자님, 2번 진료실로 들어오세요.",
  "data": {
    "appointmentId": "15",
    "roomNumber": "2번 진료실",
    "type": "PATIENT_CALL"
  }
}
```

### 5. 응답 예시
```json
{
  "status": "OK", 
  "code": "NOTIFICATION_2004",
  "message": "환자 호출이 완료되었습니다.",
  "data": "SUCCESS"
}
```

## 📋 예약 확인 알림

### 1. 자동 발송 조건
- 예약 생성 시 자동으로 발송
- 환자에게 FCM 토큰이 등록되어 있는 경우에만 발송

### 2. 알림 메시지 형식
```json
{
  "title": "예약 확인",
  "body": "서울대병원 내과 2025-08-30 10:30 예약이 확정되었습니다.",
  "data": {
    "hospitalName": "서울대병원",
    "department": "내과", 
    "appointmentDate": "2025-08-30",
    "appointmentTime": "10:30",
    "type": "APPOINTMENT_CONFIRMATION"
  }
}
```

### 3. 발송 로직
```java
// 예약 생성 후 자동 호출
notificationService.sendAppointmentConfirmation(
    memberId, 
    memberName, 
    hospitalName, 
    appointmentDate.toString(), 
    appointmentTime.toString()
);
```

## 🖥️ 웹 테스트 페이지 사용법

### 1. 테스트 페이지 접속
```
http://localhost:8080/test-fcm.html
```

### 2. FCM 토큰 생성 및 등록
1. **브라우저 권한 허용**: 알림 권한 요청 시 "허용" 클릭
2. **토큰 생성**: "실제 FCM 토큰 생성하기" 버튼 클릭
3. **자동 등록**: 생성된 토큰이 자동으로 회원 ID 1에 등록됨
4. **토큰 확인**: 생성된 토큰이 화면에 표시됨

### 3. 알림 테스트 과정
1. **회원가입**: 테스트용 환자 계정 생성
2. **FCM 토큰 등록**: 위 과정으로 토큰 생성/등록
3. **예약 생성**: 같은 회원 ID로 예약 생성
4. **환자 호출**: 예약 목록에서 📢 호출 버튼 클릭
5. **알림 확인**: 브라우저 우상단에 푸시 알림 표시

### 4. 알림 수신 확인 방법
- **브라우저 알림**: 브라우저 우상단에 네이티브 알림
- **페이지 내 알림**: 페이지 우상단에 녹색 알림 박스 표시
- **콘솔 로그**: 개발자 도구 콘솔에서 메시지 확인

## 🔧 Service Worker 설정

### 1. firebase-messaging-sw.js
브라우저에서 백그라운드 메시지를 수신하기 위한 서비스 워커:

```javascript
import { initializeApp } from 'firebase/app';
import { getMessaging, onBackgroundMessage } from 'firebase/messaging/sw';

const firebaseConfig = {
    apiKey: "AIzaSyC-mtXiQdWICXo9OhKwVx01qxXqbAoCAj8",
    authDomain: "hackerton-fcm.firebaseapp.com",
    projectId: "hackerton-fcm", 
    storageBucket: "hackerton-fcm.firebasestorage.app",
    messagingSenderId: "67081294208",
    appId: "1:67081294208:android:01b8489bb9f2008d6f7165"
};

const app = initializeApp(firebaseConfig);
const messaging = getMessaging(app);

onBackgroundMessage(messaging, (payload) => {
    console.log('[firebase-messaging-sw.js] Received background message:', payload);
    
    const notificationTitle = payload.notification.title;
    const notificationOptions = {
        body: payload.notification.body,
        icon: '/favicon.ico'
    };

    self.registration.showNotification(notificationTitle, notificationOptions);
});
```

### 2. 포그라운드 메시지 처리
페이지가 활성화된 상태에서 메시지 수신:

```javascript
import { onMessage } from 'firebase/messaging';

onMessage(messaging, (payload) => {
    console.log('FCM 메시지 수신:', payload);
    
    // 브라우저 알림 표시
    if (payload.notification) {
        new Notification(payload.notification.title, {
            body: payload.notification.body,
            icon: '/favicon.ico'
        });
    }
    
    // 페이지 내 알림 표시
    showPageNotification(payload.notification);
});
```

## 📊 알림 이력 관리

### 1. NotificationHistory 엔티티
모든 알림 전송 결과를 데이터베이스에 저장:

```sql
CREATE TABLE notification_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    appointment_id BIGINT,
    title VARCHAR(200) NOT NULL,
    message VARCHAR(500) NOT NULL,
    is_success BOOLEAN NOT NULL,
    error_message VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 2. 이력 조회
특정 예약의 알림 이력 조회:
```java
List<NotificationHistory> history = notificationService.getNotificationHistory(appointmentId);
```

## 🚨 에러 처리

### 일반적인 에러 코드
| 코드 | 메시지 | 해결방법 |
|-----|--------|----------|
| NOTIFICATION_4001 | 존재하지 않는 회원입니다 | 올바른 회원 ID 확인 |
| NOTIFICATION_4004 | 호출할 수 없는 예약 상태입니다 | 예약 상태 확인 (완료/취소 상태는 호출 불가) |
| NOTIFICATION_5004 | 환자의 FCM 토큰이 없습니다 | FCM 토큰 등록 먼저 수행 |
| NOTIFICATION_5005 | 푸시 알림 전송에 실패했습니다 | Firebase 설정 및 네트워크 확인 |

### FCM 관련 에러
- **API Key not valid**: Firebase 설정의 API 키 확인
- **Service Worker 등록 실패**: HTTPS 환경 또는 localhost에서 테스트
- **토큰 생성 실패**: 브라우저 알림 권한 허용 확인

## 📱 모바일 앱 연동

### 1. Android FCM 토큰 발급
```kotlin
FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
    if (!task.isSuccessful) {
        Log.w(TAG, "Fetching FCM registration token failed", task.exception)
        return@addOnCompleteListener
    }

    val token = task.result
    Log.d(TAG, "FCM Registration Token: $token")
    
    // 서버로 토큰 전송
    sendTokenToServer(token)
}
```

### 2. 메시지 수신 처리
```kotlin
class MyFirebaseMessagingService : FirebaseMessagingService() {
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")
        
        // 알림 표시
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            showNotification(it.title, it.body)
        }
        
        // 데이터 페이로드 처리
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            handleDataPayload(remoteMessage.data)
        }
    }
    
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        sendTokenToServer(token)
    }
}
```

## 🔐 보안 고려사항

### 1. FCM 토큰 보안
- 토큰은 개인정보로 간주하여 안전하게 저장
- 토큰 만료 시 자동 갱신 처리
- 앱 삭제 시 서버에서 토큰 비활성화

### 2. 알림 권한 관리
- 사용자 동의 하에만 알림 전송
- 알림 거부 시 graceful 처리
- 개인정보 보호 정책 준수

### 3. Firebase 보안 규칙
```javascript
// Firebase Firestore 보안 규칙 예시
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /notifications/{document} {
      allow read, write: if request.auth != null;
    }
  }
}
```

## 📈 모니터링 및 분석

### 1. FCM 전송 통계
Firebase Console에서 확인 가능한 메트릭:
- 메시지 전송 수
- 전송 성공률  
- 기기별 수신률
- 국가별 통계

### 2. 애플리케이션 로그
서버에서 확인 가능한 로그:
```bash
# 성공 로그
INFO o.c.c.c.d.n.service.NotificationService : Patient call sent successfully: 김환자 (Appointment ID: 15)

# 실패 로그  
ERROR o.c.c.c.d.n.service.NotificationService : Failed to send patient call: 김환자 (Appointment ID: 15)
```

### 3. 알림 이력 분석
데이터베이스 쿼리로 알림 전송 성공률 분석:
```sql
SELECT 
    COUNT(*) as total_notifications,
    SUM(CASE WHEN is_success = true THEN 1 ELSE 0 END) as successful_notifications,
    (SUM(CASE WHEN is_success = true THEN 1 ELSE 0 END) * 100.0 / COUNT(*)) as success_rate
FROM notification_history 
WHERE created_at >= CURDATE();
```

---

알림 시스템 사용 중 문제가 발생하면 [Firebase 설정 가이드](./FIREBASE_SETUP.md)를 참조하거나 Firebase Console에서 전송 상태를 확인해주세요.