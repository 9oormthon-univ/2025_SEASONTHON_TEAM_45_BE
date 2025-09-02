# CareFreePass 누락된 API 구현 가이드 🚀

## 📋 개요

현재 CareFreePass 시스템에서 **누락된 중요한 API**들을 정리하고, 구현을 위한 가이드를 제공합니다.  
특히 **환자용 예약 조회 API**가 완전히 누락되어 있어 모바일 앱 개발에 필수적인 API들을 추가해야 합니다.

---

## 🚨 누락된 핵심 API 목록

### 📱 환자용 예약 조회 API (긴급도: 🔴 매우 높음)

| API | 엔드포인트 | 설명 | 우선순위 |
|-----|------------|------|----------|
| 내 전체 예약 목록 | `GET /api/v1/appointments/my` | 환자 본인의 모든 예약 내역 조회 | 🔴 필수 |
| 오늘 내 예약 조회 | `GET /api/v1/appointments/my/today` | 오늘 예정된 본인 예약만 조회 | 🔴 필수 |
| 특정 예약 상세 조회 | `GET /api/v1/appointments/{id}` | 특정 예약의 상세 정보 조회 | 🟡 권장 |
| 내 예약 통계 조회 | `GET /api/v1/appointments/my/statistics` | 예약 통계 및 분석 정보 | 🟢 선택 |

---

## 🛠️ 구현 가이드

### 1. AppointmentController 수정 📝

**파일**: `src/main/java/.../appointment/controller/AppointmentController.java`

```java
@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    // 1. 내 전체 예약 목록 조회 (필수)
    @GetMapping("/my")
    @Operation(summary = "내 전체 예약 목록 조회", description = "환자 본인의 모든 예약 내역을 조회합니다.")
    public ApiResponseTemplate<List<AppointmentResponse>> getMyAppointments(
            @RequestParam @Parameter(description = "환자 ID") Long memberId,
            @RequestParam(required = false) @Parameter(description = "예약 상태 필터") String status,
            @RequestParam(required = false) @Parameter(description = "날짜 필터 (YYYY-MM-DD)") String date) {
        
        List<AppointmentResponse> appointments = appointmentService.getAppointmentsByMember(memberId, status, date);
        return ApiResponseTemplate.success(ResponseCode.APPOINTMENT_SUCCESS, appointments);
    }

    // 2. 오늘 내 예약 조회 (필수)
    @GetMapping("/my/today")
    @Operation(summary = "오늘 내 예약 조회", description = "환자가 오늘 예정된 본인의 예약만 조회합니다.")
    public ApiResponseTemplate<List<AppointmentResponse>> getMyTodayAppointments(
            @RequestParam @Parameter(description = "환자 ID") Long memberId) {
        
        List<AppointmentResponse> todayAppointments = appointmentService.getTodayAppointmentsByMember(memberId);
        return ApiResponseTemplate.success(ResponseCode.APPOINTMENT_TODAY_SUCCESS, todayAppointments);
    }

    // 3. 특정 예약 상세 조회 (권장)
    @GetMapping("/{appointmentId}")
    @Operation(summary = "특정 예약 상세 조회", description = "특정 예약의 상세 정보를 조회합니다.")
    public ApiResponseTemplate<AppointmentResponse> getAppointment(
            @PathVariable @Parameter(description = "예약 ID") Long appointmentId,
            @RequestParam @Parameter(description = "환자 ID (본인 확인용)") Long memberId) {
        
        AppointmentResponse appointment = appointmentService.getAppointmentDetail(appointmentId, memberId);
        return ApiResponseTemplate.success(ResponseCode.APPOINTMENT_DETAIL_SUCCESS, appointment);
    }

    // 4. 내 예약 통계 조회 (선택)
    @GetMapping("/my/statistics")
    @Operation(summary = "내 예약 통계 조회", description = "환자의 예약 통계 정보를 제공합니다.")
    public ApiResponseTemplate<AppointmentStatisticsResponse> getMyAppointmentStatistics(
            @RequestParam @Parameter(description = "환자 ID") Long memberId) {
        
        AppointmentStatisticsResponse statistics = appointmentService.getAppointmentStatistics(memberId);
        return ApiResponseTemplate.success(ResponseCode.APPOINTMENT_STATISTICS_SUCCESS, statistics);
    }
}
```

### 2. AppointmentService 수정 📝

**파일**: `src/main/java/.../appointment/service/AppointmentService.java`

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final MemberRepository memberRepository;

    // 1. 환자별 예약 목록 조회 (필터링 포함)
    public List<AppointmentResponse> getAppointmentsByMember(Long memberId, String status, String date) {
        // 환자 존재 여부 확인
        if (!memberRepository.existsById(memberId)) {
            throw new MemberNotFoundException("존재하지 않는 회원입니다.");
        }

        List<Appointment> appointments;

        if (status != null && date != null) {
            // 상태 + 날짜 필터링
            LocalDate filterDate = LocalDate.parse(date);
            AppointmentStatus appointmentStatus = AppointmentStatus.valueOf(status);
            appointments = appointmentRepository.findByMemberIdAndStatusAndAppointmentDate(
                memberId, appointmentStatus, filterDate);
        } else if (status != null) {
            // 상태만 필터링
            AppointmentStatus appointmentStatus = AppointmentStatus.valueOf(status);
            appointments = appointmentRepository.findByMemberIdAndStatus(memberId, appointmentStatus);
        } else if (date != null) {
            // 날짜만 필터링
            LocalDate filterDate = LocalDate.parse(date);
            appointments = appointmentRepository.findByMemberIdAndAppointmentDate(memberId, filterDate);
        } else {
            // 전체 조회 (최신순)
            appointments = appointmentRepository.findByMemberIdOrderByAppointmentDateDescAppointmentTimeDesc(memberId);
        }

        return appointments.stream()
                .map(AppointmentResponse::from)
                .collect(Collectors.toList());
    }

    // 2. 오늘 예약 조회
    public List<AppointmentResponse> getTodayAppointmentsByMember(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new MemberNotFoundException("존재하지 않는 회원입니다.");
        }

        LocalDate today = LocalDate.now();
        List<Appointment> todayAppointments = appointmentRepository.findByMemberIdAndAppointmentDate(memberId, today);

        return todayAppointments.stream()
                .map(appointment -> {
                    AppointmentResponse response = AppointmentResponse.from(appointment);
                    // 오늘 예약이므로 체크인 가능
                    response.setCanCheckin(appointment.getStatus() == AppointmentStatus.WAITING_BEFORE_ARRIVAL 
                                          || appointment.getStatus() == AppointmentStatus.BOOKED);
                    // 예약까지 남은 시간 계산
                    response.setTimeUntilAppointment(calculateTimeUntilAppointment(appointment));
                    return response;
                })
                .collect(Collectors.toList());
    }

    // 3. 예약 상세 조회 (권한 확인)
    public AppointmentResponse getAppointmentDetail(Long appointmentId, Long memberId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("존재하지 않는 예약입니다."));

        // 본인 예약인지 확인
        if (!appointment.getMember().getId().equals(memberId)) {
            throw new UnauthorizedException("본인의 예약만 조회할 수 있습니다.");
        }

        AppointmentResponse response = AppointmentResponse.from(appointment);
        
        // 수정/취소 가능 여부 설정
        response.setCanModify(isModifiable(appointment));
        response.setCanCancel(isCancellable(appointment));
        response.setCanCheckin(isCheckinable(appointment));

        return response;
    }

    // 4. 예약 통계 조회
    public AppointmentStatisticsResponse getAppointmentStatistics(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new MemberNotFoundException("존재하지 않는 회원입니다.");
        }

        List<Appointment> allAppointments = appointmentRepository.findByMemberId(memberId);

        long totalCount = allAppointments.size();
        long completedCount = allAppointments.stream()
                .filter(apt -> apt.getStatus() == AppointmentStatus.COMPLETED)
                .count();
        long cancelledCount = allAppointments.stream()
                .filter(apt -> apt.getStatus() == AppointmentStatus.CANCELLED)
                .count();
        long upcomingCount = allAppointments.stream()
                .filter(apt -> apt.getAppointmentDate().isAfter(LocalDate.now()) || 
                              (apt.getAppointmentDate().equals(LocalDate.now()) && 
                               apt.getStatus() != AppointmentStatus.COMPLETED &&
                               apt.getStatus() != AppointmentStatus.CANCELLED))
                .count();

        // 가장 많이 방문한 병원/진료과 계산
        String favoriteHospital = allAppointments.stream()
                .collect(Collectors.groupingBy(apt -> apt.getHospital().getName(), Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("없음");

        String favoriteDepartment = allAppointments.stream()
                .collect(Collectors.groupingBy(Appointment::getDepartmentName, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("없음");

        LocalDate lastAppointmentDate = allAppointments.stream()
                .filter(apt -> apt.getStatus() == AppointmentStatus.COMPLETED)
                .map(Appointment::getAppointmentDate)
                .max(LocalDate::compareTo)
                .orElse(null);

        return AppointmentStatisticsResponse.builder()
                .totalAppointments(totalCount)
                .completedAppointments(completedCount)
                .cancelledAppointments(cancelledCount)
                .upcomingAppointments(upcomingCount)
                .favoriteHospital(favoriteHospital)
                .favoriteDepartment(favoriteDepartment)
                .lastAppointmentDate(lastAppointmentDate)
                .build();
    }

    // 헬퍼 메서드들
    private String calculateTimeUntilAppointment(Appointment appointment) {
        LocalDateTime appointmentDateTime = LocalDateTime.of(
            appointment.getAppointmentDate(), 
            appointment.getAppointmentTime()
        );
        LocalDateTime now = LocalDateTime.now();
        
        if (appointmentDateTime.isBefore(now)) {
            return "예약 시간 지남";
        }
        
        Duration duration = Duration.between(now, appointmentDateTime);
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        
        return String.format("%d시간 %d분 후", hours, minutes);
    }

    private boolean isModifiable(Appointment appointment) {
        // 예약 시간 2시간 전까지 수정 가능
        LocalDateTime appointmentDateTime = LocalDateTime.of(
            appointment.getAppointmentDate(),
            appointment.getAppointmentTime()
        );
        return LocalDateTime.now().isBefore(appointmentDateTime.minusHours(2)) &&
               appointment.getStatus() != AppointmentStatus.COMPLETED &&
               appointment.getStatus() != AppointmentStatus.CANCELLED;
    }

    private boolean isCancellable(Appointment appointment) {
        // 예약 시간 1시간 전까지 취소 가능
        LocalDateTime appointmentDateTime = LocalDateTime.of(
            appointment.getAppointmentDate(),
            appointment.getAppointmentTime()
        );
        return LocalDateTime.now().isBefore(appointmentDateTime.minusHours(1)) &&
               appointment.getStatus() != AppointmentStatus.COMPLETED &&
               appointment.getStatus() != AppointmentStatus.CANCELLED;
    }

    private boolean isCheckinable(Appointment appointment) {
        // 예약 당일에만 체크인 가능
        return appointment.getAppointmentDate().equals(LocalDate.now()) &&
               (appointment.getStatus() == AppointmentStatus.WAITING_BEFORE_ARRIVAL ||
                appointment.getStatus() == AppointmentStatus.BOOKED);
    }
}
```

### 3. AppointmentRepository 수정 📝

**파일**: `src/main/java/.../appointment/repository/AppointmentRepository.java`

```java
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // 기존 메서드들...

    // 환자별 예약 조회 메서드들 추가
    List<Appointment> findByMemberIdOrderByAppointmentDateDescAppointmentTimeDesc(Long memberId);
    
    List<Appointment> findByMemberIdAndStatus(Long memberId, AppointmentStatus status);
    
    List<Appointment> findByMemberIdAndAppointmentDate(Long memberId, LocalDate date);
    
    List<Appointment> findByMemberIdAndStatusAndAppointmentDate(
        Long memberId, AppointmentStatus status, LocalDate date);
    
    List<Appointment> findByMemberId(Long memberId);

    // 통계용 메서드들
    long countByMemberIdAndStatus(Long memberId, AppointmentStatus status);
    
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.member.id = :memberId " +
           "AND (a.appointmentDate > CURRENT_DATE OR " +
           "(a.appointmentDate = CURRENT_DATE AND a.status NOT IN ('COMPLETED', 'CANCELLED')))")
    long countUpcomingAppointmentsByMemberId(@Param("memberId") Long memberId);
}
```

### 4. 새로운 Response DTO 생성 📝

**파일**: `src/main/java/.../appointment/dto/response/AppointmentStatisticsResponse.java`

```java
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "환자 예약 통계 응답")
public class AppointmentStatisticsResponse {
    
    @Schema(description = "총 예약 횟수")
    private Long totalAppointments;
    
    @Schema(description = "완료된 예약 수")
    private Long completedAppointments;
    
    @Schema(description = "취소된 예약 수")
    private Long cancelledAppointments;
    
    @Schema(description = "예정된 예약 수")
    private Long upcomingAppointments;
    
    @Schema(description = "가장 많이 방문한 병원")
    private String favoriteHospital;
    
    @Schema(description = "가장 많이 이용한 진료과")
    private String favoriteDepartment;
    
    @Schema(description = "마지막 예약 날짜")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate lastAppointmentDate;
}
```

### 5. AppointmentResponse 수정 📝

기존 `AppointmentResponse`에 다음 필드들을 추가해야 합니다:

```java
@Getter
@Setter // Setter 추가 필요
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "예약 응답")
public class AppointmentResponse {
    
    // 기존 필드들...
    
    @Schema(description = "체크인 가능 여부")
    private Boolean canCheckin;
    
    @Schema(description = "수정 가능 여부")
    private Boolean canModify;
    
    @Schema(description = "취소 가능 여부")
    private Boolean canCancel;
    
    @Schema(description = "예약까지 남은 시간 (오늘 예약인 경우)")
    private String timeUntilAppointment;
    
    @Schema(description = "병원 주소")
    private String hospitalAddress;
    
    @Schema(description = "병원 전화번호")
    private String hospitalPhone;
    
    // from 메서드도 수정 필요
    public static AppointmentResponse from(Appointment appointment) {
        return AppointmentResponse.builder()
                // 기존 필드 매핑...
                .hospitalAddress(appointment.getHospital().getAddress())
                .hospitalPhone(appointment.getHospital().getPhoneNumber())
                .canCheckin(false) // 기본값, 서비스에서 설정
                .canModify(false)  // 기본값, 서비스에서 설정  
                .canCancel(false)  // 기본값, 서비스에서 설정
                .build();
    }
}
```

### 6. ResponseCode 추가 📝

**파일**: `src/main/java/.../global/response/ResponseCode.java`

```java
public enum ResponseCode {
    // 기존 코드들...
    
    // 환자 예약 조회 관련 응답 코드 추가
    APPOINTMENT_SUCCESS("APPOINTMENT_SUCCESS", "내 예약 목록 조회가 완료되었습니다."),
    APPOINTMENT_TODAY_SUCCESS("APPOINTMENT_TODAY_SUCCESS", "오늘 내 예약 조회가 완료되었습니다."),
    APPOINTMENT_DETAIL_SUCCESS("APPOINTMENT_DETAIL_SUCCESS", "예약 상세 조회가 완료되었습니다."),
    APPOINTMENT_STATISTICS_SUCCESS("APPOINTMENT_STATISTICS_SUCCESS", "내 예약 통계 조회가 완료되었습니다.");
    
    // 생성자 등은 기존과 동일...
}
```

---

## 🧪 테스트 가이드

### 1. 단위 테스트 작성 📝

```java
@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    
    @Mock
    private MemberRepository memberRepository;
    
    @InjectMocks
    private AppointmentService appointmentService;

    @Test
    @DisplayName("환자 본인의 예약 목록 조회 성공")
    void getMyAppointments_Success() {
        // Given
        Long memberId = 1L;
        Member member = createTestMember(memberId);
        List<Appointment> appointments = createTestAppointments(member);
        
        when(memberRepository.existsById(memberId)).thenReturn(true);
        when(appointmentRepository.findByMemberIdOrderByAppointmentDateDescAppointmentTimeDesc(memberId))
                .thenReturn(appointments);

        // When
        List<AppointmentResponse> result = appointmentService.getAppointmentsByMember(memberId, null, null);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getMemberName()).isEqualTo("김환자");
    }

    @Test
    @DisplayName("존재하지 않는 회원 ID로 조회 시 예외 발생")
    void getMyAppointments_MemberNotFound() {
        // Given
        Long memberId = 999L;
        when(memberRepository.existsById(memberId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> appointmentService.getAppointmentsByMember(memberId, null, null))
                .isInstanceOf(MemberNotFoundException.class)
                .hasMessage("존재하지 않는 회원입니다.");
    }
}
```

### 2. 통합 테스트 작성 📝

```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class AppointmentIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private AppointmentRepository appointmentRepository;

    @Test
    @DisplayName("내 예약 목록 조회 API 테스트")
    void getMyAppointments_Integration() {
        // Given: 테스트 데이터 준비
        Long memberId = 1L;
        
        // When: API 호출
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/appointments/my?memberId=" + memberId, String.class);
        
        // Then: 응답 검증
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        // JSON 응답 파싱 및 검증...
    }
}
```

---

## 🚀 배포 및 릴리즈 체크리스트

### ✅ 개발 완료 체크리스트

- [ ] **Controller 메서드 구현** (4개 API)
- [ ] **Service 로직 구현** (비즈니스 로직 포함)
- [ ] **Repository 메서드 추가** (JPA 쿼리 메서드)
- [ ] **Response DTO 생성/수정**
- [ ] **ResponseCode 추가**
- [ ] **단위 테스트 작성** (90% 이상 커버리지)
- [ ] **통합 테스트 작성**
- [ ] **Swagger 문서 자동 생성 확인**
- [ ] **예외 처리 완료**
- [ ] **보안 검토** (권한 체크)

### ✅ 테스트 체크리스트

- [ ] **정상 케이스 테스트**
- [ ] **경계값 테스트**
- [ ] **예외 상황 테스트**
- [ ] **권한 체크 테스트**
- [ ] **성능 테스트** (대량 데이터)
- [ ] **동시성 테스트**

### ✅ 문서화 체크리스트

- [ ] **API 명세서 업데이트**
- [ ] **모바일용 명세서 업데이트** ✅ (완료)
- [ ] **Swagger UI 동작 확인**
- [ ] **개발자 가이드 작성** ✅ (완료)

---

## 📱 모바일 앱 연동 가이드

### iOS (Swift) 예시 코드

```swift
// 내 예약 목록 조회
func fetchMyAppointments(memberId: Int, completion: @escaping (Result<[Appointment], Error>) -> Void) {
    let url = URL(string: "\(API_BASE_URL)/api/v1/appointments/my?memberId=\(memberId)")!
    var request = URLRequest(url: url)
    request.setValue("Bearer \(accessToken)", forHTTPHeaderField: "Authorization")
    
    URLSession.shared.dataTask(with: request) { data, response, error in
        // 응답 처리 로직...
    }.resume()
}
```

### Android (Kotlin) 예시 코드

```kotlin
// Retrofit Interface
interface AppointmentApi {
    @GET("/api/v1/appointments/my")
    suspend fun getMyAppointments(
        @Query("memberId") memberId: Long,
        @Query("status") status: String? = null,
        @Query("date") date: String? = null
    ): ApiResponse<List<AppointmentResponse>>
}

// Usage
viewModelScope.launch {
    try {
        val response = appointmentApi.getMyAppointments(memberId)
        if (response.isSuccessful) {
            _appointments.value = response.data
        }
    } catch (e: Exception) {
        _error.value = e.message
    }
}
```

---

## 📞 개발 지원

### 구현 중 문의사항이 있을 때:
1. **GitHub Issues**: 기술적 질문 및 버그 리포트
2. **Slack**: 실시간 개발 지원 
3. **코드 리뷰**: Pull Request를 통한 코드 검토

### 추가 기능 제안:
- 예약 취소 API (`DELETE /api/v1/appointments/my/{appointmentId}`)
- 예약 알림 설정 API (`PUT /api/v1/appointments/{appointmentId}/notifications`)
- 병원 즐겨찾기 기능 (`POST /api/v1/favorites/hospitals/{hospitalId}`)

---

**우선순위**: 🔴 필수 API 4개를 먼저 구현 후, 🟡🟢 단계별 확장 권장  
**예상 개발 시간**: 2-3일 (테스트 포함)  
**검토자**: 백엔드 개발팀 리더