# Firebase 설정 가이드

CareFreePass 프로젝트에서 FCM(Firebase Cloud Messaging) 푸시 알림을 사용하기 위한 Firebase 설정 방법을 안내합니다.

## 🔥 Firebase 프로젝트 생성

### 1. Firebase Console 접속
1. [Firebase Console](https://console.firebase.google.com) 접속
2. "프로젝트 추가" 클릭
3. 프로젝트 이름 입력 (예: `hackerton-fcm`)
4. Google Analytics 설정 (선택사항)
5. 프로젝트 생성 완료

### 2. Android 앱 등록
1. 프로젝트 개요에서 "Android" 아이콘 클릭
2. Android 패키지 이름 입력: `com.carefreepass.carefreepass`
3. 앱 닉네임 입력: `CareFreePass`
4. SHA-1 디버그 서명 인증서 추가 (선택사항)
5. `google-services.json` 다운로드

### 3. 웹 앱 등록 (테스트용)
1. 프로젝트 개요에서 "웹" 아이콘 클릭
2. 앱 닉네임 입력: `CareFreePass Web`
3. 호스팅 설정하지 않음
4. Firebase SDK 설정 코드 복사 (나중에 사용)

## 🔑 Firebase 서비스 계정 설정

### 1. 서비스 계정 키 생성
1. Firebase Console → 프로젝트 설정 → 서비스 계정 탭
2. "새 비공개 키 생성" 클릭
3. JSON 파일 다운로드
4. 파일 이름을 `firebase-service-account.json`으로 변경

### 2. 서비스 계정 키 배치
```bash
# 프로젝트 루트에 배치
mv ~/Downloads/firebase-service-account.json ./src/main/resources/firebase-service-account.json
```

### 3. .gitignore에 추가
```bash
# Firebase 설정 파일
src/main/resources/firebase-service-account.json
src/main/resources/google-services.json
```

## 🌐 웹 푸시 인증서 (VAPID) 설정

### 1. VAPID 키 생성
1. Firebase Console → 프로젝트 설정 → 클라우드 메시징 탭
2. "웹 구성" 섹션에서 "키 페어 생성" 클릭
3. 생성된 키를 복사 (예: `BO0E5srhJZcO686JAgCl94NqeWmm8YRE0TSfHbukbeyosncpcMxgh9Td3OQZJ99joHOlTm2LC9CFPvbuK99xUNM`)

## ⚙️ 환경 변수 설정

### 1. .env 파일 생성
프로젝트 루트에 `.env` 파일 생성:

```bash
# Firebase 설정
FIREBASE_PROJECT_ID=hackerton-fcm
FIREBASE_API_KEY=AIzaSyC-mtXiQdWICXo9OhKwVx01qxXqbAoCAj8
FIREBASE_APP_ID=1:67081294208:android:01b8489bb9f2008d6f7165
FIREBASE_MESSAGING_SENDER_ID=67081294208
FIREBASE_VAPID_KEY=BO0E5srhJZcO686JAgCl94NqeWmm8YRE0TSfHbukbeyosncpcMxgh9Td3OQZJ99joHOlTm2LC9CFPvbuK99xUNM

# 데이터베이스 설정
DB_URL=jdbc:mariadb://localhost:3306/carefreepass
DB_USERNAME=carefreepass
DB_PASSWORD=your_password

# JWT 설정
JWT_ACCESS_TOKEN_EXPIRATION_TIME=3600000
JWT_REFRESH_TOKEN_EXPIRATION_TIME=86400000
JWT_SECRET_KEY=your-secret-key-here
```

### 2. google-services.json에서 값 추출
`google-services.json` 파일을 열어서 필요한 값들을 추출:

```json
{
  "project_info": {
    "project_id": "hackerton-fcm",  // FIREBASE_PROJECT_ID
    "project_number": "67081294208"  // FIREBASE_MESSAGING_SENDER_ID
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "1:67081294208:android:01b8489bb9f2008d6f7165"  // FIREBASE_APP_ID
      },
      "api_key": [
        {
          "current_key": "AIzaSyC-mtXiQdWICXo9OhKwVx01qxXqbAoCAj8"  // FIREBASE_API_KEY
        }
      ]
    }
  ]
}
```

## 🔧 애플리케이션 설정

### 1. Firebase 설정 클래스 확인
`FirebaseConfig.java`에서 설정이 올바른지 확인:

```java
@Configuration
public class FirebaseConfig {
    
    @PostConstruct
    public void initialize() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            InputStream serviceAccount = getClass()
                .getResourceAsStream("/firebase-service-account.json");
            
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();
                
            FirebaseApp.initializeApp(options);
        }
    }
}
```

### 2. 테스트 페이지 Firebase 설정
`test-fcm.html`의 Firebase 설정 확인:

```javascript
const firebaseConfig = {
    apiKey: "AIzaSyC-mtXiQdWICXo9OhKwVx01qxXqbAoCAj8",
    authDomain: "hackerton-fcm.firebaseapp.com", 
    projectId: "hackerton-fcm",
    storageBucket: "hackerton-fcm.firebasestorage.app",
    messagingSenderId: "67081294208",
    appId: "1:67081294208:android:01b8489bb9f2008d6f7165"
};
```

### 3. Service Worker 설정
`firebase-messaging-sw.js`에서 같은 설정 사용:

```javascript
import { initializeApp } from 'firebase/app';
import { getMessaging, onBackgroundMessage } from 'firebase/messaging/sw';

const firebaseConfig = {
    // 위와 동일한 설정
};

const app = initializeApp(firebaseConfig);
const messaging = getMessaging(app);
```

## ✅ 설정 검증

### 1. Firebase 연결 테스트
서버 시작 시 로그 확인:
```bash
INFO o.c.c.c.golbal.config.FirebaseConfig : Firebase application initialized successfully with project ID: hackerton-fcm
```

### 2. FCM 토큰 생성 테스트
1. 테스트 페이지 접속: `http://localhost:8080/test-fcm.html`
2. "실제 FCM 토큰 생성하기" 버튼 클릭
3. 알림 권한 허용
4. 토큰이 생성되는지 확인

### 3. 푸시 알림 테스트
1. FCM 토큰 등록
2. 회원가입 → 예약 생성
3. 호출 버튼 클릭
4. 브라우저에서 알림 수신 확인

## 🚨 문제 해결

### API Key not valid 에러
- `google-services.json`에서 올바른 API 키 확인
- 환경변수 `FIREBASE_API_KEY` 값 재확인

### Service Worker 등록 실패
- `firebase-messaging-sw.js` 파일 경로 확인
- MIME type 설정 확인 (application/javascript)

### VAPID Key 에러
- Firebase Console에서 웹 푸시 인증서 키 재생성
- 환경변수 `FIREBASE_VAPID_KEY` 값 업데이트

## 📱 모바일 앱 연동

### Android 앱 설정
1. `google-services.json`을 `app/` 디렉토리에 배치
2. `build.gradle` 설정:
```gradle
implementation 'com.google.firebase:firebase-messaging:23.0.0'
implementation 'com.google.firebase:firebase-analytics:21.0.0'
```

### FCM 토큰 발급
```kotlin
FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
    val token = task.result
    // 서버로 토큰 전송
}
```

## 🔐 보안 고려사항

1. **환경변수 사용**: 민감한 정보는 반드시 환경변수로 관리
2. **서비스 계정 키 보안**: JSON 파일을 Git에 커밋하지 말 것
3. **API 키 제한**: Firebase Console에서 API 키 사용 제한 설정
4. **HTTPS 사용**: 프로덕션에서는 반드시 HTTPS 사용

---

설정이 완료되면 [알림 시스템 가이드](./NOTIFICATION_GUIDE.md)를 참조하여 FCM 알림 기능을 테스트해보세요.