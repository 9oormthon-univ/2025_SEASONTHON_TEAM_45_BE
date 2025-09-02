package org.carefreepass.com.carefreepassserver.domain.hospital.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.carefreepass.com.carefreepassserver.domain.hospital.dto.request.TimeSlotBlockRequest;
import org.carefreepass.com.carefreepassserver.domain.hospital.entity.HospitalDepartment;
import org.carefreepass.com.carefreepassserver.domain.hospital.entity.TimeSlotException;
import org.carefreepass.com.carefreepassserver.domain.hospital.repository.HospitalDepartmentRepository;
import org.carefreepass.com.carefreepassserver.domain.hospital.repository.TimeSlotExceptionRepository;
import org.carefreepass.com.carefreepassserver.golbal.error.BusinessException;
import org.carefreepass.com.carefreepassserver.golbal.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 시간 차단 관리 서비스
 * 병원 관리자가 특정 시간을 차단하거나 해제하는 기능을 담당합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimeSlotExceptionService {

    private final TimeSlotExceptionRepository timeSlotExceptionRepository;
    private final HospitalDepartmentRepository hospitalDepartmentRepository;

    /**
     * 특정 시간 차단
     * 
     * @param request 시간 차단 요청
     * @return 생성된 시간 예외 ID
     * @throws BusinessException 진료과를 찾을 수 없는 경우 (DEPARTMENT_NOT_FOUND)
     * @throws BusinessException 이미 차단된 시간인 경우 (TIME_SLOT_ALREADY_BLOCKED)
     */
    @Transactional
    public Long blockTimeSlot(TimeSlotBlockRequest request) {
        // 진료과 존재 여부 확인
        HospitalDepartment department = hospitalDepartmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND));

        // 이미 차단된 시간인지 확인
        if (timeSlotExceptionRepository.existsByHospitalDepartmentAndExceptionDateAndExceptionTimeAndBlockedTrue(
                department, request.getBlockDate(), request.getBlockTime())) {
            throw new BusinessException(ErrorCode.TIME_SLOT_ALREADY_BLOCKED);
        }

        // 기존 예외 설정이 있는지 확인 (차단 해제된 상태일 수 있음)
        TimeSlotException existingException = timeSlotExceptionRepository
                .findByHospitalDepartmentAndExceptionDateAndExceptionTime(
                        department, request.getBlockDate(), request.getBlockTime())
                .orElse(null);

        TimeSlotException savedException;
        if (existingException != null) {
            // 기존 예외가 있으면 차단으로 변경
            existingException.block();
            savedException = existingException;
        } else {
            // 새로운 차단 생성
            TimeSlotException newException = TimeSlotException.createBlockedTimeSlot(
                    department, request.getBlockDate(), request.getBlockTime());
            savedException = timeSlotExceptionRepository.save(newException);
        }


        return savedException.getId();
    }



    /**
     * 진료과의 특정 날짜 모든 시간 예외 조회
     * 
     * @param departmentId 진료과 ID
     * @param date 조회할 날짜
     * @return 시간 예외 목록
     * @throws BusinessException 진료과를 찾을 수 없는 경우 (DEPARTMENT_NOT_FOUND)
     */
    public List<TimeSlotException> getAllTimeExceptions(Long departmentId, LocalDate date) {
        HospitalDepartment department = hospitalDepartmentRepository.findById(departmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND));

        return timeSlotExceptionRepository.findByHospitalDepartmentAndExceptionDate(department, date);
    }

    /**
     * 시간 예외 삭제 (완전 제거)
     * 
     * @param exceptionId 시간 예외 ID
     * @throws BusinessException 시간 예외를 찾을 수 없는 경우 (TIME_SLOT_EXCEPTION_NOT_FOUND)
     */
    @Transactional
    public void deleteTimeSlotException(Long exceptionId) {
        TimeSlotException exception = timeSlotExceptionRepository.findById(exceptionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TIME_SLOT_EXCEPTION_NOT_FOUND));

        timeSlotExceptionRepository.delete(exception);

    }

}