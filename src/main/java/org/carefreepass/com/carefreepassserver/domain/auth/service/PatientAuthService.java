package org.carefreepass.com.carefreepassserver.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.request.PatientSignInRequest;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.request.PatientSignUpRequest;
import org.carefreepass.com.carefreepassserver.domain.auth.dto.response.TokenPairResponse;
import org.carefreepass.com.carefreepassserver.domain.auth.entity.TemporaryMember;
import org.carefreepass.com.carefreepassserver.domain.auth.entity.Gender;
import org.carefreepass.com.carefreepassserver.domain.auth.entity.Member;
import org.carefreepass.com.carefreepassserver.domain.auth.entity.MemberRole;
import org.carefreepass.com.carefreepassserver.domain.auth.entity.PatientProfile;
import org.carefreepass.com.carefreepassserver.domain.auth.repository.MemberRepository;
import org.carefreepass.com.carefreepassserver.domain.auth.repository.PatientProfileRepository;
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

    public TokenPairResponse patientSignUpWithLocal(TemporaryMember temporaryMember, PatientSignUpRequest request) {
        if (memberRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new BusinessException(ErrorCode.ALREADY_REGISTERED_PHONE_NUMBER);
        }

        if (!temporaryMember.getPhoneNumber().equals(request.phoneNumber())) {
            throw new BusinessException(ErrorCode.TEMPORARY_MEMBER_MISMATCH);
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

        return jwtTokenProvider.generateFullTokenPair(member, patientProfile, null);
    }

    public TokenPairResponse patientSignInWithLocal(PatientSignInRequest request) {
        Member member = memberRepository.findByPhoneNumber(request.phoneNumber())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        PatientProfile patientProfile = patientProfileRepository.findByMember(member).orElse(null);
        return jwtTokenProvider.generateFullTokenPair(member, patientProfile, null);
    }
}
