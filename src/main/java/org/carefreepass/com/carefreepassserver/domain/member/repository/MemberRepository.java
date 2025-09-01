package org.carefreepass.com.carefreepassserver.domain.member.repository;

import java.util.Optional;
import org.carefreepass.com.carefreepassserver.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 회원 리포지토리
 * 회원 정보에 대한 데이터베이스 접근을 담당합니다.
 * 회원가입, 로그인, 회원정보 조회 등의 기능을 제공합니다.
 */
@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    
    /**
     * 전화번호 중복 확인
     * 회원가입 시 동일한 전화번호가 이미 등록되어 있는지 확인합니다.
     * 
     * @param phoneNumber 확인할 전화번호
     * @return 존재 여부 (true: 존재함, false: 존재하지 않음)
     */
    boolean existsByPhoneNumber(String phoneNumber);
    
    /**
     * 전화번호로 회원 조회
     * 로그인 시 전화번호를 기반으로 회원 정보를 조회합니다.
     * 
     * @param phoneNumber 조회할 전화번호
     * @return 회원 정보 (존재하지 않을 경우 Optional.empty())
     */
    Optional<Member> findByPhoneNumber(String phoneNumber);

    /**
     * 이메일로 회원 조회
     * 이메일을 기반으로 회원 정보를 조회합니다.
     *
     * @param email 조회할 이메일
     * @return 회원 정보 (존재하지 않을 경우 Optional.empty())
     */
    Optional<Member> findByEmail(String email);
}
