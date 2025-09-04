package org.carefreepass.com.carefreepassserver.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record TokenPairResponse(
        @Schema(description = "액세스 토큰", defaultValue = "accessToken") String accessToken,
        @Schema(description = "리프레시 토큰", defaultValue = "refreshToken") String refreshToken,
        @Schema(description = "회원 ID", example = "1") Long memberId,
        @Schema(description = "회원 이름", example = "김철수") String memberName,
        @Schema(description = "회원 역할", example = "ROLE_USER") String role,
        @Schema(description = "전화번호", example = "010-1234-5678") String phoneNumber,
        @Schema(description = "이메일", example = "patient@example.com") String email,
        @Schema(description = "회원 상태", example = "ACTIVE") String status,
        @Schema(description = "생년월일", example = "1990-01-01") String birthDate,
        @Schema(description = "성별", example = "남성") String gender,
        @Schema(description = "병원명", example = "서울대학교병원") String hospitalName,
        @Schema(description = "병원 주소", example = "서울시 종로구") String hospitalAddress,
        @Schema(description = "회원가입 일시") LocalDateTime createdAt,
        @Schema(description = "정보수정 일시") LocalDateTime updatedAt
) {
    public static TokenPairResponse of(final String accessToken, final String refreshToken, 
                                       final Long memberId, final String memberName, final String role,
                                       final String phoneNumber, final String email, final String status,
                                       final String birthDate, final String gender,
                                       final String hospitalName, final String hospitalAddress,
                                       final LocalDateTime createdAt, final LocalDateTime updatedAt) {
        return new TokenPairResponse(accessToken, refreshToken, memberId, memberName, role,
                phoneNumber, email, status, birthDate, gender, hospitalName, hospitalAddress,
                createdAt, updatedAt);
    }
    
    // 기존 메서드 호환성을 위한 오버로드
    public static TokenPairResponse of(final String accessToken, final String refreshToken, final Long memberId, final String memberName, final String role) {
        return new TokenPairResponse(accessToken, refreshToken, memberId, memberName, role,
                null, null, null, null, null, null, null, null, null);
    }
    
    public static TokenPairResponse of(final String accessToken, final String refreshToken) {
        return new TokenPairResponse(accessToken, refreshToken, null, null, null,
                null, null, null, null, null, null, null, null, null);
    }
}