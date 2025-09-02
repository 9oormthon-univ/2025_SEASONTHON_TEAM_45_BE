package org.carefreepass.com.carefreepassserver.golbal.util;

import static org.carefreepass.com.carefreepassserver.golbal.constant.SecurityConstant.TOKEN_PREFIX;
import static org.carefreepass.com.carefreepassserver.golbal.constant.SecurityConstant.TOKEN_ROLE_NAME;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.AccessTokenDto;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.RefreshTokenDto;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.TemporaryTokenDto;
import org.carefreepass.com.carefreepassserver.domain.auth.entity.TokenType;
import org.carefreepass.com.carefreepassserver.domain.member.entity.MemberRole;
import org.carefreepass.com.carefreepassserver.golbal.properties.JwtProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;

    private static final String TOKEN_TYPE_KEY_NAME = "type";
    private static final String HEADER_TYP = "typ";
    private static final String HEADER_ALG = "alg";
    private static final String HEADER_REG_DATE = "regDate";
    private static final String JWT_TYPE = "JWT";
    private static final String HS256_ALG = "HS256";

    public String generateAccessToken(Long memberId, MemberRole memberRole) {
        TokenTimeInfo timeInfo = createTokenTimeInfo(jwtProperties.accessTokenExpirationMilliTime());
        return buildJwtToken(
                TokenType.ACCESS,
                memberId.toString(),
                Map.of(TOKEN_ROLE_NAME, memberRole.name()),
                timeInfo,
                getAccessTokenKey()
        );
    }

    public AccessTokenDto generateAccessTokenDto(Long memberId, MemberRole memberRole) {
        String tokenValue = generateAccessToken(memberId, memberRole);
        return AccessTokenDto.of(memberId, memberRole, tokenValue);
    }

    public String generateRefreshToken(Long memberId) {
        TokenTimeInfo timeInfo = createTokenTimeInfo(jwtProperties.refreshTokenExpirationMilliTime());
        return buildJwtToken(
                TokenType.REFRESH,
                memberId.toString(),
                Map.of(),
                timeInfo,
                getRefreshTokenKey()
        );
    }

    public RefreshTokenDto generateRefreshTokenDto(Long memberId) {
        Date issuedAt = new Date();
        Date expiredAt =
                new Date(issuedAt.getTime() + jwtProperties.refreshTokenExpirationMilliTime());
        String tokenValue = buildRefreshToken(memberId, issuedAt, expiredAt);
        return new RefreshTokenDto(
                memberId, tokenValue, jwtProperties.refreshTokenExpirationTime());
    }

    public String generateTemporaryToken(String temporaryMemberId) {
        TokenTimeInfo timeInfo = createTokenTimeInfo(jwtProperties.temporaryTokenExpirationTime());
        return buildJwtToken(
                TokenType.TEMPORARY,
                temporaryMemberId,
                Map.of(TOKEN_ROLE_NAME, MemberRole.TEMPORARY.name()),
                timeInfo,
                getTemporaryTokenKey()
        );
    }

    public AccessTokenDto parseAccessToken(String token) throws ExpiredJwtException {
        try {
            Jws<Claims> claims = parseTokenClaims(token, getAccessTokenKey());
            Claims body = claims.getBody();

            return new AccessTokenDto(
                    Long.parseLong(body.getSubject()),
                    MemberRole.valueOf(body.get(TOKEN_ROLE_NAME, String.class)),
                    token
            );
        } catch (ExpiredJwtException e) {
            throw e;
        } catch (Exception e) {
            return null;
        }
    }

    public RefreshTokenDto parseRefreshToken(String token) throws ExpiredJwtException {
        try {
            Jws<Claims> claims = parseTokenClaims(token, getRefreshTokenKey());
            Claims body = claims.getBody();

            return new RefreshTokenDto(
                    Long.parseLong(body.getSubject()),
                    token,
                    jwtProperties.refreshTokenExpirationTime()
            );
        } catch (ExpiredJwtException e) {
            throw e;
        } catch (Exception e) {
            return null;
        }
    }

    public TemporaryTokenDto parseTemporaryToken(String token) throws ExpiredJwtException {
        try {
            Jws<Claims> claims = parseTokenClaims(token, getTemporaryTokenKey());
            Claims body = claims.getBody();

            return new TemporaryTokenDto(
                    body.getSubject(),
                    MemberRole.valueOf(body.get(TOKEN_ROLE_NAME, String.class)),
                    token,
                    jwtProperties.temporaryTokenExpirationTime()
            );
        } catch (ExpiredJwtException e) {
            throw e;
        } catch (Exception e) {
            return null;
        }
    }

    public static String extractTemporaryTokenFromHeader(HttpServletRequest request) {
        return extractTokenFromAuthorizationHeader(request);
    }

    public static String extractTokenFromAuthorizationHeader(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
                .filter(header -> header.startsWith(TOKEN_PREFIX))
                .map(header -> header.replace(TOKEN_PREFIX, ""))
                .orElse(null);
    }

    public long getRefreshTokenExpirationTime() {
        return jwtProperties.refreshTokenExpirationTime();
    }

    private String buildJwtToken(TokenType tokenType, String subject,
                                 Map<String, Object> claims, TokenTimeInfo timeInfo, Key signingKey) {
        var builder = Jwts.builder()
                .setHeader(createTokenHeader(tokenType))
                .setSubject(subject)
                .setIssuedAt(timeInfo.issuedAt())
                .setExpiration(timeInfo.expiredAt())
                .signWith(signingKey);

        claims.forEach(builder::claim);

        return builder.compact();
    }

    private TokenTimeInfo createTokenTimeInfo(long expirationMillis) {
        Date issuedAt = new Date();
        Date expiredAt = new Date(issuedAt.getTime() + expirationMillis);
        return new TokenTimeInfo(issuedAt, expiredAt);
    }

    private Map<String, Object> createTokenHeader(TokenType tokenType) {
        return Map.of(
                HEADER_TYP, JWT_TYPE,
                HEADER_ALG, HS256_ALG,
                HEADER_REG_DATE, System.currentTimeMillis(),
                TOKEN_TYPE_KEY_NAME, tokenType.getValue()
        );
    }

    private String buildRefreshToken(Long memberId, Date issuedAt, Date expiredAt) {
        return Jwts.builder()
                .setHeader(createTokenHeader(TokenType.REFRESH))
                .setSubject(memberId.toString())
                .setIssuedAt(issuedAt)
                .setExpiration(expiredAt)
                .signWith(getRefreshTokenKey())
                .compact();
    }

    private Jws<Claims> parseTokenClaims(String token, Key key) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }

    private Key getAccessTokenKey() {
        return Keys.hmacShaKeyFor(jwtProperties.accessTokenSecret().getBytes());
    }

    private Key getRefreshTokenKey() {
        return Keys.hmacShaKeyFor(jwtProperties.refreshTokenSecret().getBytes());
    }

    private Key getTemporaryTokenKey() {
        return Keys.hmacShaKeyFor(jwtProperties.temporaryTokenSecret().getBytes());
    }

    private record TokenTimeInfo(Date issuedAt, Date expiredAt) {
    }
}
