package org.carefreepass.com.carefreepassserver.domain.auth.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.request.HospitalSignInRequest;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.request.HospitalSignUpRequest;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.request.PatientSignInRequest;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.request.PatientSignUpRequest;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.request.RefreshTokenRequest;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.response.TemporaryTokenResponse;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.response.TokenPairResponse;
import org.carefreepass.com.carefreepassserver.golbal.response.ApiResponseTemplate;
import org.carefreepass.com.carefreepassserver.infrastructure.sms.dto.request.SmsCodeRequest;
import org.carefreepass.com.carefreepassserver.infrastructure.sms.dto.request.SmsVerificationRequest;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "인증 API", description = "환자/병원 회원가입·로그인, 토큰 재발급, SMS 인증")
public interface AuthDocs {

    @Operation(
            summary = "SMS 인증코드 전송",
            description = "입력한 휴대전화번호로 인증코드를 발송합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "문자 전송 성공",
                            content = @Content(schema = @Schema(implementation = ApiResponseTemplate.class))),
                    @ApiResponse(responseCode = "400", description = "전화번호 유효성 실패 또는 레이트리밋"),
                    @ApiResponse(responseCode = "500", description = "외부 SMS 게이트웨이 오류")
            }
    )
    ApiResponseTemplate<Void> sendSmsVerification(@Valid @RequestBody SmsVerificationRequest request);

    @Operation(
            summary = "SMS 인증코드 검증",
            description = "수신한 인증코드를 검증하고 임시 토큰을 발급합니다. 임시 토큰을 발급받은 회원만 회원가입이 가능합니다."
                    + " 발급받은 임시 토큰을 헤더에 담아 회원가입 API를 호출하세요.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "인증 성공, 임시 토큰 발급",
                            content = @Content(schema = @Schema(implementation = TemporaryTokenResponse.class))),
                    @ApiResponse(responseCode = "400", description = "인증코드 불일치/만료"),
                    @ApiResponse(responseCode = "404", description = "인증코드 없음"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")
            }
    )
    ApiResponseTemplate<TemporaryTokenResponse> verifySmsCode(@Valid @RequestBody SmsCodeRequest request);

    @Operation(
            summary = "환자 회원가입",
            description = "새로운 환자 계정을 생성하고 JWT 토큰을 발급합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "회원가입 성공, 토큰 발급",
                            content = @Content(schema = @Schema(implementation = TokenPairResponse.class))),
                    @ApiResponse(responseCode = "400", description = "중복 전화번호 또는 필수 정보 누락"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")
            }
    )
    ApiResponseTemplate<TokenPairResponse> patientSignUpWithLocal(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody PatientSignUpRequest request
    );

    @Operation(
            summary = "환자 로그인",
            description = "전화번호/비밀번호로 로그인하고 JWT 토큰을 발급합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "로그인 성공, 토큰 발급",
                            content = @Content(schema = @Schema(implementation = TokenPairResponse.class))),
                    @ApiResponse(responseCode = "400", description = "자격 증명 오류"),
                    @ApiResponse(responseCode = "404", description = "계정 없음"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")
            }
    )
    ApiResponseTemplate<TokenPairResponse> patientSignInWithLocal(
            @Valid @RequestBody PatientSignInRequest request
    );

    @Operation(
            summary = "JWT 토큰 재발급",
            description = "Refresh Token으로 Access/Refresh 토큰을 재발급합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "재발급 성공",
                            content = @Content(schema = @Schema(implementation = TokenPairResponse.class))),
                    @ApiResponse(responseCode = "400", description = "유효하지 않은 Refresh Token"),
                    @ApiResponse(responseCode = "401", description = "만료된 Refresh Token"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")
            }
    )
    ApiResponseTemplate<TokenPairResponse> reissueTokenPair(
            @Valid @RequestBody RefreshTokenRequest request
    );
    @Operation(
            summary = "병원 회원가입(관리자 + 병원 + 소속 생성)",
            description = "관리자·병원 정보를 등록하고 JWT 토큰을 발급합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "회원가입 성공, 토큰 발급",
                            content = @Content(schema = @Schema(implementation = TokenPairResponse.class))),
                    @ApiResponse(responseCode = "400", description = "중복/유효성 실패"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")
            }
    )
    ApiResponseTemplate<TokenPairResponse> hospitalSignUpWithLocal(
            @Valid @RequestBody HospitalSignUpRequest request
    );

    @Operation(
            summary = "병원 로그인(관리자)",
            description = "아이디/비밀번호로 로그인하고 JWT 토큰을 발급합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "로그인 성공, 토큰 발급",
                            content = @Content(schema = @Schema(implementation = TokenPairResponse.class))),
                    @ApiResponse(responseCode = "400", description = "자격 증명 오류"),
                    @ApiResponse(responseCode = "404", description = "계정 없음"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")
            }
    )
    ApiResponseTemplate<TokenPairResponse> hospitalSignInWithLocal(
            @Valid @RequestBody HospitalSignInRequest request
    );
}
