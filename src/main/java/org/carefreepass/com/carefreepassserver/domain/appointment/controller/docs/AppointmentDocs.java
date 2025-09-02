package org.carefreepass.com.carefreepassserver.domain.appointment.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.carefreepass.com.carefreepassserver.domain.appointment.dto.request.AppointmentCheckinRequest;
import org.carefreepass.com.carefreepassserver.domain.appointment.dto.request.AppointmentCreateRequest;
import org.carefreepass.com.carefreepassserver.domain.appointment.dto.request.AppointmentUpdateRequest;
import org.carefreepass.com.carefreepassserver.domain.appointment.dto.response.AppointmentResponse;
import org.carefreepass.com.carefreepassserver.golbal.response.ApiResponseTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "예약 관리 API", description = "환자 예약 생성, 수정, 조회, 삭제 기능 (환자/관리자 권한별 구분)")
public interface AppointmentDocs {
    
    @Operation(
            summary = "예약 생성 (환자도 가능)",
            description = "환자의 병원 예약을 생성합니다. 같은 날짜에 중복 예약은 불가능합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "예약 생성 성공, 예약 ID 반환"),
                    @ApiResponse(responseCode = "400", description = "중복 예약 또는 존재하지 않는 회원"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            }
    )
    ApiResponseTemplate<Long> createAppointment(@Valid @RequestBody AppointmentCreateRequest request);
    
    @Operation(
            summary = "환자 체크인 (환자도 가능)",
            description = "환자가 병원에 도착했을 때 체크인 처리합니다. 상태가 ARRIVED로 변경됩니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "체크인 성공"),
                    @ApiResponse(responseCode = "400", description = "본인 예약이 아니거나 이미 체크인 완료"),
                    @ApiResponse(responseCode = "404", description = "예약을 찾을 수 없음")
            }
    )
    ApiResponseTemplate<String> checkinAppointment(@Valid @RequestBody AppointmentCheckinRequest request);
    
    @Operation(
            summary = "오늘 대기 환자 조회 (관리자 전용)",
            description = "오늘 날짜의 대기 중인 환자 목록을 조회합니다. (BOOKED, ARRIVED 상태)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "대기 환자 목록 조회 성공"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            }
    )
    ApiResponseTemplate<List<AppointmentResponse>> getTodayWaitingPatients();
    
    @Operation(
            summary = "오늘 전체 예약 조회 (관리자 전용)",
            description = "오늘 날짜의 모든 예약을 조회합니다. 상태와 관계없이 전체 목록을 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "전체 예약 목록 조회 성공"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            }
    )
    ApiResponseTemplate<List<AppointmentResponse>> getAllTodayAppointments();
    
    @Operation(
            summary = "예약 삭제 (관리자 전용)",
            description = "예약을 완전히 삭제합니다. 삭제된 예약은 복구할 수 없습니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "예약 삭제 성공"),
                    @ApiResponse(responseCode = "404", description = "예약을 찾을 수 없음"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            }
    )
    ApiResponseTemplate<String> deleteAppointment(@PathVariable Long appointmentId);
    
    @Operation(
            summary = "예약 상태 변경 (관리자 전용)",
            description = "예약의 상태를 변경합니다. (WAITING_BEFORE_ARRIVAL→BOOKED→ARRIVED→CALLED→COMPLETED 또는 CANCELLED)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "상태 변경 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 상태값"),
                    @ApiResponse(responseCode = "404", description = "예약을 찾을 수 없음")
            }
    )
    ApiResponseTemplate<String> updateAppointmentStatus(@PathVariable Long appointmentId, @PathVariable String status);
    
    @Operation(
            summary = "예약 정보 수정 (환자도 가능)",
            description = "예약의 병원명, 진료과, 날짜, 시간 등을 수정합니다. 완료/취소된 예약은 수정할 수 없습니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "예약 수정 성공"),
                    @ApiResponse(responseCode = "400", description = "수정 불가능한 상태"),
                    @ApiResponse(responseCode = "404", description = "예약을 찾을 수 없음")
            }
    )
    ApiResponseTemplate<String> updateAppointment(@PathVariable Long appointmentId, @Valid @RequestBody AppointmentUpdateRequest request);

    // 환자용 예약 조회 API 문서화 추가
    @Operation(
            summary = "내 전체 예약 목록 조회 (환자 전용)",
            description = "환자가 본인의 모든 예약 내역을 조회합니다. (과거/현재/미래 예약 포함)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "내 예약 목록 조회 성공"),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 회원"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            }
    )
    ApiResponseTemplate<List<AppointmentResponse>> getMyAppointments(@RequestParam Long memberId);

    @Operation(
            summary = "오늘 내 예약 조회 (환자 전용)",
            description = "환자가 오늘 예정된 본인의 예약만 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "오늘 내 예약 조회 성공"),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 회원"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            }
    )
    ApiResponseTemplate<List<AppointmentResponse>> getMyTodayAppointments(@RequestParam Long memberId);
}