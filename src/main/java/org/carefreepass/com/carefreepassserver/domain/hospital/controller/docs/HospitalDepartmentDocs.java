package org.carefreepass.com.carefreepassserver.domain.hospital.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.carefreepass.com.carefreepassserver.domain.hospital.dto.request.HospitalDepartmentCreateRequest;
import org.carefreepass.com.carefreepassserver.domain.hospital.dto.response.HospitalDepartmentResponse;
import org.carefreepass.com.carefreepassserver.golbal.response.ApiResponseTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 병원 진료과 관리 API 문서
 * 병원 관리자가 진료과를 생성, 조회, 수정, 삭제하는 기능을 제공합니다.
 */
@Tag(name = "병원 진료과 관리 API", description = "병원 관리자용 진료과 관리 기능")
public interface HospitalDepartmentDocs {

    @Operation(
            summary = "진료과 생성",
            description = "병원에 새로운 진료과를 생성합니다. 같은 병원에서 동일한 진료과명은 중복될 수 없습니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "진료과 생성 성공, 진료과 ID 반환"),
                    @ApiResponse(responseCode = "400", description = "중복된 진료과명 또는 잘못된 요청 데이터"),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
                    @ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 병원"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            }
    )
    ApiResponseTemplate<Long> createDepartment(
            @Parameter(description = "병원 ID", required = true, example = "1")
            @PathVariable Long hospitalId,
            @Parameter(description = "진료과 생성 정보", required = true)
            @Valid @RequestBody HospitalDepartmentCreateRequest request
    );

    @Operation(
            summary = "병원별 진료과 목록 조회",
            description = "특정 병원의 모든 진료과 목록을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "진료과 목록 조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
                    @ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 병원"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            }
    )
    ApiResponseTemplate<List<HospitalDepartmentResponse>> getDepartmentsByHospital(
            @Parameter(description = "병원 ID", required = true, example = "1")
            @PathVariable Long hospitalId
    );

    @Operation(
            summary = "진료과 상세 조회",
            description = "특정 진료과의 상세 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "진료과 정보 조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
                    @ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 진료과"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            }
    )
    ApiResponseTemplate<HospitalDepartmentResponse> getDepartment(
            @Parameter(description = "병원 ID", required = true, example = "1")
            @PathVariable Long hospitalId,
            @Parameter(description = "진료과 ID", required = true, example = "1")
            @PathVariable Long departmentId
    );

    @Operation(
            summary = "진료과 정보 수정",
            description = "기존 진료과의 정보를 수정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "진료과 수정 성공"),
                    @ApiResponse(responseCode = "400", description = "중복된 진료과명 또는 잘못된 요청 데이터"),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
                    @ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 병원 또는 진료과"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            }
    )
    ApiResponseTemplate<Void> updateDepartment(
            @Parameter(description = "병원 ID", required = true, example = "1")
            @PathVariable Long hospitalId,
            @Parameter(description = "진료과 ID", required = true, example = "1")
            @PathVariable Long departmentId,
            @Parameter(description = "진료과 수정 정보", required = true)
            @Valid @RequestBody HospitalDepartmentCreateRequest request
    );

    @Operation(
            summary = "진료과 삭제",
            description = "진료과를 삭제합니다. 예약이 있는 진료과는 삭제할 수 없습니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "진료과 삭제 성공"),
                    @ApiResponse(responseCode = "400", description = "예약이 있어 삭제할 수 없는 진료과"),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
                    @ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 병원 또는 진료과"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            }
    )
    ApiResponseTemplate<Void> deleteDepartment(
            @Parameter(description = "병원 ID", required = true, example = "1")
            @PathVariable Long hospitalId,
            @Parameter(description = "진료과 ID", required = true, example = "1")
            @PathVariable Long departmentId
    );
}