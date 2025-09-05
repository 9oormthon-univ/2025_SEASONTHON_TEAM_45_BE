package org.carefreepass.com.carefreepassserver.golbal.util;


import lombok.RequiredArgsConstructor;
import org.carefreepass.com.carefreepassserver.domain.auth.entity.Member;
import org.carefreepass.com.carefreepassserver.domain.auth.repository.MemberRepository;
import org.carefreepass.com.carefreepassserver.golbal.error.BusinessException;
import org.carefreepass.com.carefreepassserver.golbal.error.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class MemberUtil {

    private final SecurityUtil securityUtil;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public Member getCurrentMember() {
        return findMemberOrThrow(securityUtil.getCurrentMemberId());
    }

    @Transactional(readOnly = true)
    public Member getMemberByMemberId(final Long memberId) {
        return findMemberOrThrow(memberId);
    }

    private Member findMemberOrThrow(final Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public String getMemberRole() {
        String role = securityUtil.getCurrentMemberRole();
        if (role == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return role;
    }

}
