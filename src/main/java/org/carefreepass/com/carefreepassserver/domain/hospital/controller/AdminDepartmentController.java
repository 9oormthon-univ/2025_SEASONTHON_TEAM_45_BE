package org.carefreepass.com.carefreepassserver.domain.hospital.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.carefreepass.com.carefreepassserver.domain.hospital.dto.response.DepartmentListResponse;
import org.carefreepass.com.carefreepassserver.domain.hospital.entity.HospitalDepartment;
import org.carefreepass.com.carefreepassserver.domain.hospital.service.HospitalDepartmentService;
import org.carefreepass.com.carefreepassserver.golbal.response.ApiResponseTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 관리자용 진료과 조회 컨트롤러
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@Slf4j
public class AdminDepartmentController {

    private final HospitalDepartmentService hospitalDepartmentService;

    @GetMapping("/departments")
    public ApiResponseTemplate<List<DepartmentListResponse>> getAllDepartments() {
        List<HospitalDepartment> departments = hospitalDepartmentService.getAllActiveDepartments();
        List<DepartmentListResponse> responses = departments.stream()
                .map(DepartmentListResponse::from)
                .toList();
        
        return ApiResponseTemplate.ok()
                .code("ADMIN_DEPARTMENT_5001")
                .message("관리자용 진료과 목록 조회가 완료되었습니다.")
                .body(responses);
    }
}