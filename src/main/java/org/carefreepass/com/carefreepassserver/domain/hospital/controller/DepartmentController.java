package org.carefreepass.com.carefreepassserver.domain.hospital.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.carefreepass.com.carefreepassserver.domain.hospital.controller.docs.DepartmentDocs;
import org.carefreepass.com.carefreepassserver.domain.hospital.dto.response.DepartmentListResponse;
import org.carefreepass.com.carefreepassserver.domain.hospital.entity.HospitalDepartment;
import org.carefreepass.com.carefreepassserver.domain.hospital.service.HospitalDepartmentService;
import org.carefreepass.com.carefreepassserver.golbal.response.ApiResponseTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 진료과 목록 조회 컨트롤러
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/departments")
@Slf4j
public class DepartmentController implements DepartmentDocs {

    private final HospitalDepartmentService hospitalDepartmentService;

    @Override
    @GetMapping
    public ApiResponseTemplate<List<DepartmentListResponse>> getAllDepartments() {
        List<HospitalDepartment> departments = hospitalDepartmentService.getAllActiveDepartments();
        List<DepartmentListResponse> responses = departments.stream()
                .map(DepartmentListResponse::from)
                .toList();
        
        return ApiResponseTemplate.ok()
                .code("DEPARTMENT_5001")
                .message("진료과 목록 조회가 완료되었습니다.")
                .body(responses);
    }
}