package org.carefreepass.com.carefreepassserver.domain.member.repository;

import java.util.Optional;
import org.carefreepass.com.carefreepassserver.domain.member.entity.PatientProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientProfileRepository extends JpaRepository<PatientProfile, Long> {
    Optional<PatientProfile> findByMemberPhoneNumber(String phoneNumber);
}
