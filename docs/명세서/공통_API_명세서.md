# CareFreePass 공통 API 명세서 🔗

## 📋 개요

CareFreePass 병원 예약 시스템의 **공통 API 명세서**입니다.  
웹 관리자와 모바일 환자 앱에서 모두 사용하는 공통 API 엔드포인트를 포함합니다.

**Base URL**: `http://13.124.250.98:8080`  
**Swagger UI**: `http://13.124.250.98:8080/swagger-ui/index.html`  
**API Version**: `v1`

---

## 🔐 1. 토큰 관리 API

### 1.1 토큰 재발급 🔄
```http
POST /api/v1/auth/reissue
Content-Type: application/json
```

**📝 설명**: Access Token이 만료된 경우 Refresh Token으로 새 토큰 쌍을 발급받습니다. 웹과 모바일 앱 모두에서 사용됩니다.

**🔍 Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..." // 유효한 Refresh Token (필수)
}
```

**✅ Response (200 OK):**
```json
{
  "code": "AUTH_2003",
  "message": "토큰 재발급에 성공했습니다.",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",  // 새 Access Token (1시간 유효)
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...", // 새 Refresh Token (14일 유효)
    "memberId": 1,
    "memberName": "김환자",
    "role": "PATIENT"  // 또는 "HOSPITAL"
  }
}
```

**❌ Error Cases:**
- `400`: 잘못된 Refresh Token 형식
- `401`: 만료되거나 유효하지 않은 Refresh Token
- `404`: 토큰에 해당하는 사용자 없음

**💡 구현 가이드:**
```javascript
// JavaScript 예시 - 자동 토큰 재발급
async function apiRequest(url, options = {}) {
  const token = getAccessToken();
  
  let response = await fetch(url, {
    ...options,
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
      ...options.headers
    }
  });
  
  // 401 에러 시 토큰 재발급 시도
  if (response.status === 401) {
    const refreshToken = getRefreshToken();
    const refreshResponse = await fetch('/api/v1/auth/reissue', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken })
    });
    
    if (refreshResponse.ok) {
      const newTokens = await refreshResponse.json();
      saveTokens(newTokens.data.accessToken, newTokens.data.refreshToken);
      
      // 원래 요청 재시도
      response = await fetch(url, {
        ...options,
        headers: {
          'Authorization': `Bearer ${newTokens.data.accessToken}`,
          'Content-Type': 'application/json',
          ...options.headers
        }
      });
    }
  }
  
  return response;
}
```

---

## 🧪 2. 테스트 및 헬스체크 API

### 2.1 서버 상태 확인 💚
```http
GET /api/v1/test/health
```

**📝 설명**: 서버의 정상 작동 여부를 확인합니다. 모니터링 도구나 앱 시작 시 서버 연결 테스트용으로 사용됩니다.

**✅ Response (200 OK):**
```json
{
  "code": "TEST_2001",
  "message": "서버가 정상 작동 중입니다.",
  "data": "OK"
}
```

**📎 사용 사례:**
- **웹 관리자**: 대시보드 로딩 전 서버 상태 확인
- **모바일 앱**: 앱 시작시 서버 연결 테스트
- **모니터링**: 서버 헬스체크 엔드포인트

### 2.2 Echo 테스트 📡
```http
POST /api/v1/test/echo
Content-Type: application/json
```

**📝 설명**: 서버와의 통신 테스트를 위한 에코 API입니다. 전송한 메시지를 그대로 반환합니다.

**🔍 Request Body:**
```json
"Hello CareFreePass!"  // 테스트 메시지 (String)
```

**✅ Response (200 OK):**
```json
{
  "code": "TEST_2002",
  "message": "메시지 수신 완료",
  "data": "Echo: Hello CareFreePass!"  // "Echo: " + 입력 메시지
}
```

**📎 사용 사례:**
- **개발/테스트**: API 통신 연결 확인
- **네트워크 진단**: 요청/응답 지연시간 측정
- **디버깅**: 서버 응답 형식 확인

---

## 🔧 3. 예약 정보 수정 API (공통)

### 3.1 예약 정보 수정 ✏️
```http
PUT /api/v1/appointments/{appointmentId}
Content-Type: application/json
Authorization: Bearer <token>
```

**📝 설명**: 기존 예약의 진료과, 날짜, 시간을 수정합니다. 환자와 관리자 모두 사용 가능합니다.

**🔗 Path Parameters:**
- `appointmentId`: 수정할 예약 ID (Long)

**🔍 Request Body:**
```json
{
  "departmentName": "정형외과",      // 변경할 진료과명 (선택)
  "appointmentDate": "2025-09-04",  // 변경할 날짜 YYYY-MM-DD (선택)
  "appointmentTime": "15:00"        // 변경할 시간 HH:MM (선택)
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
- `403`: 권한 없음 (환자는 본인 예약만 수정 가능)

**📎 사용 주체별 차이점:**

#### 🖥️ 웹 관리자
```javascript
// 관리자는 모든 예약 수정 가능
PUT /api/v1/appointments/123
Authorization: Bearer <hospital_admin_token>

{
  "departmentName": "외과",
  "appointmentDate": "2025-09-05", 
  "appointmentTime": "16:00"
}
```

#### 📱 모바일 환자
```javascript
// 환자는 본인 예약만 수정 가능 (memberId 검증)
PUT /api/v1/appointments/123
Authorization: Bearer <patient_token>

{
  "departmentName": "내과",
  "appointmentDate": "2025-09-04",
  "appointmentTime": "14:00"
}
```

---

## 📊 4. 공통 에러 응답 규격

### 4.1 표준 에러 응답 형식 ❌
```json
{
  "code": "ERROR_CODE",     // 에러 코드 (String)
  "message": "에러 메시지", // 사용자 친화적 메시지 (String)
  "data": null              // 에러 시에는 null
}
```

### 4.2 주요 공통 에러 코드 📋

#### 인증 관련 에러
- `AUTH_4001`: "유효하지 않은 토큰입니다"
- `AUTH_4002`: "토큰이 만료되었습니다"
- `AUTH_4003`: "권한이 없습니다"
- `AUTH_4004`: "로그인이 필요합니다"

#### 요청 관련 에러
- `REQUEST_4001`: "잘못된 요청 형식입니다"
- `REQUEST_4002`: "필수 필드가 누락되었습니다"
- `REQUEST_4003`: "유효성 검사에 실패했습니다"

#### 리소스 관련 에러
- `RESOURCE_4001`: "존재하지 않는 리소스입니다"
- `RESOURCE_4002`: "이미 존재하는 리소스입니다"
- `RESOURCE_4003`: "리소스 접근 권한이 없습니다"

#### 서버 관련 에러
- `SERVER_5001`: "서버 내부 오류가 발생했습니다"
- `SERVER_5002`: "데이터베이스 연결 오류입니다"
- `SERVER_5003`: "외부 서비스 연결 실패입니다"

### 4.3 에러 처리 가이드 🛠️

#### 웹 (JavaScript)
```javascript
async function handleApiResponse(response) {
  if (!response.ok) {
    const errorData = await response.json();
    
    switch (errorData.code) {
      case 'AUTH_4002':
        // 토큰 재발급 시도
        await refreshToken();
        break;
      case 'AUTH_4004':
        // 로그인 페이지로 이동
        window.location.href = '/login';
        break;
      default:
        // 사용자에게 에러 메시지 표시
        showErrorMessage(errorData.message);
    }
    
    throw new Error(errorData.message);
  }
  
  return response.json();
}
```

#### 모바일 (Swift)
```swift
func handleApiError(_ errorCode: String, _ message: String) {
    switch errorCode {
    case "AUTH_4002":
        // 토큰 재발급 시도
        refreshToken()
    case "AUTH_4004":
        // 로그인 화면으로 이동
        navigateToLogin()
    default:
        // 에러 알림 표시
        showAlert(title: "오류", message: message)
    }
}
```

---

## 🔒 5. 보안 및 인증 공통 가이드

### 5.1 JWT 토큰 구조 📝
```
Header: {
  "alg": "HS256",
  "typ": "JWT"
}

Payload: {
  "sub": "1",                    // 사용자 ID
  "role": "PATIENT",             // 역할 (PATIENT/HOSPITAL)
  "name": "김환자",              // 사용자 이름
  "iat": 1693660800,            // 발급 시간
  "exp": 1693664400             // 만료 시간 (1시간 후)
}
```

### 5.2 토큰 저장 방식 권장사항 💾

#### 🖥️ 웹 (브라우저)
```javascript
// localStorage 사용 (HTTPS 환경에서만)
localStorage.setItem('accessToken', token.accessToken);
localStorage.setItem('refreshToken', token.refreshToken);

// 또는 HttpOnly 쿠키 사용 (더 보안적)
// Set-Cookie: refreshToken=abc123; HttpOnly; Secure; SameSite=Strict
```

#### 📱 모바일 앱
```swift
// iOS: Keychain Services 사용
import Security

func saveToken(_ token: String, forKey key: String) {
    let data = token.data(using: .utf8)!
    let query = [
        kSecClass: kSecClassGenericPassword,
        kSecAttrAccount: key,
        kSecValueData: data
    ] as CFDictionary
    
    SecItemAdd(query, nil)
}
```

```kotlin
// Android: EncryptedSharedPreferences 사용
val sharedPreferences = EncryptedSharedPreferences.create(
    "secure_prefs",
    masterKey,
    context,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)

sharedPreferences.edit()
    .putString("access_token", accessToken)
    .apply()
```

### 5.3 HTTPS 통신 필수 🔐
```javascript
// 모든 API 호출 시 HTTPS 사용
const API_BASE_URL = 'https://13.124.250.98:8080'; // HTTP 금지

// 개발 환경에서도 가급적 HTTPS 사용
// 운영 환경에서는 반드시 HTTPS 필수
```

---

## 📱 6. 공통 HTTP 상태 코드

### 6.1 성공 응답 ✅
- `200 OK`: 요청 성공
- `201 Created`: 리소스 생성 성공
- `204 No Content`: 성공했지만 반환할 데이터 없음

### 6.2 클라이언트 에러 ❌
- `400 Bad Request`: 잘못된 요청
- `401 Unauthorized`: 인증 필요
- `403 Forbidden`: 권한 없음
- `404 Not Found`: 리소스 없음
- `409 Conflict`: 리소스 충돌 (중복 등)

### 6.3 서버 에러 💥
- `500 Internal Server Error`: 서버 내부 오류
- `502 Bad Gateway`: 게이트웨이 오류
- `503 Service Unavailable`: 서비스 이용 불가

---

## 🔄 7. API 호출 패턴 예시

### 7.1 기본 호출 패턴
```javascript
// 공통 API 호출 함수
async function callApi(endpoint, options = {}) {
  const baseUrl = 'https://13.124.250.98:8080/api/v1';
  const accessToken = getAccessToken();
  
  try {
    const response = await fetch(`${baseUrl}${endpoint}`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json',
        ...options.headers
      },
      ...options
    });
    
    if (!response.ok) {
      await handleApiError(response);
    }
    
    return await response.json();
  } catch (error) {
    console.error('API 호출 실패:', error);
    throw error;
  }
}

// 사용 예시
const healthStatus = await callApi('/test/health');
const echoResult = await callApi('/test/echo', {
  method: 'POST',
  body: JSON.stringify('Hello Server!')
});
```

---

## 📞 문의 및 지원

- **개발팀**: development@carefreepass.com
- **기술 지원**: support@carefreepass.com
- **GitHub Issues**: https://github.com/9oormthon-univ/2025_SEASONTHON_TEAM_45_BE/issues

---

**최종 업데이트**: 2025년 9월 2일  
**버전**: 1.0.0  
**대상**: 웹/모바일 공통 개발자