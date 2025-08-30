# Spring Boot FCM 구현 완전 가이드

## 1. 사전 준비 단계

### 1.1 Firebase 프로젝트 설정
1. **Firebase Console 접속**
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
    implementation 'mysql:mysql-connector-java:8.0.33'
    
    // FCM Admin SDK
    implementation 'com.google.firebase:firebase-admin:9.2.0'
    
    // Validation
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    
    // Lombok (선택사항)
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    
    // 테스트
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

### 2.2 application.properties 설정
```properties
# 데이터베이스 설정
spring.datasource.url=jdbc:mysql://localhost:3306/hospital_booking
spring.datasource.username=root
spring.datasource.password=your-password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA 설정
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
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

### 4.1 User.java
```java
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
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
    
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Appointment> appointments = new ArrayList<>();
}
```

### 4.2 Appointment.java
```java
@Entity
@Table(name = "appointments")
@Data
@NoArgsConstructor
@AllArgsConstructor
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
    private AppointmentStatus status = AppointmentStatus.BOOKED;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
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
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FCMTokenRequest {
    @NotNull(message = "사용자 ID는 필수입니다")
    private Long userId;
    
    @NotBlank(message = "FCM 토큰은 필수입니다")
    private String fcmToken;
}
```

### 8.2 PatientCallRequest.java
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientCallRequest {
    @NotNull(message = "예약 ID는 필수입니다")
    private Long appointmentId;
    
    private String roomNumber = "2번 진료실"; // 기본값
}
```

### 8.3 ApiResponse.java
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "성공", data);
    }
    
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }
    
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
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
            user.setFcmToken(request.getFcmToken());
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
            if (appointment.getStatus() != AppointmentStatus.ARRIVED) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("환자가 아직 도착하지 않았습니다"));
            }
            
            // FCM 토큰이 있는지 확인
            if (patient.getFcmToken() == null || patient.getFcmToken().isEmpty()) {
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
                appointment.setStatus(AppointmentStatus.CALLED);
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
@Data
@NoArgsConstructor
@AllArgsConstructor
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
        WaitingPatientDto dto = new WaitingPatientDto();
        dto.setAppointmentId(appointment.getId());
        dto.setUserId(appointment.getUser().getId());
        dto.setPatientName(appointment.getUser().getName());
        dto.setDepartment(appointment.getDepartment());
        dto.setDoctorName(appointment.getDoctorName());
        dto.setAppointmentTime(appointment.getAppointmentTime());
        dto.setStatus(appointment.getStatus());
        dto.setStatusDescription(appointment.getStatus().getDescription());
        dto.setCanCall(appointment.getStatus() == AppointmentStatus.ARRIVED);
        
        return dto;
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
public class TestDataInitializer {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @PostConstruct
    public void initData() {
        // 테스트 사용자 생성
        User testUser = new User();
        testUser.setName("김환자");
        testUser.setPhone("010-1234-5678");
        testUser.setFcmToken("test_fcm_token_here");
        userRepository.save(testUser);
        
        // 테스트 예약 생성
        Appointment appointment = new Appointment();
        appointment.setUser(testUser);
        appointment.setHospitalName("서울대학교병원");
        appointment.setDepartment("내과");
        appointment.setDoctorName("홍길동");
        appointment.setAppointmentDate(LocalDate.now());
        appointment.setAppointmentTime(LocalTime.of(10, 30));
        appointment.setStatus(AppointmentStatus.ARRIVED); // 도착 상태로 설정
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
# 개발환경 데이터베이스
spring.datasource.url=jdbc:mysql://localhost:3306/hospital_booking_dev
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
# 운영환경 데이터베이스
spring.datasource.url=jdbc:mysql://prod-server:3306/hospital_booking
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