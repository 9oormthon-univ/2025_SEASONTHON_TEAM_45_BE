# API 변경사항 가이드

## 📡 개요

예약 시스템 간소화로 인한 API 변경사항을 정리한 문서입니다. 기존 클라이언트 앱이나 프론트엔드에서 API를 사용하는 경우 이 문서를 참고하여 업데이트해주세요.

## 🔄 변경된 API 엔드포인트

### 1. 예약 생성 API

#### `POST /api/v1/appointments`

**🔴 이전 요청 형식**:
```json
{
  "memberId": 1,
  "hospitalName": "서울대병원",
  "doctorName": "김의사",           // ❌ 제거됨
  "department": "정형외과",
  "roomNumber": "201호",            // ❌ 제거됨
  "appointmentDate": "2025-09-01",
  "appointmentTime": "10:30"
}
```

**🟢 현재 요청 형식**:
```json
{
  "memberId": 1,
  "hospitalName": "서울대병원",
  "department": "정형외과",
  "appointmentDate": "2025-09-01",
  "appointmentTime": "10:30"
}
```

**응답 형식 (동일)**:
```json
{
  "code": "APPOINTMENT_2001",
  "message": "예약이 성공적으로 생성되었습니다.",
  "data": 123
}
```

#### 마이그레이션 예시

**React/JavaScript 클라이언트**:
```javascript
// ❌ 이전 코드
const createAppointment = async (appointmentData) => {
  const response = await fetch('/api/v1/appointments', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      memberId: appointmentData.memberId,
      hospitalName: appointmentData.hospitalName,
      doctorName: appointmentData.doctorName,     // 제거 필요
      department: appointmentData.department,
      roomNumber: appointmentData.roomNumber,     // 제거 필요
      appointmentDate: appointmentData.date,
      appointmentTime: appointmentData.time
    })
  });
};

// ✅ 새로운 코드
const createAppointment = async (appointmentData) => {
  const response = await fetch('/api/v1/appointments', {
    method: 'POST', 
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      memberId: appointmentData.memberId,
      hospitalName: appointmentData.hospitalName,  // 기본값: "서울대병원"
      department: appointmentData.department,
      appointmentDate: appointmentData.date,
      appointmentTime: appointmentData.time
    })
  });
};
```

**Android (Kotlin) 클라이언트**:
```kotlin
// ❌ 이전 데이터 클래스
data class AppointmentRequest(
    val memberId: Long,
    val hospitalName: String,
    val doctorName: String,        // 제거 필요
    val department: String,
    val roomNumber: String,        // 제거 필요
    val appointmentDate: String,
    val appointmentTime: String
)

// ✅ 새로운 데이터 클래스
data class AppointmentRequest(
    val memberId: Long,
    val hospitalName: String = "서울대병원",  // 기본값 설정
    val department: String,
    val appointmentDate: String,
    val appointmentTime: String
)
```

### 2. 예약 수정 API

#### `PUT /api/v1/appointments/{id}`

**🔴 이전 요청 형식**:
```json
{
  "hospitalName": "서울대병원",
  "doctorName": "이의사",           // ❌ 제거됨
  "department": "내과", 
  "roomNumber": "101호",            // ❌ 제거됨
  "appointmentDate": "2025-09-02",
  "appointmentTime": "14:00"
}
```

**🟢 현재 요청 형식**:
```json
{
  "hospitalName": "서울대병원",
  "department": "내과",
  "appointmentDate": "2025-09-02", 
  "appointmentTime": "14:00"
}
```

### 3. 예약 조회 API 응답 변경

#### `GET /api/v1/appointments/today`

**🔴 이전 응답 형식**:
```json
{
  "code": "APPOINTMENT_2001",
  "message": "오늘 예약 목록을 조회했습니다.",
  "data": [
    {
      "appointmentId": 1,
      "memberName": "김환자",
      "hospitalName": "서울대병원",
      "doctorName": "김의사",          // ❌ 제거됨
      "department": "정형외과",
      "roomNumber": "201호",           // ❌ 제거됨
      "appointmentDate": "2025-08-31",
      "appointmentTime": "10:30",
      "status": "BOOKED",
      "statusDescription": "예약됨",
      "canCall": true
    }
  ]
}
```

**🟢 현재 응답 형식**:
```json
{
  "code": "APPOINTMENT_2001", 
  "message": "오늘 예약 목록을 조회했습니다.",
  "data": [
    {
      "appointmentId": 1,
      "memberName": "김환자",
      "hospitalName": "서울대병원",
      "department": "정형외과",
      "appointmentDate": "2025-08-31",
      "appointmentTime": "10:30",
      "status": "BOOKED",
      "statusDescription": "예약됨", 
      "canCall": true
    }
  ]
}
```

### 4. 예약 가능 시간 조회 API

#### 🔴 제거된 엔드포인트
```http
GET /api/v1/appointments/available-times/doctor?doctorName=김의사&date=2025-09-01
```

#### 🟢 새로운 엔드포인트
```http
GET /api/v1/appointments/available-times/department?department=정형외과&appointmentDate=2025-09-01
```

**응답 형식 (동일)**:
```json
{
  "code": "APPOINTMENT_2001",
  "message": "예약 가능한 시간을 조회했습니다.",
  "data": [
    "09:00", "09:30", "10:00", "10:30", "11:00",
    "14:00", "14:30", "15:00", "15:30", "16:00"
  ]
}
```

#### 마이그레이션 예시
```javascript
// ❌ 이전 코드
const getAvailableTimes = async (doctorName, date) => {
  const response = await fetch(
    `/api/v1/appointments/available-times/doctor?doctorName=${doctorName}&date=${date}`
  );
  return response.json();
};

// ✅ 새로운 코드
const getAvailableTimes = async (department, appointmentDate) => {
  const response = await fetch(
    `/api/v1/appointments/available-times/department?department=${department}&appointmentDate=${appointmentDate}`
  );
  return response.json();
};
```

## 🔧 환자 호출 API (변경 없음)

### `POST /api/v1/notifications/call`

**요청 형식 (동일)**:
```json
{
  "appointmentId": 1,
  "roomNumber": "2번 진료실"  // 사용자 지정 진료실 (선택사항)
}
```

**응답 형식 (동일)**:
```json
{
  "code": "NOTIFICATION_2001",
  "message": "환자 호출이 완료되었습니다.",
  "data": null
}
```

> **참고**: 진료실 번호는 여전히 환자 호출 시 사용자가 지정할 수 있습니다. 다만 예약 데이터베이스에는 저장되지 않습니다.

## 📱 모바일 앱 업데이트 가이드

### Android 앱

#### 1. 데이터 모델 업데이트
```kotlin
// models/AppointmentResponse.kt
data class AppointmentResponse(
    val appointmentId: Long,
    val memberName: String,
    val hospitalName: String,
    // val doctorName: String,        // ❌ 제거
    val department: String,
    // val roomNumber: String,        // ❌ 제거  
    val appointmentDate: String,
    val appointmentTime: String,
    val status: String,
    val statusDescription: String,
    val canCall: Boolean
)
```

#### 2. API 서비스 업데이트
```kotlin
// api/AppointmentService.kt
interface AppointmentService {
    @POST("appointments")
    suspend fun createAppointment(
        @Body request: AppointmentCreateRequest
    ): Response<ApiResponse<Long>>
    
    // ❌ 제거된 메서드
    // @GET("appointments/available-times/doctor")
    // suspend fun getAvailableTimesByDoctor(...)
    
    // ✅ 새로운 메서드
    @GET("appointments/available-times/department")
    suspend fun getAvailableTimesByDepartment(
        @Query("department") department: String,
        @Query("appointmentDate") appointmentDate: String
    ): Response<ApiResponse<List<String>>>
}
```

#### 3. UI 화면 업데이트
```kotlin
// ui/AppointmentFragment.kt
class AppointmentFragment : Fragment() {
    private fun setupUI() {
        // ❌ 의사 선택 스피너 제거
        // doctorSpinner.visibility = View.GONE
        
        // ❌ 진료실 표시 텍스트뷰 제거  
        // roomNumberTextView.visibility = View.GONE
        
        // ✅ 진료과 선택에 중점
        departmentSpinner.setOnItemSelectedListener { _, _, position, _ ->
            val selectedDepartment = departments[position]
            loadAvailableTimes(selectedDepartment)
        }
    }
    
    private fun loadAvailableTimes(department: String) {
        // 진료과 기반으로 가능한 시간 조회
        viewModel.getAvailableTimesByDepartment(department, selectedDate)
    }
}
```

### iOS 앱

#### 1. 데이터 모델 업데이트
```swift
// Models/AppointmentResponse.swift
struct AppointmentResponse: Codable {
    let appointmentId: Int64
    let memberName: String
    let hospitalName: String
    // let doctorName: String        // ❌ 제거
    let department: String
    // let roomNumber: String        // ❌ 제거
    let appointmentDate: String
    let appointmentTime: String
    let status: String
    let statusDescription: String
    let canCall: Bool
}
```

#### 2. API 서비스 업데이트
```swift
// Services/AppointmentService.swift
class AppointmentService {
    // ✅ 간소화된 예약 생성
    func createAppointment(
        memberId: Int64,
        hospitalName: String,
        department: String,
        appointmentDate: String,
        appointmentTime: String
    ) async throws -> ApiResponse<Int64> {
        let request = AppointmentCreateRequest(
            memberId: memberId,
            hospitalName: hospitalName,
            department: department,
            appointmentDate: appointmentDate,
            appointmentTime: appointmentTime
        )
        return try await apiClient.post("/appointments", body: request)
    }
    
    // ✅ 진료과별 가능 시간 조회
    func getAvailableTimesByDepartment(
        department: String,
        appointmentDate: String
    ) async throws -> ApiResponse<[String]> {
        return try await apiClient.get(
            "/appointments/available-times/department",
            parameters: [
                "department": department,
                "appointmentDate": appointmentDate
            ]
        )
    }
}
```

## 🌐 웹 프론트엔드 업데이트 가이드

### React 앱

#### 1. 컴포넌트 수정
```jsx
// components/AppointmentForm.jsx
const AppointmentForm = () => {
  const [formData, setFormData] = useState({
    memberId: '',
    hospitalName: '서울대병원',  // 기본값 설정
    department: '',
    appointmentDate: '',
    appointmentTime: ''
    // doctorName 필드 제거
    // roomNumber 필드 제거
  });

  return (
    <form onSubmit={handleSubmit}>
      <input 
        type="text" 
        value={formData.hospitalName}
        onChange={(e) => setFormData({...formData, hospitalName: e.target.value})}
        placeholder="병원명"
      />
      
      <select 
        value={formData.department}
        onChange={(e) => setFormData({...formData, department: e.target.value})}
      >
        <option value="">진료과 선택</option>
        <option value="내과">내과</option>
        <option value="정형외과">정형외과</option>
        <option value="피부과">피부과</option>
      </select>
      
      {/* 의사 선택 필드 제거 */}
      {/* 진료실 표시 필드 제거 */}
      
      <input 
        type="date" 
        value={formData.appointmentDate}
        onChange={(e) => setFormData({...formData, appointmentDate: e.target.value})}
      />
      
      <input 
        type="time" 
        value={formData.appointmentTime}
        onChange={(e) => setFormData({...formData, appointmentTime: e.target.value})}
      />
    </form>
  );
};
```

#### 2. API 호출 함수 수정
```jsx
// utils/api.js
export const createAppointment = async (appointmentData) => {
  const response = await fetch('/api/v1/appointments', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      memberId: appointmentData.memberId,
      hospitalName: appointmentData.hospitalName,
      department: appointmentData.department,
      appointmentDate: appointmentData.appointmentDate,
      appointmentTime: appointmentData.appointmentTime
      // doctorName과 roomNumber 제거
    })
  });
  return response.json();
};

export const getAvailableTimesByDepartment = async (department, appointmentDate) => {
  const response = await fetch(
    `/api/v1/appointments/available-times/department?department=${department}&appointmentDate=${appointmentDate}`
  );
  return response.json();
};
```

## 🧪 테스트 케이스 업데이트

### 단위 테스트

```java
// AppointmentServiceTest.java
@Test
void createAppointment_Success() {
    // Given
    Long memberId = 1L;
    String hospitalName = "서울대병원";
    String department = "정형외과";
    LocalDate appointmentDate = LocalDate.of(2025, 9, 1);
    LocalTime appointmentTime = LocalTime.of(10, 30);
    
    // When
    Long appointmentId = appointmentService.createAppointment(
        memberId, hospitalName, department, appointmentDate, appointmentTime
        // doctorName과 roomNumber 파라미터 제거
    );
    
    // Then
    assertThat(appointmentId).isNotNull();
    
    Appointment savedAppointment = appointmentRepository.findById(appointmentId).orElseThrow();
    assertThat(savedAppointment.getDepartment()).isEqualTo(department);
    assertThat(savedAppointment.getAppointmentDate()).isEqualTo(appointmentDate);
    assertThat(savedAppointment.getAppointmentTime()).isEqualTo(appointmentTime);
    // doctorName과 roomNumber 검증 제거
}
```

### 통합 테스트

```java
// AppointmentControllerTest.java
@Test
void createAppointment_ValidRequest_Success() throws Exception {
    // Given
    AppointmentCreateRequest request = AppointmentCreateRequest.builder()
        .memberId(1L)
        .hospitalName("서울대병원")
        .department("정형외과")
        .appointmentDate(LocalDate.of(2025, 9, 1))
        .appointmentTime(LocalTime.of(10, 30))
        // doctorName과 roomNumber 제거
        .build();
        
    // When & Then
    mockMvc.perform(post("/api/v1/appointments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("APPOINTMENT_2001"))
        .andExpect(jsonPath("$.message").value("예약이 성공적으로 생성되었습니다."))
        .andExpect(jsonPath("$.data").isNumber());
}
```

## 📋 체크리스트

### 백엔드 팀
- [ ] API 스펙 문서 업데이트
- [ ] Swagger 문서 업데이트  
- [ ] 통합 테스트 케이스 수정
- [ ] 기존 API 버전 관리 (필요 시)

### 프론트엔드 팀
- [ ] 예약 생성 폼에서 의사/진료실 필드 제거
- [ ] API 호출 함수 업데이트
- [ ] 예약 목록 화면에서 의사/진료실 표시 제거
- [ ] 진료과 기반 가능 시간 조회로 변경

### 모바일 앱 팀
- [ ] 데이터 모델 클래스 업데이트
- [ ] API 서비스 인터페이스 수정
- [ ] UI 화면에서 의사/진료실 관련 컴포넌트 제거
- [ ] 앱 스토어 업데이트 배포

### QA 팀
- [ ] API 테스트 케이스 업데이트
- [ ] 웹/모바일 UI 테스트 시나리오 수정
- [ ] 회귀 테스트 수행
- [ ] 성능 테스트 (간소화된 API 성능 확인)

## 🚨 마이그레이션 주의사항

1. **하위 호환성**: 기존 API와 호환되지 않으므로 클라이언트 앱 업데이트 필수
2. **점진적 마이그레이션**: 가능하다면 기존 API와 새 API를 병행 운영 후 단계적 전환 고려
3. **데이터 검증**: 의사명/진료실 번호를 검증하던 로직 제거 확인
4. **테스트**: 모든 기능에 대한 충분한 테스트 수행 필요

---

**문서 버전**: 1.0  
**최종 업데이트**: 2025년 8월 31일  
**담당자**: CareFreePass 개발팀