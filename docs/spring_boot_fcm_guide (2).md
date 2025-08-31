## 5. API 엔드포인트 정리

### 5.1 FCM 토큰 등록
```http
POST /api/notifications/fcm/register
Content-Type: application/json

{
    "userId": 1,
    "fcmToken": "실제FCM토큰값"
}
```

### 5.2 환자 호출
```http
POST /api/notifications/call
Content-Type: application/json

{
    "appointmentId": 1,
    "roomNumber": "2번 진료실"
}
```

### 5.3 대기 환자 목록 조회
```http
GET /api/notifications/waiting-patients
```

## 6. 도메인 구조의 장점

### 6.1 관심사의 분리
- **User 도메인**: 사용자 관리, FCM 토큰 관리
- **Appointment 도메인**: 예약 관리, 상태 변경
- **Notification 도메인**: 알림 전송, 호출 로직
- **Global**: 공통 설정, 예외 처리, 외부 서비스

## 6. 도메인 구조의 장점

### 6.1 관심사의 분리
- **User 도메인**: 사용자 관리, FCM 토큰 관리
- **Appointment 도메인**: 예약 관리, 상태 변경
- **Notification 도메인**: 알림 전송, 호출 로직
- **Global**: 공통 설정, 예외 처리, 외부 서비스

### 6.2 확장성
- 새로운 도메인 추가 시 기존 코드에 영향 최소화
- 각 도메인별 독립적인 개발 가능
- 마이크로서비스 전환 시 도메인 단위로 분리 용이

### 6.3 유지보수성
- 비즈니스 로직을 도메인 중심으로 구성
- 코드의 응집도는 높이고 결합도는 낮춤
- 테스트 작성 및 디버깅 용이

## 7. 추가 구현 가능한 기능들

### 7.1 Hospital 도메인 (선택사항)
```
domain/hospital/
├── entity/
│   ├── Hospital.java
│   └── Department.java
├── repository/
│   └── HospitalRepository.java
├── service/
│   └── HospitalService.java
└── dto/
    ├── HospitalResponse.java
    └── DepartmentResponse.java
```

### 7.2 Doctor 도메인 (선택사항)
```
domain/doctor/
├── entity/
│   └── Doctor.java
├── repository/
│   └── DoctorRepository.java
├── service/
│   └── DoctorService.java
└── dto/
    └── DoctorResponse.java
```

### 7.3 BLE 비콘 도메인 (확장 시)
```
domain/beacon/
├── entity/
│   └── BeaconLog.java
├── repository/
│   └── BeaconLogRepository.java
├── service/
│   └── BeaconService.java
├── controller/
│   └── BeaconController.java
└── dto/
    └── BeaconDetectionRequest.java
```

## 8. 설정 파일들

### 8.1 개발 환경별 설정

#### application-dev.properties
```properties
# MariaDB 개발환경
spring.datasource.url=jdbc:mariadb://localhost:3306/hospital_booking_dev
spring.datasource.username=root
spring.datasource.password=dev-password
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# 개발용 Firebase 프로젝트
firebase.project-id=hospital-booking-system-dev

# 로그 레벨 상세 설정
logging.level.root=INFO
logging.level.com.hospital.booking=DEBUG
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

#### application-prod.properties
```properties
# MariaDB 운영환경
spring.datasource.url=jdbc:mariadb://prod-server:3306/hospital_booking
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# 운영용 Firebase 프로젝트
firebase.project-id=hospital-booking-system-prod

# 운영 로그 설정
logging.level.root=WARN
logging.level.com.hospital.booking=INFO

# 보안 설정
server.error.include-stacktrace=never
server.error.include-message=never
```

#### application-test.properties
```properties
# H2 인메모리 데이터베이스 (테스트용)
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

# Firebase 비활성화 (테스트에서는 Mock 사용)
firebase.project-id=test-project
```

## 9. Gradle 추가 설정 및 의존성

### 9.1 build.gradle 전체 설정
```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.1.4'
}

group = 'com.hospital'
version = '1.0.0'

java {
    sourceCompatibility = '17'
}

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starters
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    
    // Database
    implementation 'org.mariadb.jdbc:mariadb-java-client:3.1.4'
    
    // FCM
    implementation 'com.google.firebase:firebase-admin:9.2.0'
    
    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    
    // Test
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'com.h2database:h2'
    testCompileOnly 'org.projectlombok:lombok'
    testAnnotationProcessor 'org.projectlombok:lombok'
    
    // Development Tools
    developmentOnly 'org.springframework.boot:spring-boot-devtools'
}

tasks.named('test') {
    useJUnitPlatform()
}

// JAR 파일 설정
jar {
    archiveBaseName = 'hospital-booking-api'
    archiveVersion = '1.0.0'
    enabled = false
}

bootJar {
    archiveBaseName = 'hospital-booking-api'
    archiveVersion = '1.0.0'
}
```

### 9.2 .gitignore 설정
```gitignore
# Firebase 보안 파일
firebase-service-account.json
**/firebase-service-account*.json

# 빌드 파일
build/
.gradle/
out/

# IDE 파일
.idea/
*.iml
*.iws
.vscode/
.settings/
.project
.classpath

# 로그 파일
*.log
logs/

# OS 파일
.DS_Store
Thumbs.db

# 환경 변수 파일
.env
.env.local
.env.prod

# 테스트 관련
/test-results/
/coverage/
```

## 10. 테스트 작성 예시

### 10.1 단위 테스트 예시

#### UserServiceTest.java
```java
package com.hospital.booking.domain.user.service;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    @DisplayName("FCM 토큰 등록 성공")
    void registerFcmToken_Success() {
        // Given
        Long userId = 1L;
        String fcmToken = "test-fcm-token";
        User user = User.builder()
                .id(userId)
                .name("테스트사용자")
                .phone("010-1234-5678")
                .build();
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        // When & Then
        assertDoesNotThrow(() -> userService.registerFcmToken(userId, fcmToken));
        
        verify(userRepository).findById(userId);
        verify(userRepository).save(user);
        assertEquals(fcmToken, user.getFcmToken());
    }
    
    @Test
    @DisplayName("존재하지 않는 사용자 FCM 토큰 등록 시 예외 발생")
    void registerFcmToken_UserNotFound_ThrowsException() {
        // Given
        Long userId = 999L;
        String fcmToken = "test-fcm-token";
        
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(BusinessException.class, 
            () -> userService.registerFcmToken(userId, fcmToken));
        
        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }
}
```

### 10.2 통합 테스트 예시

#### NotificationControllerIntegrationTest.java
```java
package com.hospital.booking.domain.notification.controller;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class NotificationControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @MockBean
    private FCMService fcmService;
    
    @Test
    @DisplayName("FCM 토큰 등록 통합 테스트")
    void registerFcmToken_Integration_Success() {
        // Given
        User user = User.builder()
                .name("테스트사용자")
                .phone("010-1234-5678")
                .build();
        user = userRepository.save(user);
        
        FCMTokenRequest request = FCMTokenRequest.builder()
                .userId(user.getId())
                .fcmToken("test-fcm-token")
                .build();
        
        // When
        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                "/api/notifications/fcm/register", 
                request, 
                ApiResponse.class
        );
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        
        User updatedUser = userRepository.findById(user.getId()).orElse(null);
        assertNotNull(updatedUser);
        assertEquals("test-fcm-token", updatedUser.getFcmToken());
    }
}
```

## 11. 운영 고려사항

### 11.1 모니터링 설정

#### application.properties에 추가
```properties
# Actuator 엔드포인트 활성화
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=always
management.info.env.enabled=true

# 애플리케이션 정보
info.app.name=Hospital Booking System
info.app.version=1.0.0
info.app.description=병원 예약 및 FCM 알림 시스템
```

### 11.2 로깅 설정

#### logback-spring.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <springProfile name="dev">
        <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
            <encoder>
                <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
            </encoder>
        </appender>
        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
        </root>
        <logger name="com.hospital.booking" level="DEBUG"/>
    </springProfile>
    
    <springProfile name="prod">
        <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
            <file>logs/hospital-booking.log</file>
            <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
                <fileNamePattern>logs/hospital-booking.%d{yyyy-MM-dd}.%i.gz</fileNamePattern>
                <maxFileSize>10MB</maxFileSize>
                <maxHistory>30</maxHistory>
                <totalSizeCap>1GB</totalSizeCap>
            </rollingPolicy>
            <encoder>
                <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
            </encoder>
        </appender>
        <root level="INFO">
            <appender-ref ref="FILE"/>
        </root>
        <logger name="com.hospital.booking" level="INFO"/>
    </springProfile>
</configuration>
```

### 11.3 Docker 설정 (선택사항)

#### Dockerfile
```dockerfile
FROM openjdk:17-jre-slim

VOLUME /tmp

ARG JAR_FILE=build/libs/hospital-booking-api-1.0.0.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]
```

#### docker-compose.yml

```yaml
version: '3.8'
services:
  mariadb:
    image: mariadb:10.9
    environment:
      MYSQL_ROOT_PASSWORD: root-password
      MYSQL_DATABASE: hospital_booking
      MYSQL_USER: booking_user
      MYSQL_PASSWORD: booking_password
    ports:
      - "3306:3306"
    volumes:
      - mariadb_data:/var/lib/mysql

  app:
    build: ../../../Users/PC/Downloads
    ports:
      - "8080:8080"
    depends_on:
      - mariadb
    environment:
      SPRING_DATASOURCE_URL: jdbc:mariadb://mariadb:3306/hospital_booking
      SPRING_DATASOURCE_USERNAME: booking_user
      SPRING_DATASOURCE_PASSWORD: booking_password
      SPRING_PROFILES_ACTIVE: prod

volumes:
  mariadb_data:
```

## 12. 개발 시작 체크리스트

### 12.1 필수 준비사항
- [ ] Firebase 프로젝트 생성 완료
- [ ] firebase-service-account.json 다운로드 및 배치
- [ ] MariaDB 설치 및 데이터베이스 생성
- [ ] application.properties 데이터베이스 정보 수정
- [ ] .gitignore에 Firebase 키 파일 추가

### 12.2 구현 순서 추천
1. [ ] 글로벌 설정 (FirebaseConfig, ApiResponse, Exception 처리)
2. [ ] User 도메인 구현 (Entity → Repository → Service)
3. [ ] Appointment 도메인 구현
4. [ ] FCM 서비스 구현
5. [ ] Notification 도메인 구현 (Service → Controller)
6. [ ] 테스트 데이터 초기화
7. [ ] Postman으로 API 테스트
8. [ ] 단위 테스트 작성

### 12.3 프론트엔드와의 협업 포인트
- [ ] FCM 토큰 등록 API 문서 공유
- [ ] 환자 호출 API 문서 공유
- [ ] 대기 환자 목록 API 문서 공유
- [ ] 에러 응답 형식 공유
- [ ] Firebase 프로젝트 설정 파일 공유

이제 도메인 기반의 클린한 아키텍처로 FCM 푸시 알림 시스템을 구축할 수 있습니다! 🎯# Spring Boot FCM 구현 완전 가이드 (도메인 기반 구조)

## 0. 프로젝트 패키지 구조

### 도메인 기반 패키지 구조 (Domain-Driven Design)
```
src/main/java/com/hospital/booking/
├── HospitalBookingApplication.java
├── global/                           # 글로벌 공통 기능
│   ├── config/
│   │   ├── FirebaseConfig.java
│   │   ├── JpaConfig.java
│   │   └── WebConfig.java
│   ├── common/
│   │   ├── response/
│   │   │   └── ApiResponse.java
│   │   ├── exception/
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   ├── BusinessException.java
│   │   │   └── ErrorCode.java
│   │   └── util/
│   │       └── DateTimeUtil.java
│   ├── external/
│   │   └── fcm/
│   │       ├── FCMService.java
│   │       └── FCMProperties.java
│   └── security/
│       └── SecurityConfig.java
├── domain/                           # 도메인별 구성
│   ├── user/
│   │   ├── entity/
│   │   │   └── User.java
│   │   ├── repository/
│   │   │   └── UserRepository.java
│   │   ├── service/
│   │   │   └── UserService.java
│   │   ├── controller/
│   │   │   └── UserController.java
│   │   └── dto/
│   │       ├── UserCreateRequest.java
│   │       ├── UserResponse.java
│   │       └── FCMTokenRequest.java
│   ├── appointment/
│   │   ├── entity/
│   │   │   ├── Appointment.java
│   │   │   └── AppointmentStatus.java
│   │   ├── repository/
│   │   │   └── AppointmentRepository.java
│   │   ├── service/
│   │   │   └── AppointmentService.java
│   │   ├── controller/
│   │   │   └── AppointmentController.java
│   │   └── dto/
│   │       ├── AppointmentCreateRequest.java
│   │       ├── AppointmentResponse.java
│   │       └── WaitingPatientDto.java
│   └── notification/
│       ├── service/
│       │   └── NotificationService.java
│       ├── controller/
│       │   └── NotificationController.java
│       └── dto/
│           └── PatientCallRequest.java
└── infrastructure/                   # 인프라 관련
    └── persistence/
        └── TestDataInitializer.java
```

## 1. 사전 준비 단계
### 1.1 Firebase 프로젝트 설정
   - https://console.firebase.google.com/ 접속
   - Google 계정으로 로그인

2. **프로젝트 생성**
   - "프로젝트 추가" 클릭
   - 프로젝트 이름: `hospital-booking-system`
   - Google Analytics: 선택 사항

3. **서비스 계정 키 생성**
   - Firebase Console → 프로젝트 설정 (⚙️ 아이콘)
   - "서비스 계정" 탭
   - "새 비공개 키 생성" 클릭
   - JSON 파일 다운로드 → `firebase-service-account.json` 저장

4. **Android 앱 추가 (프론트엔드와 협업)**
   - 프로젝트 개요 → "앱 추가" → Android
   - 패키지 이름: `com.hospital.booking`
   - google-services.json 다운로드 (앱 개발자에게 전달)

---

## 2. Spring Boot 프로젝트 설정

### 2.1 의존성 추가 (build.gradle)
```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    
    // MariaDB
    implementation 'org.mariadb.jdbc:mariadb-java-client:3.1.4'
    
    // FCM Admin SDK
    implementation 'com.google.firebase:firebase-admin:9.2.0'
    
    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    
    // 테스트
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testCompileOnly 'org.projectlombok:lombok'
    testAnnotationProcessor 'org.projectlombok:lombok'
}
```

### 2.2 application.properties 설정
```properties
# MariaDB 데이터베이스 설정
spring.datasource.url=jdbc:mariadb://localhost:3306/hospital_booking
spring.datasource.username=root
spring.datasource.password=your-password
spring.datasource.driver-class-name=org.mariadb.jdbc.Driver

# JPA 설정
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.database-platform=org.hibernate.dialect.MariaDBDialect
spring.jpa.properties.hibernate.format_sql=true

# Firebase 설정
firebase.service-account-key=classpath:firebase-service-account.json
firebase.project-id=hospital-booking-system

# 서버 포트 (선택사항)
server.port=8080

# 로그 레벨 설정
logging.level.com.hospital.booking=DEBUG
logging.level.org.springframework.web=DEBUG

### 2.3 Firebase 서비스 계정 키 파일 위치
```
src/
└── main/
    └── resources/
        ├── application.properties
        └── firebase-service-account.json  ← 여기에 배치
```

---

## 3. 데이터베이스 테이블 설계

### 3.1 사용자 및 FCM 토큰 테이블
```sql
-- 사용자 테이블
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(20) UNIQUE NOT NULL,
    fcm_token TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 예약 테이블
CREATE TABLE appointments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    hospital_name VARCHAR(100) NOT NULL,
    department VARCHAR(50) NOT NULL,
    doctor_name VARCHAR(50),
    appointment_date DATE NOT NULL,
    appointment_time TIME NOT NULL,
    status VARCHAR(20) DEFAULT 'BOOKED', -- BOOKED, ARRIVED, CALLED, IN_PROGRESS, COMPLETED
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 호출 이력 테이블 (선택사항)
CREATE TABLE call_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    appointment_id BIGINT NOT NULL,
    called_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_successful BOOLEAN DEFAULT TRUE,
    error_message TEXT,
    
    FOREIGN KEY (appointment_id) REFERENCES appointments(id)
);
```

---

## 4. 엔티티 클래스 생성

## 4. 도메인별 구현

### 4.1 글로벌 설정 (global/config/)

#### FirebaseConfig.java
```java
package com.hospital.booking.global.config;

@Configuration
@Slf4j
public class FirebaseConfig {
    
    @Value("${firebase.service-account-key}")
    private String serviceAccountKeyPath;
    
    @Value("${firebase.project-id}")
    private String projectId;
    
    @PostConstruct
    public void initializeFirebase() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount = getClass()
                    .getClassLoader()
                    .getResourceAsStream("firebase-service-account.json");
                
                if (serviceAccount == null) {
                    throw new FileNotFoundException("Firebase service account key file not found in classpath");
                }
                
                FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setProjectId(projectId)
                    .build();
                
                FirebaseApp.initializeApp(options);
                log.info("Firebase application initialized successfully with project ID: {}", projectId);
            }
        } catch (Exception e) {
            log.error("Failed to initialize Firebase: {}", e.getMessage(), e);
            throw new RuntimeException("Firebase initialization failed", e);
        }
    }
}
```

#### ApiResponse.java
```java
package com.hospital.booking.global.common.response;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("성공")
                .data(data)
                .build();
    }
    
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }
    
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(null)
                .build();
    }
}
```

#### GlobalExceptionHandler.java
```java
package com.hospital.booking.global.common.exception;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("Business exception occurred: {}", e.getMessage());
        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(ApiResponse.error(e.getMessage()));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        
        log.warn("Validation exception occurred: {}", message);
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("입력값이 올바르지 않습니다: " + message));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception e) {
        log.error("Unexpected exception occurred", e);
        return ResponseEntity.internalServerError()
                .body(ApiResponse.error("서버 내부 오류가 발생했습니다"));
    }
}
```

### 4.2 사용자 도메인 (domain/user/)

#### User.java (Entity)
```java
package com.hospital.booking.domain.user.entity;

@Service
@Slf4j
public class FCMService {
    
    /**
     * 단일 기기에 푸시 알림 전송
     */
    public boolean sendNotification(String fcmToken, String title, String body, Map<String, String> data) {
        try {
            Message.Builder messageBuilder = Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build())
                .setAndroid(AndroidConfig.builder()
                    .setNotification(AndroidNotification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .setIcon("ic_notification")
                        .setColor("#FF0000")
                        .setSound("default")
                        .setPriority(AndroidNotification.Priority.HIGH)
                        .build())
                    .build());
            
            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }
            
            Message message = messageBuilder.build();
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Successfully sent FCM message: {}", response);
            
            return true;
            
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send FCM message to token: {}", fcmToken, e);
            
            if (e.getErrorCode().equals("INVALID_ARGUMENT") || 
                e.getErrorCode().equals("UNREGISTERED")) {
                log.warn("Invalid FCM token detected: {}", fcmToken);
            }
            
            return false;
        } catch (Exception e) {
            log.error("Unexpected error while sending FCM message", e);
            return false;
        }
    }
    
    /**
     * 환자 호출 전용 알림
     */
    public boolean sendCallNotification(String fcmToken, String patientName, String roomNumber) {
        String title = "진료 호출";
        String body = String.format("%s님, %s로 들어오세요.", patientName, roomNumber);
        
        Map<String, String> data = new HashMap<>();
        data.put("type", "PATIENT_CALL");
        data.put("patient_name", patientName);
        data.put("room_number", roomNumber);
        data.put("timestamp", String.valueOf(System.currentTimeMillis()));
        
        return sendNotification(fcmToken, title, body, data);
    }
}
```

### 4.6 예외 처리 (global/common/exception/)

#### ErrorCode.java
```java
package com.hospital.booking.global.common.exception;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // User 관련
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다"),
    
    // Appointment 관련
    APPOINTMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "예약 정보를 찾을 수 없습니다"),
    APPOINTMENT_CANNOT_BE_CALLED(HttpStatus.BAD_REQUEST, "환자가 아직 도착하지 않았습니다"),
    
    // FCM 관련
    FCM_TOKEN_NOT_FOUND(HttpStatus.BAD_REQUEST, "환자의 FCM 토큰이 없습니다"),
    FCM_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "푸시 알림 전송에 실패했습니다"),
    
    // 일반적인 오류
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다");
    
    private final HttpStatus httpStatus;
    private final String message;
}
```

#### BusinessException.java
```java
package com.hospital.booking.global.common.exception;

@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
```

### 4.7 테스트 데이터 초기화 (infrastructure/)

#### TestDataInitializer.java
```java
package com.hospital.booking.infrastructure.persistence;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class TestDataInitializer {
    
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    
    @PostConstruct
    public void initData() {
        User testUser = User.builder()
                .name("김환자")
                .phone("010-1234-5678")
                .fcmToken("test_fcm_token_here")
                .build();
        userRepository.save(testUser);
        
        Appointment appointment = Appointment.builder()
                .user(testUser)
                .hospitalName("서울대학교병원")
                .department("내과")
                .doctorName("홍길동")
                .appointmentDate(LocalDate.now())
                .appointmentTime(LocalTime.of(10, 30))
                .status(AppointmentStatus.ARRIVED)
                .build();
        appointmentRepository.save(appointment);
    }
}
```
```

#### UserRepository.java
```java
package com.hospital.booking.domain.user.repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhone(String phone);
    
    @Modifying
    @Query("UPDATE User u SET u.fcmToken = :fcmToken WHERE u.id = :userId")
    int updateFcmToken(@Param("userId") Long userId, @Param("fcmToken") String fcmToken);
}
```

#### UserService.java
```java
package com.hospital.booking.domain.user.service;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    
    public void registerFcmToken(Long userId, String fcmToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        user.updateFcmToken(fcmToken);
        userRepository.save(user);
    }
    
    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
    
    @Transactional(readOnly = true)
    public Optional<User> getUserByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }
}
```

#### FCMTokenRequest.java (DTO)
```java
package com.hospital.booking.domain.user.dto;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FCMTokenRequest {
    @NotNull(message = "사용자 ID는 필수입니다")
    private Long userId;
    
    @NotBlank(message = "FCM 토큰은 필수입니다")
    private String fcmToken;
}
```

### 4.3 예약 도메인 (domain/appointment/)

#### Appointment.java (Entity)
```java
package com.hospital.booking.domain.appointment.entity;

@Entity
@Table(name = "appointments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, length = 100)
    private String hospitalName;
    
    @Column(nullable = false, length = 50)
    private String department;
    
    @Column(length = 50)
    private String doctorName;
    
    @Column(nullable = false)
    private LocalDate appointmentDate;
    
    @Column(nullable = false)
    private LocalTime appointmentTime;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.BOOKED;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    // 비즈니스 로직 메서드
    public void updateStatus(AppointmentStatus newStatus) {
        this.status = newStatus;
    }
    
    public void markAsArrived() {
        updateStatus(AppointmentStatus.ARRIVED);
    }
    
    public void markAsCalled() {
        updateStatus(AppointmentStatus.CALLED);
    }
    
    public void markAsInProgress() {
        updateStatus(AppointmentStatus.IN_PROGRESS);
    }
    
    public void markAsCompleted() {
        updateStatus(AppointmentStatus.COMPLETED);
    }
    
    public boolean canBeCalled() {
        return status == AppointmentStatus.ARRIVED;
    }
    
    public boolean isToday() {
        return appointmentDate.equals(LocalDate.now());
    }
    
    // 정적 팩토리 메서드
    public static Appointment createAppointment(User user, String hospitalName, String department, 
                                              String doctorName, LocalDate date, LocalTime time) {
        return Appointment.builder()
                .user(user)
                .hospitalName(hospitalName)
                .department(department)
                .doctorName(doctorName)
                .appointmentDate(date)
                .appointmentTime(time)
                .build();
    }
}
```

#### AppointmentRepository.java
```java
package com.hospital.booking.domain.appointment.repository;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    List<Appointment> findByStatusOrderByAppointmentTimeAsc(AppointmentStatus status);
    
    List<Appointment> findByStatusInOrderByAppointmentTimeAsc(List<AppointmentStatus> statuses);
    
    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate = :date AND a.status IN :statuses ORDER BY a.appointmentTime ASC")
    List<Appointment> findTodayAppointmentsByStatus(
        @Param("date") LocalDate date, 
        @Param("statuses") List<AppointmentStatus> statuses
    );
    
    Optional<Appointment> findByIdAndStatus(Long id, AppointmentStatus status);
}
```

#### AppointmentService.java
```java
package com.hospital.booking.domain.appointment.service;

@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentService {
    
    private final AppointmentRepository appointmentRepository;
    
    @Transactional(readOnly = true)
    public List<Appointment> getTodayWaitingAppointments() {
        List<AppointmentStatus> waitingStatuses = Arrays.asList(
                AppointmentStatus.BOOKED, 
                AppointmentStatus.ARRIVED
        );
        
        return appointmentRepository.findTodayAppointmentsByStatus(LocalDate.now(), waitingStatuses);
    }
    
    @Transactional(readOnly = true)
    public Appointment getAppointmentById(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPOINTMENT_NOT_FOUND));
    }
    
    public void markAppointmentAsCalled(Long appointmentId) {
        Appointment appointment = getAppointmentById(appointmentId);
        
        if (!appointment.canBeCalled()) {
            throw new BusinessException(ErrorCode.APPOINTMENT_CANNOT_BE_CALLED);
        }
        
        appointment.markAsCalled();
        appointmentRepository.save(appointment);
    }
}
```

#### WaitingPatientDto.java
```java
package com.hospital.booking.domain.appointment.dto;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class WaitingPatientDto {
    private Long appointmentId;
    private Long userId;
    private String patientName;
    private String department;
    private String doctorName;
    private LocalTime appointmentTime;
    private AppointmentStatus status;
    private String statusDescription;
    private boolean canCall;
    
    public static WaitingPatientDto from(Appointment appointment) {
        return WaitingPatientDto.builder()
                .appointmentId(appointment.getId())
                .userId(appointment.getUser().getId())
                .patientName(appointment.getUser().getName())
                .department(appointment.getDepartment())
                .doctorName(appointment.getDoctorName())
                .appointmentTime(appointment.getAppointmentTime())
                .status(appointment.getStatus())
                .statusDescription(appointment.getStatus().getDescription())
                .canCall(appointment.canBeCalled())
                .build();
    }
}
```

### 4.4 알림 도메인 (domain/notification/)

#### NotificationService.java
```java
package com.hospital.booking.domain.notification.service;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {
    
    private final FCMService fcmService;
    private final AppointmentService appointmentService;
    private final UserService userService;
    
    public void callPatient(Long appointmentId, String roomNumber) {
        Appointment appointment = appointmentService.getAppointmentById(appointmentId);
        User patient = appointment.getUser();
        
        // 호출 가능 상태 검증
        if (!appointment.canBeCalled()) {
            throw new BusinessException(ErrorCode.APPOINTMENT_CANNOT_BE_CALLED);
        }
        
        // FCM 토큰 존재 여부 검증
        if (!patient.hasFcmToken()) {
            throw new BusinessException(ErrorCode.FCM_TOKEN_NOT_FOUND);
        }
        
        // FCM 푸시 알림 전송
        boolean success = fcmService.sendCallNotification(
                patient.getFcmToken(), 
                patient.getName(), 
                roomNumber
        );
        
        if (!success) {
            throw new BusinessException(ErrorCode.FCM_SEND_FAILED);
        }
        
        // 예약 상태 변경
        appointmentService.markAppointmentAsCalled(appointmentId);
    }
}
```

#### NotificationController.java
```java
package com.hospital.booking.domain.notification.controller;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {
    
    private final NotificationService notificationService;
    private final UserService userService;
    private final AppointmentService appointmentService;
    
    /**
     * FCM 토큰 등록
     */
    @PostMapping("/fcm/register")
    public ResponseEntity<ApiResponse<String>> registerFcmToken(@Valid @RequestBody FCMTokenRequest request) {
        userService.registerFcmToken(request.getUserId(), request.getFcmToken());
        
        log.info("FCM token registered for user: {}", request.getUserId());
        return ResponseEntity.ok(ApiResponse.success("FCM 토큰이 성공적으로 등록되었습니다", "SUCCESS"));
    }
    
    /**
     * 환자 호출
     */
    @PostMapping("/call")
    public ResponseEntity<ApiResponse<String>> callPatient(@Valid @RequestBody PatientCallRequest request) {
        notificationService.callPatient(request.getAppointmentId(), request.getRoomNumber());
        
        Appointment appointment = appointmentService.getAppointmentById(request.getAppointmentId());
        String patientName = appointment.getUser().getName();
        
        log.info("Patient called successfully: {} (Appointment ID: {})", patientName, appointment.getId());
        
        return ResponseEntity.ok(ApiResponse.success(
                String.format("%s 환자에게 호출 알림을 전송했습니다", patientName),
                "SUCCESS"
        ));
    }
    
    /**
     * 대기 중인 환자 목록 조회
     */
    @GetMapping("/waiting-patients")
    public ResponseEntity<ApiResponse<List<WaitingPatientDto>>> getWaitingPatients() {
        List<Appointment> appointments = appointmentService.getTodayWaitingAppointments();
        
        List<WaitingPatientDto> patients = appointments.stream()
                .map(WaitingPatientDto::from)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success("대기 환자 목록 조회 성공", patients));
    }
}
```

### 4.5 FCM 서비스 (global/external/fcm/)

#### FCMService.java
```java
package com.hospital.booking.global.external.fcm;
```java
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 50)
    private String name;
    
    @Column(nullable = false, length = 20, unique = true)
    private String phone;
    
    @Column(columnDefinition = "TEXT")
    private String fcmToken;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<Appointment> appointments = new ArrayList<>();
    
    // 비즈니스 로직 메서드
    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
    
    public boolean hasFcmToken() {
        return fcmToken != null && !fcmToken.trim().isEmpty();
    }
    
    // 정적 팩토리 메서드
    public static User createUser(String name, String phone) {
        return User.builder()
                .name(name)
                .phone(phone)
                .build();
    }
}
```

### 4.2 Appointment.java
```java
@Entity
@Table(name = "appointments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, length = 100)
    private String hospitalName;
    
    @Column(nullable = false, length = 50)
    private String department;
    
    @Column(length = 50)
    private String doctorName;
    
    @Column(nullable = false)
    private LocalDate appointmentDate;
    
    @Column(nullable = false)
    private LocalTime appointmentTime;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.BOOKED;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    // 비즈니스 로직 메서드
    public void updateStatus(AppointmentStatus newStatus) {
        this.status = newStatus;
    }
    
    public void markAsArrived() {
        updateStatus(AppointmentStatus.ARRIVED);
    }
    
    public void markAsCalled() {
        updateStatus(AppointmentStatus.CALLED);
    }
    
    public void markAsInProgress() {
        updateStatus(AppointmentStatus.IN_PROGRESS);
    }
    
    public void markAsCompleted() {
        updateStatus(AppointmentStatus.COMPLETED);
    }
    
    public boolean canBeCalled() {
        return status == AppointmentStatus.ARRIVED;
    }
    
    public boolean isToday() {
        return appointmentDate.equals(LocalDate.now());
    }
    
    // 정적 팩토리 메서드
    public static Appointment createAppointment(User user, String hospitalName, String department, 
                                              String doctorName, LocalDate date, LocalTime time) {
        return Appointment.builder()
                .user(user)
                .hospitalName(hospitalName)
                .department(department)
                .doctorName(doctorName)
                .appointmentDate(date)
                .appointmentTime(time)
                .build();
    }
}
```

### 4.3 AppointmentStatus.java (Enum)
```java
public enum AppointmentStatus {
    BOOKED("예약됨"),
    ARRIVED("도착"),
    CALLED("호출됨"),
    IN_PROGRESS("진료중"),
    COMPLETED("진료완료"),
    CANCELLED("취소됨");
    
    private final String description;
    
    AppointmentStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
```

---

## 5. Firebase 설정 클래스

### 5.1 FirebaseConfig.java
```java
@Configuration
@Slf4j
public class FirebaseConfig {
    
    @Value("${firebase.service-account-key}")
    private String serviceAccountKeyPath;
    
    @Value("${firebase.project-id}")
    private String projectId;
    
    @PostConstruct
    public void initializeFirebase() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                // 서비스 계정 키 파일 로드
                InputStream serviceAccount = getClass()
                    .getClassLoader()
                    .getResourceAsStream("firebase-service-account.json");
                
                if (serviceAccount == null) {
                    throw new FileNotFoundException("Firebase service account key file not found in classpath");
                }
                
                FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setProjectId(projectId)
                    .build();
                
                FirebaseApp.initializeApp(options);
                log.info("Firebase application initialized successfully with project ID: {}", projectId);
            }
        } catch (Exception e) {
            log.error("Failed to initialize Firebase: {}", e.getMessage(), e);
            throw new RuntimeException("Firebase initialization failed", e);
        }
    }
}
```

---

## 6. FCM 서비스 클래스

### 6.1 FCMService.java
```java
@Service
@Slf4j
public class FCMService {
    
    /**
     * 단일 기기에 푸시 알림 전송
     */
    public boolean sendNotification(String fcmToken, String title, String body, Map<String, String> data) {
        try {
            // 메시지 빌더 생성
            Message.Builder messageBuilder = Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build())
                .setAndroid(AndroidConfig.builder()
                    .setNotification(AndroidNotification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .setIcon("ic_notification") // 앱의 알림 아이콘
                        .setColor("#FF0000") // 알림 색상
                        .setSound("default") // 알림음
                        .setPriority(AndroidNotification.Priority.HIGH) // 높은 우선순위
                        .build())
                    .build());
            
            // 추가 데이터가 있으면 포함
            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }
            
            Message message = messageBuilder.build();
            
            // FCM으로 메시지 전송
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Successfully sent FCM message: {}", response);
            
            return true;
            
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send FCM message to token: {}", fcmToken, e);
            
            // 토큰이 유효하지 않은 경우 로그 추가
            if (e.getErrorCode().equals("INVALID_ARGUMENT") || 
                e.getErrorCode().equals("UNREGISTERED")) {
                log.warn("Invalid FCM token detected: {}", fcmToken);
            }
            
            return false;
        } catch (Exception e) {
            log.error("Unexpected error while sending FCM message", e);
            return false;
        }
    }
    
    /**
     * 환자 호출 전용 알림
     */
    public boolean sendCallNotification(String fcmToken, String patientName, String roomNumber) {
        String title = "진료 호출";
        String body = String.format("%s님, %s로 들어오세요.", patientName, roomNumber);
        
        // 추가 데이터 (앱에서 특별한 처리를 위해)
        Map<String, String> data = new HashMap<>();
        data.put("type", "PATIENT_CALL");
        data.put("patient_name", patientName);
        data.put("room_number", roomNumber);
        data.put("timestamp", String.valueOf(System.currentTimeMillis()));
        
        return sendNotification(fcmToken, title, body, data);
    }
    
    /**
     * 다중 기기에 푸시 알림 전송 (필요시 사용)
     */
    public void sendNotificationToMultipleDevices(List<String> fcmTokens, String title, String body) {
        MulticastMessage message = MulticastMessage.builder()
            .addAllTokens(fcmTokens)
            .setNotification(Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build())
            .build();
        
        try {
            BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);
            log.info("Successfully sent {} messages, {} failures", 
                response.getSuccessCount(), response.getFailureCount());
                
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send multicast FCM message", e);
        }
    }
}
```

---

## 7. Repository 클래스

### 7.1 UserRepository.java
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhone(String phone);
    
    @Modifying
    @Query("UPDATE User u SET u.fcmToken = :fcmToken WHERE u.id = :userId")
    int updateFcmToken(@Param("userId") Long userId, @Param("fcmToken") String fcmToken);
}
```

### 7.2 AppointmentRepository.java
```java
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    List<Appointment> findByStatusOrderByAppointmentTimeAsc(AppointmentStatus status);
    
    List<Appointment> findByStatusInOrderByAppointmentTimeAsc(List<AppointmentStatus> statuses);
    
    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate = :date AND a.status IN :statuses ORDER BY a.appointmentTime ASC")
    List<Appointment> findTodayAppointmentsByStatus(
        @Param("date") LocalDate date, 
        @Param("statuses") List<AppointmentStatus> statuses
    );
    
    Optional<Appointment> findByIdAndStatus(Long id, AppointmentStatus status);
}
```

---

## 8. DTO 클래스

### 8.1 FCMTokenRequest.java
```java
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FCMTokenRequest {
    @NotNull(message = "사용자 ID는 필수입니다")
    private Long userId;
    
    @NotBlank(message = "FCM 토큰은 필수입니다")
    private String fcmToken;
}
```

### 8.2 PatientCallRequest.java
```java
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PatientCallRequest {
    @NotNull(message = "예약 ID는 필수입니다")
    private Long appointmentId;
    
    @Builder.Default
    private String roomNumber = "2번 진료실";
}
```

### 8.3 ApiResponse.java
```java
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("성공")
                .data(data)
                .build();
    }
    
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }
    
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(null)
                .build();
    }
}
```

---

## 9. 컨트롤러 클래스

### 9.1 FCMController.java
```java
@RestController
@RequestMapping("/api/fcm")
@Slf4j
@RequiredArgsConstructor
public class FCMController {
    
    private final FCMService fcmService;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    
    /**
     * FCM 토큰 등록/업데이트
     */
    @PostMapping("/register-token")
    public ResponseEntity<ApiResponse<String>> registerFCMToken(@Valid @RequestBody FCMTokenRequest request) {
        try {
            Optional<User> userOpt = userRepository.findById(request.getUserId());
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("사용자를 찾을 수 없습니다"));
            }
            
            User user = userOpt.get();
            user.updateFcmToken(request.getFcmToken());
            userRepository.save(user);
            
            log.info("FCM token registered for user: {} (ID: {})", user.getName(), user.getId());
            
            return ResponseEntity.ok(ApiResponse.success("FCM 토큰이 성공적으로 등록되었습니다", "SUCCESS"));
            
        } catch (Exception e) {
            log.error("Failed to register FCM token", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("FCM 토큰 등록에 실패했습니다"));
        }
    }
    
    /**
     * 환자 호출 (핵심 기능)
     */
    @PostMapping("/call-patient")
    public ResponseEntity<ApiResponse<String>> callPatient(@Valid @RequestBody PatientCallRequest request) {
        try {
            // 예약 정보 조회
            Optional<Appointment> appointmentOpt = appointmentRepository.findById(request.getAppointmentId());
            if (appointmentOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("예약 정보를 찾을 수 없습니다"));
            }
            
            Appointment appointment = appointmentOpt.get();
            User patient = appointment.getUser();
            
            // 환자가 도착한 상태인지 확인
            if (!appointment.canBeCalled()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("환자가 아직 도착하지 않았습니다"));
            }
            
            // FCM 토큰이 있는지 확인
            if (!patient.hasFcmToken()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("환자의 FCM 토큰이 없습니다"));
            }
            
            // 푸시 알림 전송
            boolean success = fcmService.sendCallNotification(
                patient.getFcmToken(), 
                patient.getName(), 
                request.getRoomNumber()
            );
            
            if (success) {
                // 예약 상태를 '호출됨'으로 변경
                appointment.markAsCalled();
                appointmentRepository.save(appointment);
                
                log.info("Patient called successfully: {} (Appointment ID: {})", 
                    patient.getName(), appointment.getId());
                
                return ResponseEntity.ok(ApiResponse.success(
                    String.format("%s 환자에게 호출 알림을 전송했습니다", patient.getName()),
                    "SUCCESS"
                ));
            } else {
                return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("푸시 알림 전송에 실패했습니다"));
            }
            
        } catch (Exception e) {
            log.error("Failed to call patient", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("환자 호출에 실패했습니다"));
        }
    }
    
    /**
     * 대기 중인 환자 목록 조회 (웹 관리자용)
     */
    @GetMapping("/waiting-patients")
    public ResponseEntity<ApiResponse<List<WaitingPatientDto>>> getWaitingPatients() {
        try {
            List<AppointmentStatus> waitingStatuses = Arrays.asList(
                AppointmentStatus.BOOKED, 
                AppointmentStatus.ARRIVED
            );
            
            List<Appointment> appointments = appointmentRepository
                .findTodayAppointmentsByStatus(LocalDate.now(), waitingStatuses);
            
            List<WaitingPatientDto> patients = appointments.stream()
                .map(WaitingPatientDto::from)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success("대기 환자 목록 조회 성공", patients));
            
        } catch (Exception e) {
            log.error("Failed to get waiting patients", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("대기 환자 목록 조회에 실패했습니다"));
        }
    }
}
```

### 9.2 WaitingPatientDto.java
```java
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class WaitingPatientDto {
    private Long appointmentId;
    private Long userId;
    private String patientName;
    private String department;
    private String doctorName;
    private LocalTime appointmentTime;
    private AppointmentStatus status;
    private String statusDescription;
    private boolean canCall; // 호출 가능 여부
    
    public static WaitingPatientDto from(Appointment appointment) {
        return WaitingPatientDto.builder()
                .appointmentId(appointment.getId())
                .userId(appointment.getUser().getId())
                .patientName(appointment.getUser().getName())
                .department(appointment.getDepartment())
                .doctorName(appointment.getDoctorName())
                .appointmentTime(appointment.getAppointmentTime())
                .status(appointment.getStatus())
                .statusDescription(appointment.getStatus().getDescription())
                .canCall(appointment.canBeCalled())
                .build();
    }
}
```

---

## 10. 테스트 방법

### 10.1 Postman으로 API 테스트

#### FCM 토큰 등록
```http
POST http://localhost:8080/api/fcm/register-token
Content-Type: application/json

{
    "userId": 1,
    "fcmToken": "실제FCM토큰값"
}
```

#### 환자 호출
```http
POST http://localhost:8080/api/fcm/call-patient
Content-Type: application/json

{
    "appointmentId": 1,
    "roomNumber": "2번 진료실"
}
```

#### 대기 환자 목록 조회
```http
GET http://localhost:8080/api/fcm/waiting-patients
```

### 10.2 테스트용 더미 데이터 생성 (선택사항)
```java
@Component
@Profile("dev") // 개발 환경에서만 실행
@RequiredArgsConstructor
public class TestDataInitializer {
    
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    
    @PostConstruct
    public void initData() {
        // 테스트 사용자 생성
        User testUser = User.builder()
                .name("김환자")
                .phone("010-1234-5678")
                .fcmToken("test_fcm_token_here")
                .build();
        userRepository.save(testUser);
        
        // 테스트 예약 생성
        Appointment appointment = Appointment.builder()
                .user(testUser)
                .hospitalName("서울대학교병원")
                .department("내과")
                .doctorName("홍길동")
                .appointmentDate(LocalDate.now())
                .appointmentTime(LocalTime.of(10, 30))
                .status(AppointmentStatus.ARRIVED) // 도착 상태로 설정
                .build();
        appointmentRepository.save(appointment);
    }
}
```

---

## 11. 주요 주의사항

### 11.1 보안
- Firebase 서비스 계정 키 파일을 절대 Git에 커밋하지 말 것
- `.gitignore`에 `firebase-service-account.json` 추가
- 운영 환경에서는 환경변수나 외부 볼트 사용 권장

### 11.2 에러 처리
- FCM 토큰 만료시 자동 처리 로직 필요
- 네트워크 실패시 재시도 로직 고려
- 잘못된 토큰 감지시 데이터베이스에서 제거

### 11.3 성능 최적화
- 대량 푸시 전송시 배치 처리 사용
- 데이터베이스 인덱스 설정 (fcm_token, status 컬럼)
- FCM 요청 제한 고려 (초당 600,000개)

## 추가 설정 및 팁

### 개발 환경 설정
**application-dev.properties** (개발용)
```properties
# MariaDB 개발환경
spring.datasource.url=jdbc:mariadb://localhost:3306/hospital_booking_dev
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# 개발용 Firebase 프로젝트
firebase.project-id=hospital-booking-system-dev

# 로그 레벨 상세 설정
logging.level.root=INFO
logging.level.com.hospital.booking=DEBUG
logging.level.org.springframework.web=DEBUG
```

**application-prod.properties** (운영용)
```properties
# MariaDB 운영환경
spring.datasource.url=jdbc:mariadb://prod-server:3306/hospital_booking
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# 운영용 Firebase 프로젝트
firebase.project-id=hospital-booking-system-prod

# 운영 로그 설정
logging.level.root=WARN
logging.level.com.hospital.booking=INFO
```

### Gradle 추가 설정
**build.gradle에 추가할 수 있는 유용한 설정들:**
```gradle
// Java 버전 설정
java {
    sourceCompatibility = '11'
}

// 빌드 시 테스트 스킵 (개발 중)
test {
    useJUnitPlatform()
}

// JAR 파일 이름 설정
jar {
    archiveBaseName = 'hospital-booking-api'
    archiveVersion = '1.0.0'
}
```

### .gitignore 필수 설정
```gitignore
# Firebase 보안 파일
firebase-service-account.json
**/firebase-service-account*.json

# 빌드 파일
build/
.gradle/

# IDE 파일
.idea/
*.iml
.vscode/

# 로그 파일
*.log
logs/
```