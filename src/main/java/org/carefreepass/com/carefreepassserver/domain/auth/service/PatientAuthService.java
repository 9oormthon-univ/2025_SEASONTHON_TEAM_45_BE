package org.carefreepass.com.carefreepassserver.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.request.PatientSignInRequest;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.request.PatientSignUpRequest;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.response.TokenPairResponse;
import org.carefreepass.com.carefreepassserver.domain.member.entity.Gender;
import org.carefreepass.com.carefreepassserver.domain.member.entity.Member;
import org.carefreepass.com.carefreepassserver.domain.member.entity.MemberRole;
import org.carefreepass.com.carefreepassserver.domain.member.entity.PatientProfile;
import org.carefreepass.com.carefreepassserver.domain.member.repository.MemberRepository;
import org.carefreepass.com.carefreepassserver.domain.member.repository.PatientProfileRepository;
import org.carefreepass.com.carefreepassserver.golbal.error.BusinessException;
import org.carefreepass.com.carefreepassserver.golbal.error.ErrorCode;
import org.carefreepass.com.carefreepassserver.golbal.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PatientAuthService {
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final PatientProfileRepository patientProfileRepository;

    public TokenPairResponse patientSignUpWithLocal(PatientSignUpRequest request) {
        if (memberRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new BusinessException(ErrorCode.ALREADY_REGISTERED_PHONE_NUMBER);
        }

        Member member = Member.createPatient(
                request.name(),
                request.phoneNumber(),
                passwordEncoder.encode(request.password())
        );
        memberRepository.save(member);

        PatientProfile patientProfile = PatientProfile.createPatientProfile(member, request.birthDate(),
                Gender.from(request.gender()));
        patientProfileRepository.save(patientProfile);

        return jwtTokenProvider.generateTokenPair(member.getId(), member.getRole());
    }

    public TokenPairResponse patientSignInWithLocal(PatientSignInRequest request) {
        Member member = memberRepository.findByPhoneNumber(request.phoneNumber())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        return jwtTokenProvider.generateTokenPair(member.getId(), member.getRole());
    }
}
