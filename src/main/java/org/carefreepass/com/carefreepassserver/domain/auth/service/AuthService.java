package org.carefreepass.com.carefreepassserver.domain.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.RefreshTokenDto;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.TemporaryTokenDto;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.request.HospitalSignInRequest;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.request.HospitalSignUpRequest;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.request.PatientSignInRequest;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.request.PatientSignUpRequest;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.request.RefreshTokenRequest;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.response.TemporaryTokenResponse;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.response.TokenPairResponse;
import org.carefreepass.com.carefreepassserver.domain.auth.entity.TemporaryMember;
import org.carefreepass.com.carefreepassserver.domain.auth.entity.VerificationCode;
import org.carefreepass.com.carefreepassserver.domain.member.entity.Member;
import org.carefreepass.com.carefreepassserver.domain.member.entity.MemberRole;
import org.carefreepass.com.carefreepassserver.domain.member.repository.MemberRepository;
import org.carefreepass.com.carefreepassserver.golbal.error.BusinessException;
import org.carefreepass.com.carefreepassserver.golbal.error.ErrorCode;
import org.carefreepass.com.carefreepassserver.golbal.security.JwtTokenProvider;
import org.carefreepass.com.carefreepassserver.golbal.util.MemberUtil;
import org.carefreepass.com.carefreepassserver.infrastructure.sms.dto.request.SmsCodeRequest;
import org.carefreepass.com.carefreepassserver.infrastructure.sms.dto.request.SmsVerificationRequest;
import org.carefreepass.com.carefreepassserver.infrastructure.sms.service.SmsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final HospitalAuthService hospitalAuthService;
    private final PatientAuthService patientAuthService;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberUtil memberUtil;
    private final VerificationCodeService verificationCodeService;
    private final SmsService smsService;
    private final TemporaryTokenService temporaryTokenService;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public void sendSmsVerification(final SmsVerificationRequest request) {
       if (memberRepository.existsByPhoneNumber(request.phoneNumber())) {
              throw new BusinessException(ErrorCode.ALREADY_REGISTERED_PHONE_NUMBER);
       }
        smsService.sendVerificationSms(request.phoneNumber());
    }

    @Transactional(readOnly = true)
    public TemporaryTokenResponse verifySmsCode(final SmsCodeRequest request) {
        return verificationCodeService.verifyAndIssueTemporaryToken(request.phoneNumber(), request.code());
    }

    @Transactional
    public TokenPairResponse patientSignUpWithLocal(HttpServletRequest httpServletRequest, PatientSignUpRequest request) {
        String tempToken = temporaryTokenService.extractFrom(httpServletRequest);
        TemporaryTokenDto dto = temporaryTokenService.parseAndValidate(tempToken);
        TemporaryMember temporaryMember = temporaryTokenService.loadTemporaryMemberOrThrow(dto.temporaryMemberId());

        TokenPairResponse tokenPairResponse = patientAuthService.patientSignUpWithLocal(temporaryMember, request);
        temporaryTokenService.deleteTemporaryMember(temporaryMember.getId());

        return tokenPairResponse;
    }

    @Transactional(readOnly = true)
    public TokenPairResponse patientSignInWithLocal(PatientSignInRequest request) {
        return patientAuthService.patientSignInWithLocal(request);
    }

    @Transactional(readOnly = true)
    public TokenPairResponse reissueTokenPair(RefreshTokenRequest request) {
        RefreshTokenDto refreshTokenDto =
                jwtTokenProvider.retrieveRefreshToken(request.refreshToken());
        RefreshTokenDto refreshToken =
                jwtTokenProvider.createRefreshTokenDto(refreshTokenDto.memberId());

        Member member = memberUtil.getMemberByMemberId(refreshToken.memberId());

        return jwtTokenProvider.generateTokenPair(member.getId(), MemberRole.USER);
    }

    @Transactional
    public TokenPairResponse hospitalSignUpWithLocal(HospitalSignUpRequest request) {
        return hospitalAuthService.hospitalSignUpWithLocal(request);
    }

    @Transactional(readOnly = true)
    public TokenPairResponse hospitalSignInWithLocal(HospitalSignInRequest request) {
        return hospitalAuthService.hospitalSignInWithLocal(request);
    }
}
