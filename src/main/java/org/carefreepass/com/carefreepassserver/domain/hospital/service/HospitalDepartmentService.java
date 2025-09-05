package org.carefreepass.com.carefreepassserver.domain.hospital.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.carefreepass.com.carefreepassserver.domain.hospital.dto.request.HospitalDepartmentCreateRequest;
import org.carefreepass.com.carefreepassserver.domain.hospital.dto.request.HospitalDepartmentUpdateRequest;
import org.carefreepass.com.carefreepassserver.domain.hospital.entity.Hospital;
import org.carefreepass.com.carefreepassserver.domain.hospital.entity.HospitalDepartment;
import org.carefreepass.com.carefreepassserver.domain.hospital.repository.HospitalDepartmentRepository;
import org.carefreepass.com.carefreepassserver.domain.hospital.repository.HospitalRepository;
import org.carefreepass.com.carefreepassserver.golbal.error.BusinessException;
import org.carefreepass.com.carefreepassserver.golbal.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 병원 진료과 관리 서비스 - 병원 관리자의 진료과 생성/수정/삭제/조회 기능
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HospitalDepartmentService {

    private final HospitalDepartmentRepository hospitalDepartmentRepository;
    private final HospitalRepository hospitalRepository;

    // 진료과 생성 (병원 유효성 및 중복 진료과명 확인)
    @Transactional
    public Long createDepartment(Long hospitalId, HospitalDepartmentCreateRequest request) {
        // 병원 존재 여부 확인
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new BusinessException(ErrorCode.HOSPITAL_NOT_FOUND));

        // 진료과명 중복 확인
        if (hospitalDepartmentRepository.existsByHospitalAndName(hospital, request.getName())) {
            throw new BusinessException(ErrorCode.DEPARTMENT_DUPLICATE_NAME);
        }

        // 진료과 생성
        HospitalDepartment department = HospitalDepartment.createDepartment(
                hospital, request.getName(), request.getDescription());

        HospitalDepartment savedDepartment = hospitalDepartmentRepository.save(department);
        
        return savedDepartment.getId();
    }

    // 병원의 활성화된 진료과 목록 조회
    public List<HospitalDepartment> getActiveDepartments(Long hospitalId) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new BusinessException(ErrorCode.HOSPITAL_NOT_FOUND));

        return hospitalDepartmentRepository.findByHospitalAndActiveTrue(hospital);
    }


    // 진료과 정보 수정 (진료과명 변경시 중복 확인)
    @Transactional
    public void updateDepartment(Long departmentId, HospitalDepartmentUpdateRequest request) {
        // 진료과 조회
        HospitalDepartment department = hospitalDepartmentRepository.findById(departmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND));

        // 진료과명 변경 시 중복 확인 (자기 자신 제외)
        if (!department.getName().equals(request.getName())) {
            if (hospitalDepartmentRepository.existsByHospitalAndName(department.getHospital(), request.getName())) {
                throw new BusinessException(ErrorCode.DEPARTMENT_DUPLICATE_NAME);
            }
        }

        // 진료과 정보 수정
        department.updateDepartment(request.getName(), request.getDescription());
    }

    // 진료과 비활성화
    @Transactional
    public void deactivateDepartment(Long departmentId) {
        HospitalDepartment department = hospitalDepartmentRepository.findById(departmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND));

        department.deactivate();
    }


    // 진료과 조회
    public HospitalDepartment getDepartment(Long departmentId) {
        return hospitalDepartmentRepository.findById(departmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND));
    }

    // 병원과 진료과명으로 진료과 조회
    public HospitalDepartment getDepartmentByName(Long hospitalId, String departmentName) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new BusinessException(ErrorCode.HOSPITAL_NOT_FOUND));

        return hospitalDepartmentRepository.findByHospitalAndNameAndActiveTrue(hospital, departmentName)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND));
    }

    // 모든 병원의 활성화된 진료과 조회 (환자용)
    public List<HospitalDepartment> getAllActiveDepartments() {
        return hospitalDepartmentRepository.findByActiveTrue();
    }
}