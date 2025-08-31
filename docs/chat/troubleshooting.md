# 문제 해결 가이드 (Troubleshooting)

## 🔧 개요

예약 시스템 간소화 및 RDS 마이그레이션 과정에서 발생할 수 있는 문제들과 해결 방법을 정리한 문서입니다.

## 🚨 데이터베이스 연결 문제

### 1. RDS 연결 실패

#### 문제: `Communications link failure`
```
com.mysql.cj.jdbc.exceptions.CommunicationsException: Communications link failure
The last packet sent successfully to the server was 0 milliseconds ago.
```

#### 원인
- AWS RDS 퍼블릭 액세스가 비활성화됨
- 보안 그룹에서 3306 포트 접근 차단

#### 해결 방법

**1단계: RDS 퍼블릭 액세스 활성화**
```bash
# AWS CLI로 확인
aws rds describe-db-instances --db-instance-identifier your-db-instance

# 또는 AWS 콘솔에서 확인
# RDS > 데이터베이스 > 인스턴스 선택 > 연결 및 보안 탭 > 퍼블릭 액세스 가능성
```

**2단계: 보안 그룹 규칙 추가**
```bash
# 보안 그룹에 MySQL/MariaDB 포트 (3306) 허용 규칙 추가
# Source: 0.0.0.0/0 (개발 환경용, 운영에서는 특정 IP만 허용)
```

**3단계: 연결 테스트**
```bash
# MySQL 클라이언트로 직접 연결 테스트
mysql -h carefree.cxyqkkcq4qxs.ap-northeast-2.rds.amazonaws.com -P 3306 -u admin -p
```

### 2. 데이터베이스 존재하지 않음

#### 문제: `Unknown database 'carefreepass'`
```
java.sql.SQLSyntaxErrorException: Unknown database 'carefreepass'
```

#### 해결 방법

**방법 1: JDBC URL에 자동 생성 옵션 추가**
```yaml
# application-datasource.yml
spring:
  datasource:
    url: jdbc:mariadb://host:3306/dbname?createDatabaseIfNotExist=true
```

**방법 2: 수동으로 데이터베이스 생성**
```sql
-- MySQL 클라이언트 접속 후
CREATE DATABASE carefreepass CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 인증 실패

#### 문제: `Access denied for user`
```
java.sql.SQLException: Access denied for user 'admin'@'IP' (using password: YES)
```

#### 해결 방법
```bash
# .env 파일 확인
MARIADB_USERNAME=admin
MARIADB_PASSWORD=rootroot

# 환경변수 로드 확인
echo $MARIADB_USERNAME
echo $MARIADB_PASSWORD
```

## 💻 서버 실행 문제

### 1. 포트 이미 사용 중

#### 문제: `Port 8080 was already in use`
```
Web server failed to start. Port 8080 was already in use.
```

#### 해결 방법

**Windows 환경**:
```bash
# 8080 포트 사용 프로세스 확인
netstat -ano | findstr :8080

# 프로세스 강제 종료 (PID 확인 후)
taskkill /F /PID <프로세스ID>

# 또는 PowerShell 사용
powershell "Stop-Process -Id <프로세스ID> -Force"
```

**Linux/Mac 환경**:
```bash
# 8080 포트 사용 프로세스 확인
lsof -i :8080

# 프로세스 종료
kill -9 <PID>
```

### 2. Gradle 데몬 문제

#### 문제: `Gradle daemon` 관련 오류
```
Starting a Gradle Daemon, 8 busy and 1 incompatible Daemons could not be reused
```

#### 해결 방법
```bash
# Gradle 데몬 상태 확인
./gradlew --status

# 모든 Gradle 데몬 중지
./gradlew --stop

# 프로젝트 정리 후 재실행
./gradlew clean
./gradlew bootRun --args="--spring.profiles.active=datasource"
```

## 🔤 인코딩 문제

### 1. UTF-8 JSON 파싱 에러

#### 문제: `Invalid UTF-8 start byte`
```
JSON parse error: Invalid UTF-8 start byte 0xbc
```

#### 원인
- Windows CMD/PowerShell에서 한국어 문자 인코딩 문제
- curl 명령어에서 UTF-8 처리 미지원

#### 해결 방법

**방법 1: 웹 브라우저나 Postman 사용**
```javascript
// 브라우저에서 테스트
fetch('/api/v1/appointments', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json; charset=UTF-8' },
  body: JSON.stringify({
    "memberId": 1,
    "hospitalName": "서울대병원",
    "department": "정형외과",
    "appointmentDate": "2025-09-01",
    "appointmentTime": "10:30"
  })
});
```

**방법 2: 영어로 테스트**
```bash
curl -X POST http://localhost:8080/api/v1/appointments \
  -H "Content-Type: application/json" \
  -d '{"memberId":1,"hospitalName":"Seoul Hospital","department":"Orthopedics","appointmentDate":"2025-09-01","appointmentTime":"10:30"}'
```

**방법 3: PowerShell에서 UTF-8 설정**
```powershell
# PowerShell 인코딩 설정
$PSDefaultParameterValues['*:Encoding'] = 'utf8'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
```

### 2. 데이터베이스 한글 저장 문제

#### 문제: 한글 데이터가 깨져서 저장됨

#### 해결 방법
```yaml
# application-datasource.yml
spring:
  datasource:
    url: jdbc:mariadb://host:3306/dbname?characterEncoding=UTF-8&useUnicode=true
```

```sql
-- 데이터베이스 문자셋 확인
SHOW VARIABLES LIKE 'character_set_%';
SHOW VARIABLES LIKE 'collation_%';

-- 테이블 문자셋 변경 (필요시)
ALTER TABLE appointment CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

## 🔄 스키마 마이그레이션 문제

### 1. 기존 데이터와 충돌

#### 문제: 기존 테이블에 제거된 필드 참조
```
Column 'doctor_name' cannot be null
```

#### 해결 방법

**개발 환경** (데이터 손실 허용):
```yaml
# application-datasource.yml
spring:
  jpa:
    hibernate:
      ddl-auto: create  # 테이블 재생성
```

**운영 환경** (데이터 보존 필요):
```sql
-- 수동 마이그레이션 스크립트
ALTER TABLE appointment DROP COLUMN doctor_name;
ALTER TABLE appointment DROP COLUMN room_number;
```

### 2. Foreign Key 제약조건 오류

#### 문제: 외래키 제약조건으로 인한 테이블 삭제 실패

#### 해결 방법
```sql
-- 외래키 제약조건 확인
SELECT 
  TABLE_NAME,
  COLUMN_NAME,
  CONSTRAINT_NAME,
  REFERENCED_TABLE_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'carefreepass';

-- 외래키 제약조건 삭제 (필요시)
ALTER TABLE appointment DROP FOREIGN KEY FK_constraint_name;

-- 테이블 재생성
DROP TABLE IF EXISTS appointment;
```

## 🔥 Firebase/FCM 문제

### 1. Firebase 초기화 실패

#### 문제: `Firebase application initialization failed`

#### 해결 방법
```bash
# 서비스 계정 키 파일 확인
ls -la src/main/resources/firebase-service-account.json

# .env 파일 Firebase 설정 확인
FIREBASE_PROJECT_ID=hackerton-fcm
FIREBASE_SERVICE_ACCOUNT_KEY=classpath:firebase-service-account.json
```

### 2. FCM 토큰 등록 실패

#### 문제: FCM 토큰이 null 또는 invalid

#### 해결 방법
```javascript
// 웹에서 FCM 토큰 재생성
import { getMessaging, getToken } from 'firebase/messaging';

const messaging = getMessaging();
const token = await getToken(messaging, {
  vapidKey: 'your-vapid-key'
});
console.log('FCM Token:', token);
```

## 🧪 테스트 관련 문제

### 1. 테스트 데이터베이스 연결 실패

#### 문제: 테스트 실행 시 RDS 연결 시도

#### 해결 방법
```yaml
# application-test.yml 생성
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: create-drop
```

```java
// 테스트 클래스에 프로필 지정
@ActiveProfiles("test")
@SpringBootTest
class AppointmentServiceTest {
    // 테스트 코드
}
```

### 2. MockMvc 한글 인코딩 문제

#### 문제: 테스트에서 한글 응답 깨짐

#### 해결 방법
```java
@TestConfiguration
public class TestConfig {
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
            .configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, false);
    }
}

// MockMvc 설정
mockMvc.perform(post("/api/v1/appointments")
    .contentType(MediaType.APPLICATION_JSON)
    .characterEncoding("UTF-8")
    .content(requestJson))
    .andExpect(status().isOk())
    .andExpect(content().encoding("UTF-8"));
```

## 📊 성능 관련 문제

### 1. RDS 연결 풀 부족

#### 문제: `Unable to obtain connection from database`

#### 해결 방법
```yaml
# application-datasource.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 20000
      idle-timeout: 300000
      max-lifetime: 1200000
```

### 2. 쿼리 성능 저하

#### 문제: 진료과별 예약 조회 느림

#### 해결 방법
```sql
-- 인덱스 추가
CREATE INDEX idx_appointment_department_date ON appointment(department, appointment_date);
CREATE INDEX idx_appointment_date_status ON appointment(appointment_date, status);
```

```java
// 쿼리 최적화
@Query("SELECT a FROM Appointment a WHERE a.department = :department " +
       "AND a.appointmentDate = :date AND a.status = :status")
List<Appointment> findByDepartmentAndDateAndStatus(
    @Param("department") String department,
    @Param("date") LocalDate date,
    @Param("status") AppointmentStatus status
);
```

## 🔍 로그 및 모니터링

### 1. 디버그 로그 활성화

#### application.yml 설정
```yaml
logging:
  level:
    org.carefreepass: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
    com.zaxxer.hikari: DEBUG
```

### 2. RDS 연결 상태 모니터링

#### HealthCheck 엔드포인트 추가
```java
@RestController
public class HealthController {
    
    @Autowired
    private DataSource dataSource;
    
    @GetMapping("/actuator/health/db")
    public ResponseEntity<Map<String, String>> checkDatabase() {
        try (Connection connection = dataSource.getConnection()) {
            Map<String, String> status = new HashMap<>();
            status.put("database", "UP");
            status.put("url", connection.getMetaData().getURL());
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            Map<String, String> status = new HashMap<>();
            status.put("database", "DOWN");
            status.put("error", e.getMessage());
            return ResponseEntity.status(503).body(status);
        }
    }
}
```

## 📞 지원 및 문의

### 긴급 상황 대응

1. **서버 다운**: 먼저 RDS 연결 상태 확인
2. **데이터 손실**: RDS 자동 백업에서 복구 시도
3. **성능 저하**: CloudWatch 메트릭 확인

### 개발팀 연락처

- **백엔드 이슈**: backend-team@carefreepass.com
- **데이터베이스 이슈**: dba-team@carefreepass.com  
- **인프라 이슈**: devops-team@carefreepass.com

### 참고 문서

- [AWS RDS 문서](https://docs.aws.amazon.com/rds/)
- [Spring Boot 문서](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [MariaDB 문서](https://mariadb.com/docs/)

---

**문서 버전**: 1.0  
**최종 업데이트**: 2025년 8월 31일  
**담당자**: CareFreePass 개발팀