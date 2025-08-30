package org.carefreepass.com.carefreepassserver.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.RefreshTokenDto;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.request.PatientSignInRequest;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.request.PatientSignUpRequest;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.request.RefreshTokenRequest;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.response.TokenPairResponse;
import org.carefreepass.com.carefreepassserver.domain.member.entity.Member;
import org.carefreepass.com.carefreepassserver.domain.member.entity.MemberRole;
import org.carefreepass.com.carefreepassserver.golbal.security.JwtTokenProvider;
import org.carefreepass.com.carefreepassserver.golbal.util.MemberUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final PatientAuthService patientAuthService;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberUtil memberUtil;

    @Transactional
    public TokenPairResponse patientSignUpWithLocal(PatientSignUpRequest request) {
        return patientAuthService.patientSignUpWithLocal(request);
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
}
