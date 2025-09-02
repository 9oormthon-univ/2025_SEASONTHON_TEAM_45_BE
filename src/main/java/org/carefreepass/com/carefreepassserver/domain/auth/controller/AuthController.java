package org.carefreepass.com.carefreepassserver.domain.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.carefreepass.com.carefreepassserver.domain.auth.controller.docs.AuthDocs;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.request.HospitalSignInRequest;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.request.HospitalSignUpRequest;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.request.PatientSignInRequest;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.request.PatientSignUpRequest;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.request.RefreshTokenRequest;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.response.TemporaryTokenResponse;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.response.TokenPairResponse;
import org.carefreepass.com.carefreepassserver.domain.auth.service.AuthService;
import org.carefreepass.com.carefreepassserver.golbal.response.ApiResponseTemplate;
import org.carefreepass.com.carefreepassserver.infrastructure.sms.dto.request.SmsCodeRequest;
import org.carefreepass.com.carefreepassserver.infrastructure.sms.dto.request.SmsVerificationRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController implements AuthDocs {
    private final AuthService authService;

    @PostMapping("sms/send")
    public ApiResponseTemplate<Void> sendSmsVerification(@Valid @RequestBody SmsVerificationRequest request) {
        authService.sendSmsVerification(request);
        return ApiResponseTemplate.ok()
                .code("SMS_2001")
                .message("문자가 성공적으로 전송되었습니다.")
                .body(null);
    }

    @PostMapping("sms/verify")
    public ApiResponseTemplate<TemporaryTokenResponse> verifySmsCode(@Valid @RequestBody SmsCodeRequest request) {
        TemporaryTokenResponse res = authService.verifySmsCode(request);
        return ApiResponseTemplate.ok()
                .code("SMS_2002")
                .message("SMS 인증이 성공적으로 완료되었습니다.")
                .body(res);
    }

    @PostMapping("patient/sign-up")
    public ApiResponseTemplate<TokenPairResponse> patientSignUpWithLocal(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody PatientSignUpRequest request) {
        TokenPairResponse response = authService.patientSignUpWithLocal(httpServletRequest, request);

        return ApiResponseTemplate.ok()
                .code("AUTH_2001")
                .message("환자 회원가입이 완료되었습니다.")
                .body(response);
    }

    @PostMapping("patient/sign-in")
    public ApiResponseTemplate<TokenPairResponse> patientSignInWithLocal(
            @Valid @RequestBody PatientSignInRequest request) {
        TokenPairResponse response = authService.patientSignInWithLocal(request);

        return ApiResponseTemplate.ok()
                .code("AUTH_2002")
                .message("환자 로그인에 성공했습니다.")
                .body(response);
    }

    @PostMapping("/reissue")
    public ApiResponseTemplate<TokenPairResponse> reissueTokenPair(@Valid @RequestBody RefreshTokenRequest request) {
        TokenPairResponse response = authService.reissueTokenPair(request);

        return ApiResponseTemplate.ok()
                .code("AUTH_2003")
                .message("토큰 재발급에 성공했습니다.")
                .body(response);
    }

    @PostMapping("hospital/sign-up")
    public ApiResponseTemplate<TokenPairResponse> hospitalSignUpWithLocal(@RequestBody HospitalSignUpRequest request) {
        TokenPairResponse response = authService.hospitalSignUpWithLocal(request);

        return ApiResponseTemplate.ok()
                .code("AUTH_2004")
                .message("병원 회원가입이 완료되었습니다.")
                .body(response);
    }

    @PostMapping("hospital/sign-in")
    public ApiResponseTemplate<TokenPairResponse> hospitalSignInWithLocal(@RequestBody HospitalSignInRequest request) {
        TokenPairResponse response = authService.hospitalSignInWithLocal(request);

        return ApiResponseTemplate.ok()
                .code("AUTH_2005")
                .message("병원 로그인에 성공했습니다.")
                .body(response);
    }
}
