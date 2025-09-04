# CareFreePass FCM 푸시 알림 연동 가이드 📱

## 📋 개요

CareFreePass 병원 예약 시스템에서 **FCM(Firebase Cloud Messaging)**을 활용한 푸시 알림 연동 방법을 설명합니다.  
웹(관리자)과 모바일 앱(환자)에서 실시간 알림을 주고받을 수 있습니다.

**서버 URL**: `http://13.124.250.98:8080`

---

## 🔔 알림 시나리오

### 📱 환자가 받는 알림
1. **예약 30분 전**: "진료 시간이 다가왔습니다. 병원으로 출발하세요"
2. **체크인 완료**: "체크인이 완료되었습니다. 잠시 대기해주세요"
3. **진료실 호출**: "진료실로 들어오세요"

### 🖥️ 관리자가 받는 알림
1. **환자 체크인**: "김환자님이 체크인했습니다"
2. **새 예약 생성**: "새로운 예약이 생성되었습니다"

---

## 🚀 1. 초기 설정

### 1.1 Firebase 프로젝트 설정
```javascript
// Firebase 초기화 (Web)
import { initializeApp } from 'firebase/app';
import { getMessaging, getToken } from 'firebase/messaging';

const firebaseConfig = {
  // Firebase 콘솔에서 복사한 설정
  apiKey: "your-api-key",
  authDomain: "your-project.firebaseapp.com",
  projectId: "your-project-id",
  messagingSenderId: "123456789",
  appId: "your-app-id"
};

const app = initializeApp(firebaseConfig);
const messaging = getMessaging(app);
```

### 1.2 iOS 설정
```swift
// AppDelegate.swift
import FirebaseCore
import FirebaseMessaging

@main
class AppDelegate: UIResponder, UIApplicationDelegate {
    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        FirebaseApp.configure()
        
        // FCM 델리게이트 설정
        Messaging.messaging().delegate = self
        UNUserNotificationCenter.current().delegate = self
        
        // 알림 권한 요청
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { granted, error in
            print("알림 권한: \(granted)")
        }
        
        application.registerForRemoteNotifications()
        return true
    }
}
```

### 1.3 Android 설정
```kotlin
// MainActivity.kt
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // FCM 토큰 가져오기
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "FCM 토큰 가져오기 실패", task.exception)
                return@addOnCompleteListener
            }
            
            val token = task.result
            Log.d(TAG, "FCM Token: $token")
            
            // 서버에 토큰 등록
            registerTokenToServer(token)
        }
    }
}
```

---

## 📤 2. FCM 토큰 등록

### 2.1 토큰 가져오기

#### 🌐 웹 (JavaScript)
```javascript
import { getToken } from 'firebase/messaging';

async function getFCMToken() {
    try {
        const token = await getToken(messaging, {
            vapidKey: 'your-vapid-key'  // Firebase 콘솔에서 생성
        });
        
        if (token) {
            console.log('FCM Token:', token);
            await registerTokenToServer(token, 'WEB');
        } else {
            console.log('FCM 토큰을 가져올 수 없습니다.');
        }
    } catch (error) {
        console.error('FCM 토큰 가져오기 실패:', error);
    }
}
```

#### 📱 iOS (Swift)
```swift
import FirebaseMessaging

extension AppDelegate: MessagingDelegate {
    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        guard let token = fcmToken else { return }
        print("FCM Token: \(token)")
        
        // 서버에 토큰 등록
        registerTokenToServer(token: token, deviceType: "IOS")
    }
}
```

#### 🤖 Android (Kotlin)
```kotlin
import com.google.firebase.messaging.FirebaseMessagingService

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        Log.d(TAG, "새로운 FCM 토큰: $token")
        
        // 서버에 토큰 등록
        registerTokenToServer(token, "ANDROID")
    }
}
```

### 2.2 서버에 토큰 등록

#### API 호출
```http
POST /api/v1/notifications/token
Content-Type: application/json
```

**Request Body:**
```json
{
  "memberId": 1,
  "fcmToken": "dQGfH7VkS0uE8n4k...",
  "deviceType": "ANDROID"  // "WEB", "IOS", "ANDROID"
}
```

**Response:**
```json
{
  "data": "SUCCESS"
}
```

#### 📍 구현 예시

**JavaScript (웹/React):**
```javascript
async function registerTokenToServer(fcmToken, deviceType) {
    try {
        const response = await fetch('/api/v1/notifications/token', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${accessToken}`
            },
            body: JSON.stringify({
                memberId: getCurrentUserId(),
                fcmToken: fcmToken,
                deviceType: deviceType
            })
        });
        
        if (response.ok) {
            console.log('FCM 토큰 등록 성공');
        }
    } catch (error) {
        console.error('FCM 토큰 등록 실패:', error);
    }
}
```

**Swift (iOS):**
```swift
func registerTokenToServer(token: String, deviceType: String) {
    let url = URL(string: "http://13.124.250.98:8080/api/v1/notifications/token")!
    var request = URLRequest(url: url)
    request.httpMethod = "POST"
    request.setValue("application/json", forHTTPHeaderField: "Content-Type")
    request.setValue("Bearer \(accessToken)", forHTTPHeaderField: "Authorization")
    
    let body = [
        "memberId": currentUserId,
        "fcmToken": token,
        "deviceType": deviceType
    ]
    
    request.httpBody = try? JSONSerialization.data(withJSONObject: body)
    
    URLSession.shared.dataTask(with: request) { data, response, error in
        if let httpResponse = response as? HTTPURLResponse,
           httpResponse.statusCode == 200 {
            print("FCM 토큰 등록 성공")
        }
    }.resume()
}
```

**Kotlin (Android):**
```kotlin
fun registerTokenToServer(token: String, deviceType: String) {
    val retrofit = Retrofit.Builder()
        .baseUrl("http://13.124.250.98:8080/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val api = retrofit.create(NotificationApi::class.java)
    
    val request = DeviceTokenRequest(
        memberId = getCurrentUserId(),
        fcmToken = token,
        deviceType = deviceType
    )
    
    api.registerToken(request).enqueue(object : Callback<ApiResponse<String>> {
        override fun onResponse(call: Call<ApiResponse<String>>, response: Response<ApiResponse<String>>) {
            if (response.isSuccessful) {
                Log.d(TAG, "FCM 토큰 등록 성공")
            }
        }
        
        override fun onFailure(call: Call<ApiResponse<String>>, t: Throwable) {
            Log.e(TAG, "FCM 토큰 등록 실패", t)
        }
    })
}
```

---

## 📨 3. 알림 수신 처리

### 3.1 포그라운드 알림 수신

#### 🌐 웹 (JavaScript)
```javascript
import { onMessage } from 'firebase/messaging';

// 포그라운드에서 메시지 수신
onMessage(messaging, (payload) => {
    console.log('포그라운드 메시지 수신:', payload);
    
    const { title, body, data } = payload.notification;
    
    // 브라우저 알림 표시
    if (Notification.permission === 'granted') {
        new Notification(title, {
            body: body,
            icon: '/icon-192x192.png',
            data: data
        });
    }
    
    // 앱 내 알림 UI 업데이트
    showInAppNotification(title, body, data);
});
```

#### 📱 iOS (Swift)
```swift
// 포그라운드 알림 처리
extension AppDelegate: UNUserNotificationCenterDelegate {
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                willPresent notification: UNNotification,
                                withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        
        let userInfo = notification.request.content.userInfo
        print("포그라운드 알림 수신:", userInfo)
        
        // 포그라운드에서도 알림 표시
        completionHandler([.banner, .badge, .sound])
    }
    
    // 알림 터치 시 처리
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                didReceive response: UNNotificationResponse,
                                withCompletionHandler completionHandler: @escaping () -> Void) {
        
        let userInfo = response.notification.request.content.userInfo
        print("알림 터치:", userInfo)
        
        // 알림에 따른 화면 이동
        handleNotificationTap(userInfo: userInfo)
        completionHandler()
    }
}
```

#### 🤖 Android (Kotlin)
```kotlin
class MyFirebaseMessagingService : FirebaseMessagingService() {
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "FCM 메시지 수신: ${remoteMessage.from}")
        
        // 알림 데이터 처리
        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "메시지 데이터: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }
        
        // 알림 내용 처리
        remoteMessage.notification?.let {
            Log.d(TAG, "알림 제목: ${it.title}")
            Log.d(TAG, "알림 내용: ${it.body}")
            
            showNotification(it.title, it.body, remoteMessage.data)
        }
    }
    
    private fun showNotification(title: String?, body: String?, data: Map<String, String>) {
        val intent = Intent(this, MainActivity::class.java).apply {
            data.forEach { (key, value) -> putExtra(key, value) }
        }
        
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        
        val notification = NotificationCompat.Builder(this, "default_channel")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
```

### 3.2 백그라운드 알림 처리

#### 🌐 웹 (Service Worker)
```javascript
// public/firebase-messaging-sw.js
importScripts('https://www.gstatic.com/firebasejs/9.0.0/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/9.0.0/firebase-messaging-compat.js');

firebase.initializeApp({
  // Firebase 설정
});

const messaging = firebase.messaging();

// 백그라운드 메시지 처리
messaging.onBackgroundMessage((payload) => {
  console.log('백그라운드 메시지 수신:', payload);
  
  const { title, body, icon } = payload.notification;
  
  self.registration.showNotification(title, {
    body: body,
    icon: icon || '/icon-192x192.png',
    data: payload.data
  });
});
```

---

## 🔔 4. 알림 종류별 처리

### 4.1 환자 호출 알림
```javascript
// 알림 데이터에 따른 처리 분기
function handleNotificationData(data) {
    switch (data.type) {
        case 'PATIENT_CALL':
            // 진료실 호출 알림
            showPatientCallAlert(data.appointmentId, data.roomNumber);
            break;
            
        case 'APPOINTMENT_REMINDER':
            // 예약 30분 전 알림
            showAppointmentReminder(data.appointmentId, data.appointmentTime);
            break;
            
        case 'CHECKIN_CONFIRMATION':
            // 체크인 완료 알림
            showCheckinConfirmation(data.appointmentId);
            break;
    }
}

function showPatientCallAlert(appointmentId, roomNumber) {
    // 모달이나 팝업으로 긴급 알림 표시
    const modal = document.createElement('div');
    modal.innerHTML = `
        <div class="urgent-notification">
            <h2>🔔 진료실 호출</h2>
            <p>${roomNumber}로 들어오세요</p>
            <button onclick="this.parentElement.parentElement.remove()">확인</button>
        </div>
    `;
    document.body.appendChild(modal);
    
    // 소리 재생
    const audio = new Audio('/notification-sound.mp3');
    audio.play();
}
```

### 4.2 알림 권한 요청
```javascript
// 알림 권한 요청 (웹)
async function requestNotificationPermission() {
    if ('Notification' in window) {
        const permission = await Notification.requestPermission();
        
        if (permission === 'granted') {
            console.log('알림 권한 허용됨');
            await getFCMToken();
        } else {
            console.log('알림 권한 거부됨');
            // 대체 알림 방식 제공 (예: 화면 내 알림)
        }
    }
}
```

---

## 🔧 5. 디버깅 및 테스트

### 5.1 토큰 확인
```javascript
// FCM 토큰 정상 등록 여부 확인
async function checkTokenRegistration() {
    try {
        const response = await fetch(`/api/v1/notifications/history?memberId=${memberId}`);
        const result = await response.json();
        
        console.log('등록된 알림 이력:', result.data);
    } catch (error) {
        console.error('토큰 확인 실패:', error);
    }
}
```

### 5.2 테스트 알림 전송
```javascript
// 개발자 도구에서 테스트용 알림 전송
async function sendTestNotification(appointmentId) {
    try {
        const response = await fetch('/api/v1/notifications/call', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${adminToken}`
            },
            body: JSON.stringify({
                appointmentId: appointmentId
            })
        });
        
        if (response.ok) {
            console.log('테스트 알림 전송 성공');
        }
    } catch (error) {
        console.error('테스트 알림 전송 실패:', error);
    }
}
```

---

## ⚠️ 주의사항

### 🔒 보안
- FCM 토큰은 민감한 정보이므로 HTTPS를 통해서만 전송
- 토큰이 변경될 수 있으므로 정기적으로 업데이트
- 앱 삭제/재설치 시 토큰 재등록 필요

### 🎯 사용자 경험
- 알림 권한은 적절한 시점에 요청 (로그인 후 등)
- 백그라운드/포그라운드 상태에 따른 다른 알림 처리
- 중요한 알림(환자 호출)은 소리와 함께 표시

### 📊 에러 처리
- 토큰 등록 실패 시 재시도 로직 구현
- 네트워크 오류 시 로컬 스토리지 활용
- 알림 수신 실패 시 대체 방안 제공

---

## 📞 문의 및 지원

- **개발팀**: development@carefreepass.com
- **Firebase 설정 문의**: firebase@carefreepass.com
- **GitHub Issues**: https://github.com/9oormthon-univ/2025_SEASONTHON_TEAM_45_BE/issues

---

**최종 업데이트**: 2025년 9월 2일  
**버전**: 1.0.0  
**대상**: 프론트엔드 개발자 (웹/모바일)