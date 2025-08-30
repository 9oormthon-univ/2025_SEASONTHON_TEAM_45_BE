# CareFreePass - 병원 환자 호출 시스템

병원에서 환자를 효율적으로 관리하고 호출할 수 있는 FCM 기반 푸시 알림 시스템입니다.

## 🚀 프로젝트 개요

CareFreePass는 병원 관리자가 웹에서 환자를 호출하면, 환자의 모바일 앱으로 실시간 푸시 알림을 전송하는 시스템입니다.

### 주요 기능

- **환자 예약 관리**: 예약 생성, 수정, 삭제, 조회
- **실시간 알림**: FCM을 통한 환자 호출 알림
- **상태 관리**: 예약 → 도착 → 호출 → 완료 단계별 관리
- **관리자 대시보드**: 웹 기반 환자 관리 인터페이스

## 🏗️ 시스템 아키텍처

### 도메인 분리 구조
```
📦 CareFreePass
├── 🏥 appointment (예약 도메인)
│   ├── 예약 생성/수정/삭제
│   ├── 예약 상태 관리
│   └── 환자 호출 로직
│
├── 🔔 notification (알림 도메인)  
│   ├── FCM 토큰 관리
│   ├── 푸시 알림 전송
│   └── 알림 이력 관리
│
└── 👤 member (회원 도메인)
    ├── 환자 회원가입/로그인
    └── 회원 정보 관리
```

### 기술 스택
- **Backend**: Spring Boot 3.3.5, Java 21
- **Database**: MariaDB with Docker Compose
- **Push Notification**: Firebase Cloud Messaging (FCM)
- **Architecture**: Domain-Driven Design (DDD)
- **Authentication**: JWT
- **ORM**: JPA/Hibernate

## 📋 API 엔드포인트

### 예약 관리 API (`/api/v1/appointments`)
- `POST /` - 예약 생성
- `GET /today` - 오늘 전체 예약 조회
- `GET /today/waiting` - 오늘 대기 환자 조회
- `PUT /{id}` - 예약 수정
- `DELETE /{id}` - 예약 삭제
- `PUT /checkin` - 환자 체크인

### 알림 관리 API (`/api/v1/notifications`)
- `POST /token` - FCM 토큰 등록
- `POST /call` - 환자 호출 (핵심 기능)

### 인증 API (`/api/v1/auth`)
- `POST /patient/sign-up` - 환자 회원가입
- `POST /patient/sign-in` - 환자 로그인

## 🔧 설정 및 실행

### 1. 환경 설정
[Firebase 설정 가이드](./FIREBASE_SETUP.md) 참조

### 2. 서버 실행
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 3. 테스트 페이지 접속
```
http://localhost:8080/test-fcm.html
```

## 📖 상세 문서

- [Firebase 설정 가이드](./FIREBASE_SETUP.md) - Firebase 프로젝트 설정 방법
- [예약 시스템 가이드](./APPOINTMENT_GUIDE.md) - 예약 관리 상세 사용법
- [알림 시스템 가이드](./NOTIFICATION_GUIDE.md) - FCM 알림 시스템 사용법
- [API 문서](./API_DOCUMENTATION.md) - 전체 API 명세서

## 🎯 핵심 워크플로우

1. **환자 등록**: 앱에서 회원가입 → FCM 토큰 등록
2. **예약 생성**: 웹 관리자가 환자 예약 생성
3. **환자 도착**: 환자가 병원 도착 → 체크인
4. **환자 호출**: 관리자가 호출 버튼 클릭 → FCM 푸시 알림 전송
5. **진료 완료**: 상태를 완료로 변경

## 📱 테스트 방법

1. 웹 페이지에서 회원가입
2. FCM 토큰 생성 및 등록
3. 예약 생성
4. 호출 버튼 클릭
5. 브라우저에서 푸시 알림 확인

## 🔒 보안 고려사항

- JWT 기반 인증
- FCM 토큰 보안 관리
- 환경변수를 통한 민감 정보 관리
- HTTPS 통신 권장

## 🤝 기여 방법

1. Fork the Project
2. Create Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit Changes (`git commit -m 'feat: 새로운 기능 추가'`)
4. Push to Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다.

---

**개발팀**: 2025 SEASONTHON TEAM 45  
**개발기간**: 2025년 동계 시즌톤  
**문의**: 프로젝트 이슈 탭 활용