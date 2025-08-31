package org.carefreepass.com.carefreepassserver.domain.member.repository;

import java.util.Optional;
import org.carefreepass.com.carefreepassserver.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByPhoneNumber(String phoneNumber);
    Optional<Member> findByPhoneNumber(String phoneNumber);
}
