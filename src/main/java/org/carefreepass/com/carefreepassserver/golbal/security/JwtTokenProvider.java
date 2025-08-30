package org.carefreepass.com.carefreepassserver.golbal.security;

import static org.carefreepass.com.carefreepassserver.golbal.constant.SecurityConstant.TOKEN_ROLE_NAME;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.AccessTokenDto;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.RefreshTokenDto;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.response.TokenPairResponse;
import org.carefreepass.com.carefreepassserver.domain.auth.entity.domain.RefreshToken;
import org.carefreepass.com.carefreepassserver.domain.auth.repository.RefreshTokenRepository;
import org.carefreepass.com.carefreepassserver.domain.member.entity.MemberRole;
import org.carefreepass.com.carefreepassserver.golbal.error.BusinessException;
import org.carefreepass.com.carefreepassserver.golbal.error.ErrorCode;
import org.carefreepass.com.carefreepassserver.golbal.util.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenPairResponse generateTokenPair(Long memberId, MemberRole memberRole) {
        String accessToken = generateAccessToken(memberId, memberRole);
        String refreshToken = generateAndSaveRefreshToken(memberId);

        return TokenPairResponse.of(accessToken, refreshToken);
    }

    public String generateAccessToken(Long memberId, MemberRole memberRole) {
        return jwtUtil.generateAccessToken(memberId, memberRole);
    }

    public AccessTokenDto generateAccessTokenDto(Long memberId, MemberRole memberRole) {
        return jwtUtil.generateAccessTokenDto(memberId, memberRole);
    }

    public AccessTokenDto retrieveAccessToken(String accessTokenValue) {
        try {
            return jwtUtil.parseAccessToken(accessTokenValue);
        } catch (Exception e) {
            log.debug("Access Token 파싱 실패: {}", e.getMessage());
            return null;
        }
    }

    public RefreshTokenDto retrieveRefreshToken(String refreshTokenValue) {
        RefreshTokenDto refreshTokenDto = parseRefreshTokenSafely(refreshTokenValue);
        validateRefreshTokenExists(refreshTokenDto.memberId());
        return refreshTokenDto;
    }

    public AccessTokenDto reissueAccessTokenIfExpired(String accessTokenValue) {
        try {
            jwtUtil.parseAccessToken(accessTokenValue);
            return null; // 토큰이 유효하면 재발급 불필요
        } catch (ExpiredJwtException e) {
            return reissueAccessTokenFromExpired(e);
        }
    }

    public RefreshTokenDto createRefreshTokenDto(Long memberId) {
        RefreshTokenDto refreshTokenDto = jwtUtil.generateRefreshTokenDto(memberId);
        saveRefreshTokenToStorage(memberId, refreshTokenDto.tokenValue());
        return refreshTokenDto;
    }

    private String generateAndSaveRefreshToken(Long memberId) {
        String refreshTokenValue = jwtUtil.generateRefreshToken(memberId);
        saveRefreshTokenToStorage(memberId, refreshTokenValue);
        return refreshTokenValue;
    }

    private void saveRefreshTokenToStorage(Long memberId, String refreshTokenValue) {
        RefreshToken refreshToken = RefreshToken.builder()
                .memberId(memberId)
                .token(refreshTokenValue)
                .ttl(jwtUtil.getRefreshTokenExpirationTime())
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    private RefreshTokenDto parseRefreshTokenSafely(String refreshTokenValue) {
        try {
            RefreshTokenDto refreshTokenDto = jwtUtil.parseRefreshToken(refreshTokenValue);
            if (refreshTokenDto == null) {
                throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
            }
            return refreshTokenDto;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }

    private void validateRefreshTokenExists(Long memberId) {
        if (refreshTokenRepository.findById(memberId).isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }

    private AccessTokenDto reissueAccessTokenFromExpired(ExpiredJwtException expiredException) {
        Long memberId = Long.parseLong(expiredException.getClaims().getSubject());
        MemberRole memberRole = MemberRole.valueOf(
                expiredException.getClaims().get(TOKEN_ROLE_NAME, String.class)
        );

        return generateAccessTokenDto(memberId, memberRole);
    }
}
