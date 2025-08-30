package org.carefreepass.com.carefreepassserver.domain.auth.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.request.PatientSignInRequest;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.request.PatientSignUpRequest;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.request.RefreshTokenRequest;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.response.TokenPairResponse;
import org.carefreepass.com.carefreepassserver.golbal.response.ApiResponseTemplate;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "인증 API", description = "환자 회원가입, 로그인, 토큰 재발급 기능")
public interface AuthDocs {

    @Operation(
            summary = "환자 회원가입",
            description = "새로운 환자 계정을 생성합니다. 이름, 성별, 생년월일, 전화번호, 비밀번호가 필요하며, 성공 시 JWT 토큰을 발급합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "회원가입 성공, 토큰 발급",
                            content = @Content(schema = @Schema(implementation = TokenPairResponse.class))),
                    @ApiResponse(responseCode = "400", description = "이미 등록된 전화번호 또는 필수 정보 누락"),
                    @ApiResponse(responseCode = "500", description = "회원가입 처리 중 서버 오류")
            }
    )
    ApiResponseTemplate<TokenPairResponse> patientSignUpWithLocal(@Valid @RequestBody PatientSignUpRequest request);

    @Operation(
            summary = "환자 로그인",
            description = "등록된 환자 계정으로 로그인합니다. 전화번호와 비밀번호로 인증하고 새로운 JWT 토큰을 발급합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "로그인 성공, 토큰 발급",
                            content = @Content(schema = @Schema(implementation = TokenPairResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 전화번호 또는 비밀번호"),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 계정"),
                    @ApiResponse(responseCode = "500", description = "로그인 처리 중 서버 오류")
            }
    )
    ApiResponseTemplate<TokenPairResponse> patientSignInWithLocal(@Valid @RequestBody PatientSignInRequest request);

    @Operation(
            summary = "JWT 토큰 재발급",
            description = "만료된 Access Token을 Refresh Token으로 재발급합니다. 새로운 Access Token과 Refresh Token 쌍을 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "토큰 재발급 성공",
                            content = @Content(schema = @Schema(implementation = TokenPairResponse.class))),
                    @ApiResponse(responseCode = "400", description = "유효하지 않은 Refresh Token"),
                    @ApiResponse(responseCode = "401", description = "만료된 Refresh Token"),
                    @ApiResponse(responseCode = "500", description = "토큰 재발급 중 서버 오류")
            }
    )
    ApiResponseTemplate<TokenPairResponse> reissueTokenPair(@Valid @RequestBody RefreshTokenRequest request);
}
