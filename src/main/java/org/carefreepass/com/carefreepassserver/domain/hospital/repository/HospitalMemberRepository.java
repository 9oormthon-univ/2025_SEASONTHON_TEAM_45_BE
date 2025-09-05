package org.carefreepass.com.carefreepassserver.domain.hospital.repository;

import java.util.Optional;
import org.carefreepass.com.carefreepassserver.domain.hospital.entity.HospitalMember;
import org.carefreepass.com.carefreepassserver.domain.auth.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HospitalMemberRepository extends JpaRepository<HospitalMember, Long> {
    Optional<HospitalMember> findByMember(Member member);
}
