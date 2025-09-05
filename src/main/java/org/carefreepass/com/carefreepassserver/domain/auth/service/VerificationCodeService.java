package org.carefreepass.com.carefreepassserver.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.response.TemporaryTokenResponse;
import org.carefreepass.com.carefreepassserver.domain.auth.entity.TemporaryMember;
import org.carefreepass.com.carefreepassserver.domain.auth.repository.TemporaryMemberRepository;
import org.carefreepass.com.carefreepassserver.golbal.error.BusinessException;
import org.carefreepass.com.carefreepassserver.golbal.error.ErrorCode;
import org.carefreepass.com.carefreepassserver.golbal.security.JwtTokenProvider;
import org.carefreepass.com.carefreepassserver.domain.auth.repository.VerificationCodeRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VerificationCodeService {

    private final VerificationCodeRepository verificationCodeRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final TemporaryMemberRepository temporaryMemberRepository;

    public TemporaryTokenResponse verifyAndIssueTemporaryToken(String phoneNumber, String code) {
        validateVerificationCode(phoneNumber, code);

        TemporaryMember temp = createTemporaryMember(phoneNumber);

        cleanupVerificationData(phoneNumber);

        return jwtTokenProvider.generateTemporaryToken(temp);
    }

    private void validateVerificationCode(String phoneNumber, String code) {
        if (!verifyCode(phoneNumber, code)) {
            throw new BusinessException(ErrorCode.SMS_VERIFICATION_CODE_MISMATCH);
        }
    }

    private boolean verifyCode(final String phoneNumber, final String inputCode) {
        return verificationCodeRepository.findById(phoneNumber)
                .map(verification -> verification.getCode().equals(inputCode))
                .orElseThrow(() -> new BusinessException(ErrorCode.SMS_VERIFICATION_CODE_NOT_FOUND));
    }

    private TemporaryMember createTemporaryMember(String phoneNumber) {
        TemporaryMember temp = TemporaryMember.createTemporaryMember(phoneNumber);
        return temporaryMemberRepository.save(temp);
    }

    private void cleanupVerificationData(String phoneNumber) {
        verificationCodeRepository.deleteById(phoneNumber);
    }
}
