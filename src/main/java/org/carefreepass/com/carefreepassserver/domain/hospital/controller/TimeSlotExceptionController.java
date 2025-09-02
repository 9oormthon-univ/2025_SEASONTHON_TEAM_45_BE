package org.carefreepass.com.carefreepassserver.domain.hospital.controller;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.carefreepass.com.carefreepassserver.domain.hospital.controller.docs.TimeSlotExceptionDocs;
import org.carefreepass.com.carefreepassserver.domain.hospital.dto.request.TimeSlotBlockRequest;
import org.carefreepass.com.carefreepassserver.domain.hospital.dto.response.TimeSlotExceptionResponse;
import org.carefreepass.com.carefreepassserver.domain.hospital.entity.TimeSlotException;
import org.carefreepass.com.carefreepassserver.domain.hospital.service.TimeSlotExceptionService;
import org.carefreepass.com.carefreepassserver.golbal.error.BusinessException;
import org.carefreepass.com.carefreepassserver.golbal.response.ApiResponseTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 시간 차단 관리 컨트롤러
 * 병원 관리자가 특정 시간을 차단/해제하는 API를 제공합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/time-slots")
public class TimeSlotExceptionController implements TimeSlotExceptionDocs {

    private final TimeSlotExceptionService timeSlotExceptionService;

    @Override
    @PostMapping("/block")
    public ApiResponseTemplate<Long> blockTimeSlot(@Valid @RequestBody TimeSlotBlockRequest request) {
        Long exceptionId = timeSlotExceptionService.blockTimeSlot(request);
        return ApiResponseTemplate.ok()
                .code("TIME_SLOT_2001")
                .message("시간 차단이 성공적으로 완료되었습니다.")
                .body(exceptionId);
    }

    @Override
    @DeleteMapping("/{exceptionId}")
    public ApiResponseTemplate<Void> unblockTimeSlot(@PathVariable Long exceptionId) {
        timeSlotExceptionService.deleteTimeSlotException(exceptionId);
        return ApiResponseTemplate.ok()
                .code("TIME_SLOT_2002")
                .message("시간 차단이 성공적으로 해제되었습니다.")
                .build();
    }

    @Override
    @PutMapping("/{exceptionId}")
    public ApiResponseTemplate<Void> updateTimeSlot(@PathVariable Long exceptionId, @Valid @RequestBody TimeSlotBlockRequest request) {
        // 삭제 후 재생성으로 업데이트 구현
        timeSlotExceptionService.deleteTimeSlotException(exceptionId);
        timeSlotExceptionService.blockTimeSlot(request);
        return ApiResponseTemplate.ok()
                .code("TIME_SLOT_2003")
                .message("시간 차단 설정이 성공적으로 수정되었습니다.")
                .build();
    }

    @Override
    @GetMapping("/blocked")
    public ApiResponseTemplate<List<TimeSlotExceptionResponse>> getBlockedTimeSlots(@RequestParam Long departmentId) {
        LocalDate today = LocalDate.now();
        List<TimeSlotException> exceptions = timeSlotExceptionService.getAllTimeExceptions(departmentId, today);
        List<TimeSlotExceptionResponse> responses = exceptions.stream()
                .map(TimeSlotExceptionResponse::from)
                .toList();
        return ApiResponseTemplate.ok()
                .code("TIME_SLOT_2004")
                .message("차단된 시간 목록 조회가 완료되었습니다.")
                .body(responses);
    }

    @Override
    @GetMapping("/blocked/date")
    public ApiResponseTemplate<List<TimeSlotExceptionResponse>> getBlockedTimeSlotsForDate(
            @RequestParam Long departmentId, @RequestParam LocalDate date) {
        List<TimeSlotException> exceptions = timeSlotExceptionService.getAllTimeExceptions(departmentId, date);
        List<TimeSlotExceptionResponse> responses = exceptions.stream()
                .map(TimeSlotExceptionResponse::from)
                .toList();
        return ApiResponseTemplate.ok()
                .code("TIME_SLOT_2005")
                .message("특정 날짜 차단 시간 조회가 완료되었습니다.")
                .body(responses);
    }
}