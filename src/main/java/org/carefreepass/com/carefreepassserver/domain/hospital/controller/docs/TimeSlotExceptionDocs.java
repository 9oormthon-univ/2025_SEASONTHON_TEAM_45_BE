package org.carefreepass.com.carefreepassserver.domain.hospital.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.carefreepass.com.carefreepassserver.domain.hospital.dto.request.TimeSlotBlockRequest;
import org.carefreepass.com.carefreepassserver.domain.hospital.dto.response.TimeSlotExceptionResponse;
import org.carefreepass.com.carefreepassserver.golbal.response.ApiResponseTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 병원 시간 차단 관리 API 문서
 * 병원 관리자가 특정 시간대를 차단하거나 해제하는 기능을 제공합니다.
 */
@Tag(name = "병원 시간 차단 관리 API", description = "병원 관리자용 시간 차단/해제 기능")
public interface TimeSlotExceptionDocs {

    @Operation(
            summary = "시간대 차단",
            description = "병원 관리자가 특정 날짜와 시간을 차단하여 예약을 받지 않도록 설정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "시간 차단 성공, 차단 ID 반환"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 또는 존재하지 않는 진료과"),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
                    @ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            }
    )
    ApiResponseTemplate<Long> blockTimeSlot(
            @Parameter(description = "시간 차단 요청 정보", required = true)
            @Valid @RequestBody TimeSlotBlockRequest request
    );

    @Operation(
            summary = "시간대 차단 해제",
            description = "기존에 차단된 시간대를 다시 예약 가능하도록 해제합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "시간 차단 해제 성공"),
                    @ApiResponse(responseCode = "400", description = "존재하지 않는 차단 ID"),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
                    @ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            }
    )
    ApiResponseTemplate<Void> unblockTimeSlot(
            @Parameter(description = "차단 해제할 예외 설정 ID", required = true, example = "1")
            @PathVariable Long exceptionId
    );

    @Operation(
            summary = "시간대 차단 수정",
            description = "기존 시간 차단 설정을 수정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "시간 차단 수정 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
                    @ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            }
    )
    ApiResponseTemplate<Void> updateTimeSlot(
            @Parameter(description = "수정할 차단 설정 ID", required = true, example = "1")
            @PathVariable Long exceptionId,
            @Parameter(description = "수정할 시간 차단 정보", required = true)
            @Valid @RequestBody TimeSlotBlockRequest request
    );

    @Operation(
            summary = "차단된 시간대 조회",
            description = "진료과별 차단된 시간대 목록을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "차단된 시간대 목록 조회 성공"),
                    @ApiResponse(responseCode = "400", description = "존재하지 않는 진료과"),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
                    @ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            }
    )
    ApiResponseTemplate<List<TimeSlotExceptionResponse>> getBlockedTimeSlots(
            @Parameter(description = "진료과 ID", required = true, example = "1")
            @RequestParam Long departmentId
    );

    @Operation(
            summary = "특정 날짜 차단 시간대 조회",
            description = "특정 날짜에 차단된 시간대 목록을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "해당 날짜 차단 시간대 목록 조회 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 날짜 형식 또는 존재하지 않는 진료과"),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
                    @ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            }
    )
    ApiResponseTemplate<List<TimeSlotExceptionResponse>> getBlockedTimeSlotsForDate(
            @Parameter(description = "진료과 ID", required = true, example = "1")
            @RequestParam Long departmentId,
            @Parameter(description = "조회할 날짜 (YYYY-MM-DD)", required = true, example = "2024-12-25")
            @RequestParam LocalDate date
    );
}