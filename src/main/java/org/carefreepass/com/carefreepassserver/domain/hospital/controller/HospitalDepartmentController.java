package org.carefreepass.com.carefreepassserver.domain.hospital.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.carefreepass.com.carefreepassserver.domain.hospital.controller.docs.HospitalDepartmentDocs;
import org.carefreepass.com.carefreepassserver.domain.hospital.dto.request.HospitalDepartmentCreateRequest;
import org.carefreepass.com.carefreepassserver.domain.hospital.dto.response.HospitalDepartmentResponse;
import org.carefreepass.com.carefreepassserver.domain.hospital.entity.HospitalDepartment;
import org.carefreepass.com.carefreepassserver.domain.hospital.service.HospitalDepartmentService;
import org.carefreepass.com.carefreepassserver.golbal.error.BusinessException;
import org.carefreepass.com.carefreepassserver.golbal.response.ApiResponseTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 병원 진료과 관리 컨트롤러
 * 병원 관리자가 진료과를 관리하는 API를 제공합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/hospitals/{hospitalId}/departments")
public class HospitalDepartmentController implements HospitalDepartmentDocs {

    private final HospitalDepartmentService hospitalDepartmentService;

    @Override
    @PostMapping
    public ApiResponseTemplate<Long> createDepartment(@PathVariable Long hospitalId, 
                                                    @Valid @RequestBody HospitalDepartmentCreateRequest request) {
        Long departmentId = hospitalDepartmentService.createDepartment(hospitalId, request);
        return ApiResponseTemplate.ok()
                .code("HOSPITAL_3001")
                .message("진료과 생성이 완료되었습니다.")
                .body(departmentId);
    }

    @Override
    @GetMapping
    public ApiResponseTemplate<List<HospitalDepartmentResponse>> getDepartmentsByHospital(@PathVariable Long hospitalId) {
        List<HospitalDepartment> departments = hospitalDepartmentService.getActiveDepartments(hospitalId);
        List<HospitalDepartmentResponse> responses = departments.stream()
                .map(HospitalDepartmentResponse::from)
                .toList();
        return ApiResponseTemplate.ok()
                .code("HOSPITAL_3002")
                .message("진료과 목록 조회가 완료되었습니다.")
                .body(responses);
    }

    @Override
    @GetMapping("/{departmentId}")
    public ApiResponseTemplate<HospitalDepartmentResponse> getDepartment(@PathVariable Long hospitalId, 
                                                                       @PathVariable Long departmentId) {
        HospitalDepartment department = hospitalDepartmentService.getDepartment(departmentId);
        HospitalDepartmentResponse response = HospitalDepartmentResponse.from(department);
        return ApiResponseTemplate.ok()
                .code("HOSPITAL_3003")
                .message("진료과 조회가 완료되었습니다.")
                .body(response);
    }

    @Override
    @PutMapping("/{departmentId}")
    public ApiResponseTemplate<Void> updateDepartment(@PathVariable Long hospitalId, 
                                                     @PathVariable Long departmentId, 
                                                     @Valid @RequestBody HospitalDepartmentCreateRequest request) {
        org.carefreepass.com.carefreepassserver.domain.hospital.dto.request.HospitalDepartmentUpdateRequest updateRequest = 
            new org.carefreepass.com.carefreepassserver.domain.hospital.dto.request.HospitalDepartmentUpdateRequest(
                request.getName(), request.getDescription()
            );
        hospitalDepartmentService.updateDepartment(departmentId, updateRequest);
        return ApiResponseTemplate.ok()
                .code("HOSPITAL_3004")
                .message("진료과 수정이 완료되었습니다.")
                .build();
    }

    @Override
    @DeleteMapping("/{departmentId}")
    public ApiResponseTemplate<Void> deleteDepartment(@PathVariable Long hospitalId, 
                                                     @PathVariable Long departmentId) {
        hospitalDepartmentService.deactivateDepartment(departmentId);
        return ApiResponseTemplate.ok()
                .code("HOSPITAL_3005")
                .message("진료과 삭제가 완료되었습니다.")
                .build();
    }
}