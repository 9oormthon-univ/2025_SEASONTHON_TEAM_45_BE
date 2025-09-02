package org.carefreepass.com.carefreepassserver.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.request.HospitalSignInRequest;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.request.HospitalSignUpRequest;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.response.TokenPairResponse;
import org.carefreepass.com.carefreepassserver.domain.hospital.entity.Hospital;
import org.carefreepass.com.carefreepassserver.domain.hospital.entity.HospitalMember;
import org.carefreepass.com.carefreepassserver.domain.hospital.repository.HospitalMemberRepository;
import org.carefreepass.com.carefreepassserver.domain.hospital.repository.HospitalRepository;
import org.carefreepass.com.carefreepassserver.domain.member.entity.Member;
import org.carefreepass.com.carefreepassserver.domain.member.entity.MemberRole;
import org.carefreepass.com.carefreepassserver.domain.member.repository.MemberRepository;
import org.carefreepass.com.carefreepassserver.golbal.error.BusinessException;
import org.carefreepass.com.carefreepassserver.golbal.error.ErrorCode;
import org.carefreepass.com.carefreepassserver.golbal.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HospitalAuthService {

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    private final MemberRepository memberRepository;
    private final HospitalRepository hospitalRepository;
    private final HospitalMemberRepository hospitalMemberRepository;

    public TokenPairResponse hospitalSignUpWithLocal(HospitalSignUpRequest request) {
        Member admin = Member.createHospitalAdmin(
                request.adminName(),
                request.adminEmail(),
                passwordEncoder.encode(request.adminPassword())
        );
        memberRepository.save(admin);

        Hospital hospital = Hospital.createHospital(
                request.hospitalName(),
                request.hospitalAddress()
        );
        hospitalRepository.save(hospital);

        HospitalMember hospitalMember = HospitalMember.createHospitalMember(hospital, admin, request.adminEmail());
        hospitalMemberRepository.save(hospitalMember);

        return jwtTokenProvider.generateTokenPair(admin.getId(), admin.getName(), admin.getRole());
    }

    @Transactional(readOnly = true)
    public TokenPairResponse hospitalSignInWithLocal(HospitalSignInRequest request) {
        Member admin = memberRepository.findByEmail(request.adminEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (!passwordEncoder.matches(request.adminPassword(), admin.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        return jwtTokenProvider.generateTokenPair(admin.getId(), admin.getName(), admin.getRole());
    }

}
