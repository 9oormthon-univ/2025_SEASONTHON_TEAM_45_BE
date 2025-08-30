package org.carefreepass.com.carefreepassserver.domain.auth.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.request.PatientSignInRequest;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.request.RefreshTokenRequest;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.response.TokenPairResponse;
import org.carefreepass.com.carefreepassserver.golbal.response.ApiResponseTemplate;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "인증 API", description = "환자 회원가입, 로그인, 토큰 재발급 API")
public interface AuthDocs {

    @Operation(
            summary = "환자 회원가입",
            description = "전화번호와 비밀번호를 이용해 환자 회원가입을 수행합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "회원가입 성공",
                            content = @Content(schema = @Schema(implementation = TokenPairResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "409", description = "이미 존재하는 사용자")
            }
    )
    ApiResponseTemplate<TokenPairResponse> patientSignUpWithLocal(@Valid @RequestBody PatientSignInRequest request);

    @Operation(
            summary = "환자 로그인",
            description = "전화번호와 비밀번호로 환자 로그인을 수행합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "로그인 성공",
                            content = @Content(schema = @Schema(implementation = TokenPairResponse.class))),
                    @ApiResponse(responseCode = "401", description = "인증 실패 (비밀번호 불일치, 사용자 없음)")
            }
    )
    ApiResponseTemplate<TokenPairResponse> patientSignInWithLocal(@Valid @RequestBody PatientSignInRequest request);

    @Operation(
            summary = "토큰 재발급",
            description = "Refresh Token을 이용하여 새로운 Access Token과 Refresh Token을 재발급합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "재발급 성공",
                            content = @Content(schema = @Schema(implementation = TokenPairResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Refresh Token이 유효하지 않음"),
                    @ApiResponse(responseCode = "403", description = "Refresh Token 만료")
            }
    )
    ApiResponseTemplate<TokenPairResponse> reissueTokenPair(@Valid @RequestBody RefreshTokenRequest request);
}
