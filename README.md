# 🏥 CareFreePass Backend

<div align="center">

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-green?style=flat-square&logo=springboot)
![MariaDB](https://img.shields.io/badge/MariaDB-11.0-blue?style=flat-square&logo=mariadb)
![Redis](https://img.shields.io/badge/Redis-7.0-red?style=flat-square&logo=redis)
![Docker](https://img.shields.io/badge/Docker-containerized-blue?style=flat-square&logo=docker)
![AWS](https://img.shields.io/badge/AWS-deployed-orange?style=flat-square&logo=amazonaws)

**AI 기반 스마트 병원 예약 시스템**

증상 분석부터 예약 완료까지, 한 번에 해결하는 차세대 의료 서비스 플랫폼

</div>

---

## 📋 프로젝트 정보

| 항목 | 내용 |
|------|------|
| **프로젝트명** | CareFreePass Backend Server |
| **개발 기간** | 2024.08.31 ~ 2024.09.07 (7일) |
| **팀 구성** | Backend 2명, Frontend 2명, App 1명, Design 1명 |
| **프로젝트 유형** | 2025 시즌톤 참가작 |

### 👥 개발진
| 역할 | 이름 | GitHub | 담당 업무 |
|------|------|--------|-----------|
| **Backend Developer** | 김동균 | [@dongkyun0713](https://github.com/dongkyun0713) | 인증/인가, Nginx 설정 |
| **Backend Developer** | 문준원 | [@moonjun1](https://github.com/moonjun1) | 예약 시스템, 알람 개발 |
| **Frontend Developer** | 이혜연 | [@hyperon-hyeon](https://github.com/hyperon-hyeon) | 관리자 페이지 개발 |
| **Frontend Developer** | 경화 | [@kyunghwa] | 웹 디자인 |
| **App Developer** | 김호중 | [@jack9282](https://github.com/jack9282) | Flutter/Dart 앱 개발 |
| **Designer** | 박승원 | [@seungwon-park] | UI/UX 디자인 |

**김동균**: JWT 인증시스템, Spring Security, Nginx 설정, AWS 인프라 구축  
**문준원**: OpenAI GPT API 연동, 3단계 신뢰도 시스템, 예약 CRUD, 알람 시스템  
**이혜연**: 병원 관리 시스템, 예약 통계 대시보드, 관리자 페이지  
**경화**: React 기반 웹 UI/UX, 실시간 채팅 인터페이스, 반응형 디자인  
**김호중**: Flutter 크로스플랫폼 앱, 모바일 AI 채팅, 예약 관리 앱  
**박승원**: 브랜딩 시스템, Figma 프로토타입, 전체 UI/UX 디자인

---

## 🎯 프로젝트 개요

CareFreePass는 **OpenAI GPT 기반 증상 분석**과 **비콘 기술**을 활용한 혁신적인 병원 예약 시스템입니다.

### 🔥 핵심 차별점
- **🤖 AI 증상 상담**: 자연어로 증상을 설명하면 AI가 적절한 진료과 추천
- **📱 원터치 예약**: 복잡한 병원 예약 과정을 3단계로 단축
- **🔗 비콘 체크인**: 병원 도착 시 자동 접수로 대기시간 최소화
- **⚡ 실시간 응답**: WebSocket 기반 실시간 AI 채팅 상담

---

## 🛠️ 기술 스택

<div align="center">

### 🖥️ Environment
![IntelliJ IDEA](https://img.shields.io/badge/IntelliJ%20IDEA-000000?style=for-the-badge&logo=intellijidea&logoColor=white)
![Git](https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=git&logoColor=white)
![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)

### ⚙️ Config
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)
![YAML](https://img.shields.io/badge/YAML-CB171E?style=for-the-badge&logo=yaml&logoColor=white)

### 💻 Development
![Java](https://img.shields.io/badge/Java%2021-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot%203.3.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white)

### 🗄️ Database & Cache
![MariaDB](https://img.shields.io/badge/MariaDB-003545?style=for-the-badge&logo=mariadb&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)

### ☁️ Infrastructure
![AWS](https://img.shields.io/badge/AWS-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![AWS RDS](https://img.shields.io/badge/AWS%20RDS-527FFF?style=for-the-badge&logo=amazonrds&logoColor=white)
![AWS Amplify](https://img.shields.io/badge/AWS%20Amplify-FF9900?style=for-the-badge&logo=awsamplify&logoColor=white)
![Nginx](https://img.shields.io/badge/Nginx-009639?style=for-the-badge&logo=nginx&logoColor=white)

### 🤖 AI & Communication
![OpenAI](https://img.shields.io/badge/OpenAI%20GPT--3.5-412991?style=for-the-badge&logo=openai&logoColor=white)
![WebSocket](https://img.shields.io/badge/WebSocket-010101?style=for-the-badge&logo=socketdotio&logoColor=white)

</div>

---

## 🏗️ 프로젝트 구조
```
📁 src/main/java/org/carefreepass/com/carefreepassserver/
├── 🏥 domain/
│   ├── 👤 auth/                 # 사용자 인증 & JWT 토큰 관리
│   │   ├── controller/          # 로그인, 회원가입, SMS 인증 API
│   │   ├── service/            # 사용자 서비스 로직
│   │   ├── entity/             # Member, PatientProfile 엔티티
│   │   └── repository/         # JPA Repository
│   ├── 🏥 hospital/            # 병원 & 진료과 관리
│   │   ├── controller/         # 병원, 진료과 조회 API  
│   │   ├── service/           # 병원 관리 로직
│   │   └── entity/            # Hospital, Department 엔티티
│   ├── 📅 appointment/         # 예약 시스템
│   │   ├── controller/        # 예약 CRUD API
│   │   ├── service/          # 예약 비즈니스 로직
│   │   └── entity/           # Appointment 엔티티
│   └── 💬 chat/               # AI 채팅 & 증상 분석
│       ├── service/          # OpenAI API 연동, 증상 분석
│       ├── entity/           # ChatSession, ChatMessage 엔티티
│       └── dto/              # 채팅 요청/응답 DTO
├── ⚙️ global/                # 공통 설정
│   ├── config/              # Spring, Security, Redis 설정
│   ├── error/               # 글로벌 예외 처리
│   └── util/                # 공통 유틸리티
└── 🔧 config/               # 외부 API 설정 (OpenAI, SMS)
```

### 🔄 AI 증상 분석 플로우
```
👤 사용자 증상 입력
     ↓
🔍 1단계: 직접 진료과 언급 확인 (95% 신뢰도)
     ↓ (실패 시)
🧠 2단계: 스마트 증상 키워드 분석 (가변 신뢰도)  
     ↓ (실패 시)
🤖 3단계: OpenAI GPT 분석 (70% 신뢰도)
     ↓ (실패 시)
❓ 4단계: 추가 질문 유도 (0% 신뢰도)
     ↓
📋 신뢰도 기반 응답 & 예약 안내
```

---

## 📱 주요 기능

### 🤖 AI 기반 증상 분석
```
사용자 입력: "배가 아프고 설사를 해요"
     ↓
AI 분석: 소화기 증상 인식 → 내과 추천 (신뢰도: 85%)
     ↓
응답: "소화기 증상으로 보아 내과 진료를 추천드립니다. 🏥 내과 예약이 가능합니다."
```

**핵심 특징:**
- **3단계 신뢰도 시스템**: 95% (직접 언급) → 70% (AI 분석) → 0% (추가 질문)
- **10개 진료과 지원**: 내과, 외과, 정형외과, 피부과, 이비인후과, 안과, 산부인과, 소아과, 정신과, 치과
- **실시간 WebSocket**: 즉시 응답하는 채팅 인터페이스

### 📅 스마트 예약 시스템
- **실시간 예약 가능 시간 조회**: 의료진 스케줄 기반
- **원터치 예약**: AI 추천 → 시간 선택 → 예약 완료 (3단계)
- **예약 변경/취소**: 유연한 예약 관리
- **SMS 알림**: 예약 확인 및 리마인더

### 🔐 보안 & 인증
- **JWT 토큰 기반 인증**: 안전한 API 접근 제어
- **SMS 본인 확인**: Cool SMS API 연동
- **개인정보 보호**: 의료법 준수 데이터 암호화

---
<img width="1283" height="721" alt="스크린샷 2025-09-08 152125" src="https://github.com/user-attachments/assets/25bd3a35-14b8-4139-b9dc-6b16995a85ac" />
<img width="1277" height="715" alt="스크린샷 2025-09-08 152132" src="https://github.com/user-attachments/assets/fee4694d-addc-44ff-b005-422b31e952bc" />
<img width="1280" height="714" alt="스크린샷 2025-09-08 152143" src="https://github.com/user-attachments/assets/edeb2800-477d-416c-a4a6-2060b5b6aca0" />
<img width="1277" height="718" alt="스크린샷 2025-09-08 152148" src="https://github.com/user-attachments/assets/f1624322-8286-4e83-8235-f7941076e387" />
<img width="1274" height="705" alt="스크린샷 2025-09-08 152153" src="https://github.com/user-attachments/assets/b3a8bd85-f6bd-4cf4-8f65-811f9712fbb6" />
<img width="1281" height="721" alt="스크린샷 2025-09-08 152157" src="https://github.com/user-attachments/assets/f5710fe1-1c78-4d99-b954-d76e57cfd366" />

<div align="center">

### 🏥 CareFreePass
**AI가 만드는 더 나은 의료 경험**

### 📦 관련 Repository

[![Backend](https://img.shields.io/badge/Backend-Spring%20Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://github.com/9oormthon-univ/2025_SEASONTHON_TEAM_45_BE)
[![Frontend](https://img.shields.io/badge/Frontend-React-61DAFB?style=for-the-badge&logo=react&logoColor=black)](https://github.com/9oormthon-univ/2025_SEASONTHON_TEAM_45_FE)
[![App](https://img.shields.io/badge/App-Flutter-02569B?style=for-the-badge&logo=flutter&logoColor=white)](https://github.com/9oormthon-univ/2025_SEASONTHON_TEAM_45_APP)

**Made with ❤️ by Team CareFreePass**

</div>
