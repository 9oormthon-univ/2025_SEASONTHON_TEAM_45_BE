package org.carefreepass.com.carefreepassserver.domain.auth.repository;

import java.util.Optional;
import org.carefreepass.com.carefreepassserver.domain.auth.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// 회원 리포지토리
@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    
    // 전화번호 중복 확인
    boolean existsByPhoneNumber(String phoneNumber);
    
    // 전화번호로 회원 조회
    Optional<Member> findByPhoneNumber(String phoneNumber);

    // 이메일로 회원 조회
    Optional<Member> findByEmail(String email);
}
