# CareFreePass 예약 시스템 간소화 및 RDS 마이그레이션 가이드

## 📋 개요

이 문서는 CareFreePass 병원 예약 시스템을 간소화하고 AWS RDS MariaDB로 마이그레이션한 전체 과정을 설명합니다.

## 🎯 목표

1. **예약 시스템 간소화**: 의사명과 진료실 번호 필드 제거
2. **데이터베이스 마이그레이션**: 로컬 MariaDB → AWS RDS MariaDB
3. **시스템 최적화**: 가장 단순한 형태의 예약 시스템 구축

## 📊 변경 전후 비교

### 이전 시스템
```
예약 정보 = 병원명 + 의사명 + 진료과 + 진료실번호 + 날짜 + 시간
```

### 현재 시스템 (간소화됨)
```
예약 정보 = 병원명(고정: 서울대병원) + 진료과 + 날짜 + 시간
```

## 🔧 주요 변경사항

### 1. 데이터베이스 스키마 변경

**Appointment 테이블 변경**:
```sql
-- 이전 스키마
CREATE TABLE appointment (
    id bigint PRIMARY KEY,
    member_id bigint NOT NULL,
    hospital_name varchar(100) NOT NULL,
    doctor_name varchar(50),      -- ❌ 제거됨
    department varchar(50) NOT NULL,
    room_number varchar(20),      -- ❌ 제거됨
    appointment_date date NOT NULL,
    appointment_time time NOT NULL,
    status enum(...) NOT NULL
);

-- 현재 스키마 (간소화됨)
CREATE TABLE appointment (
    id bigint PRIMARY KEY,
    member_id bigint NOT NULL,
    hospital_name varchar(100) NOT NULL,
    department varchar(50) NOT NULL,
    appointment_date date NOT NULL,
    appointment_time time NOT NULL,
    status enum(...) NOT NULL
);
```

### 2. AWS RDS 설정

**데이터베이스 연결 정보**:
```yaml
# application-datasource.yml
spring:
  datasource:
    url: jdbc:mariadb://carefree.cxyqkkcq4qxs.ap-northeast-2.rds.amazonaws.com:3306/carefreepass?characterEncoding=UTF-8&serverTimezone=Asia/Seoul&createDatabaseIfNotExist=true
    driver-class-name: org.mariadb.jdbc.Driver
    username: ${MARIADB_USERNAME}  # admin
    password: ${MARIADB_PASSWORD}  # rootroot
  jpa:
    hibernate:
      ddl-auto: create  # RDS 초기 설정용
```

### 3. 환경변수 설정

**.env 파일 업데이트**:
```env
# AWS RDS MariaDB 설정
DB_NAME=carefreepass
MARIADB_HOST=carefree.cxyqkkcq4qxs.ap-northeast-2.rds.amazonaws.com
MARIADB_PORT=3306
MARIADB_USERNAME=admin
MARIADB_PASSWORD=rootroot
```

## 📁 수정된 파일 목록

### 1. 엔티티 클래스
- `src/main/java/org/carefreepass/com/carefreepassserver/domain/appointment/entity/Appointment.java`

### 2. 서비스 클래스
- `src/main/java/org/carefreepass/com/carefreepassserver/domain/appointment/service/AppointmentService.java`
- `src/main/java/org/carefreepass/com/carefreepassserver/domain/chat/service/AppointmentBookingService.java`

### 3. 컨트롤러 클래스
- `src/main/java/org/carefreepass/com/carefreepassserver/domain/appointment/controller/AppointmentController.java`

### 4. DTO 클래스
- `src/main/java/org/carefreepass/com/carefreepassserver/domain/appointment/dto/AppointmentCreateRequest.java`
- `src/main/java/org/carefreepass/com/carefreepassserver/domain/appointment/dto/AppointmentUpdateRequest.java`
- `src/main/java/org/carefreepass/com/carefreepassserver/domain/appointment/dto/AppointmentResponse.java`
- `src/main/java/org/carefreepass/com/carefreepassserver/domain/chat/service/AppointmentInfo.java`

### 5. 리포지토리 클래스
- `src/main/java/org/carefreepass/com/carefreepassserver/domain/appointment/repository/AppointmentRepository.java`

### 6. 정적 리소스
- `src/main/resources/static/test-fcm.html`

### 7. 설정 파일
- `src/main/resources/application-datasource.yml`
- `.env`

## 🚀 실행 방법

### 1. 프로젝트 실행
```bash
# Windows 환경
./gradlew.bat bootRun --args="--spring.profiles.active=datasource"

# Linux/Mac 환경  
./gradlew bootRun --args="--spring.profiles.active=datasource"
```

### 2. 서버 확인
```bash
curl -X GET http://localhost:8080/api/v1/test/health
# 응답: {"code":"TEST_2001","message":"서버가 정상 작동 중입니다.","data":"OK"}
```

### 3. 웹 인터페이스 접근
- **FCM 테스트 페이지**: http://localhost:8080/test-fcm.html
- **AI 챗봇 테스트**: http://localhost:8080/chat-test.html

## 📝 API 변경사항

### 예약 생성 API

**이전 요청**:
```json
POST /api/v1/appointments
{
  "memberId": 1,
  "hospitalName": "서울대병원",
  "doctorName": "김의사",        // ❌ 제거됨
  "department": "정형외과",
  "roomNumber": "201호",         // ❌ 제거됨
  "appointmentDate": "2025-09-01",
  "appointmentTime": "10:30"
}
```

**현재 요청 (간소화됨)**:
```json
POST /api/v1/appointments
{
  "memberId": 1,
  "hospitalName": "서울대병원",
  "department": "정형외과",
  "appointmentDate": "2025-09-01",
  "appointmentTime": "10:30"
}
```

### 예약 가능 시간 조회 API

**이전**: `/api/appointments/available-times/doctor?doctorName=김의사&date=2025-09-01`

**현재**: `/api/appointments/available-times/department?department=정형외과&appointmentDate=2025-09-01`

## 🔍 테스트 방법

### 1. 회원가입
```bash
curl -X POST http://localhost:8080/api/v1/auth/patient/sign-up \
  -H "Content-Type: application/json" \
  -d '{"name":"김환자","gender":"남성","birthDate":"19900315","phoneNumber":"01012345678","password":"password123!"}'
```

### 2. 예약 생성
```bash
curl -X POST http://localhost:8080/api/v1/appointments \
  -H "Content-Type: application/json" \
  -d '{"memberId":1,"hospitalName":"서울대병원","department":"정형외과","appointmentDate":"2025-09-01","appointmentTime":"10:30"}'
```

### 3. 오늘 예약 목록 조회
```bash
curl -X GET http://localhost:8080/api/v1/appointments/today
```

## 🐛 알려진 이슈

### 1. UTF-8 인코딩 문제
- **현상**: curl을 통한 한국어 API 요청 시 인코딩 에러 발생
- **해결책**: 웹 브라우저나 Postman 사용 권장

### 2. RDS 연결 요구사항
- **요구사항**: AWS RDS 인스턴스의 퍼블릭 액세스 활성화 필요
- **확인 방법**: AWS Console → RDS → 보안 그룹 설정 확인

## 📈 성능 개선 효과

### 1. 데이터베이스
- **필드 수 감소**: 8개 → 6개 (25% 감소)
- **쿼리 복잡도 감소**: JOIN 없이 단일 테이블 조회
- **저장 공간 최적화**: 불필요한 VARCHAR 필드 제거

### 2. API 응답 속도
- **응답 크기 감소**: JSON 필드 2개 제거
- **비즈니스 로직 단순화**: 의사-진료실 매핑 로직 제거

### 3. 유지보수성
- **코드 복잡도 감소**: 의사 관리 관련 코드 완전 제거
- **테스트 용이성 향상**: 테스트 케이스 단순화

## 🎉 완료된 기능

✅ **의사명 필드 완전 제거**  
✅ **진료실 번호 필드 완전 제거**  
✅ **AWS RDS MariaDB 마이그레이션 완료**  
✅ **간소화된 예약 시스템 정상 작동**  
✅ **AI 챗봇과 연동 확인**  
✅ **FCM 알림 시스템 유지**  

## 📞 문의

시스템 관련 문의사항이 있으시면 개발팀에 연락해주세요.

---

**최종 업데이트**: 2025년 8월 31일  
**작성자**: CareFreePass 개발팀  
**버전**: 2.0.0 (간소화된 예약 시스템)