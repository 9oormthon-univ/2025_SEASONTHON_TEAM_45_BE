package org.carefreepass.com.carefreepassserver.domain.appointment.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import org.carefreepass.com.carefreepassserver.domain.appointment.dto.response.AvailableTimeSlotsResponse;
import org.carefreepass.com.carefreepassserver.golbal.response.ApiResponseTemplate;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "환자용 시간대 조회 API", description = "환자가 예약 가능한 시간대를 조회하는 기능")
public interface PatientTimeSlotDocs {

    @Operation(
            summary = "예약 가능한 시간대 조회",
            description = "특정 병원의 특정 진료과에서 예약 가능한 시간대 목록을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "예약 가능 시간 조회 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
                    @ApiResponse(responseCode = "404", description = "병원 또는 진료과를 찾을 수 없음"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            }
    )
    ApiResponseTemplate<AvailableTimeSlotsResponse> getAvailableTimeSlots(
            @Parameter(description = "병원 ID", required = true, example = "1") 
            @RequestParam Long hospitalId,
            
            @Parameter(description = "진료과명", required = true, example = "내과") 
            @RequestParam String departmentName,
            
            @Parameter(description = "조회할 날짜 (YYYY-MM-DD)", required = true, example = "2024-12-31") 
            @RequestParam LocalDate date
    );

    @Operation(
            summary = "특정 시간의 예약 가능 여부 확인",
            description = "특정 병원의 특정 진료과에서 특정 시간대의 예약 가능 여부를 확인합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "시간대 예약 가능 여부 확인 완료"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
                    @ApiResponse(responseCode = "404", description = "병원 또는 진료과를 찾을 수 없음"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            }
    )
    ApiResponseTemplate<Boolean> checkTimeSlotAvailable(
            @Parameter(description = "병원 ID", required = true, example = "1") 
            @RequestParam Long hospitalId,
            
            @Parameter(description = "진료과명", required = true, example = "내과") 
            @RequestParam String departmentName,
            
            @Parameter(description = "조회할 날짜 (YYYY-MM-DD)", required = true, example = "2024-12-31") 
            @RequestParam LocalDate date,
            
            @Parameter(description = "확인할 시간 (HH:mm)", required = true, example = "14:30") 
            @RequestParam String time
    );
}