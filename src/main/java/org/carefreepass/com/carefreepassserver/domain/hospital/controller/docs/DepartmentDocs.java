package org.carefreepass.com.carefreepassserver.domain.hospital.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.carefreepass.com.carefreepassserver.domain.hospital.dto.response.DepartmentListResponse;
import org.carefreepass.com.carefreepassserver.golbal.response.ApiResponseTemplate;

import java.util.List;

@Tag(name = "진료과 API", description = "진료과 목록 조회 API (환자/일반 사용자용)")
public interface DepartmentDocs {

    @Operation(
        summary = "전체 활성화된 진료과 목록 조회",
        description = "환자가 예약 시 선택할 수 있는 모든 활성화된 진료과 목록을 조회합니다. " +
                     "비활성화된 진료과는 제외되며, 병원별로 구분되어 반환됩니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "진료과 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = ApiResponseTemplate.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(schema = @Schema(implementation = ApiResponseTemplate.class))
        )
    })
    ApiResponseTemplate<List<DepartmentListResponse>> getAllDepartments();
}