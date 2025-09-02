package org.carefreepass.com.carefreepassserver.domain.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.TemporaryTokenDto;
import org.carefreepass.com.carefreepassserver.domain.auth.entity.TemporaryMember;
import org.carefreepass.com.carefreepassserver.domain.auth.repository.TemporaryMemberRepository;
import org.carefreepass.com.carefreepassserver.golbal.error.BusinessException;
import org.carefreepass.com.carefreepassserver.golbal.error.ErrorCode;
import org.carefreepass.com.carefreepassserver.golbal.security.JwtTokenProvider;
import org.carefreepass.com.carefreepassserver.golbal.util.JwtUtil;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TemporaryTokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final TemporaryMemberRepository temporaryMemberRepository;

    public String extractFrom(HttpServletRequest httpServletRequest) {
        String token = JwtUtil.extractTemporaryTokenFromHeader(httpServletRequest);
        if (token == null) {
            throw new BusinessException(ErrorCode.INVALID_TEMPORARY_TOKEN);
        }
        return token;
    }

    public TemporaryTokenDto parseAndValidate(String temporaryToken) {
        TemporaryTokenDto dto = jwtTokenProvider.retrieveTemporaryToken(temporaryToken);
        if (dto == null) {
            throw new BusinessException(ErrorCode.INVALID_TEMPORARY_TOKEN);
        }
        return dto;
    }

    public TemporaryMember loadTemporaryMemberOrThrow(String temporaryMemberId) {
        return temporaryMemberRepository.findById(temporaryMemberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    public void deleteTemporaryMember(String temporaryMemberId) {
        temporaryMemberRepository.deleteById(temporaryMemberId);
    }
}
