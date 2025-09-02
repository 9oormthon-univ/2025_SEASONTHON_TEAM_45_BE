package org.carefreepass.com.carefreepassserver.domain.auth.repository;

import org.carefreepass.com.carefreepassserver.domain.auth.entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * 리프레시 토큰 리포지토리
 * JWT 리프레시 토큰 정보에 대한 데이터베이스 접근을 담당합니다.
 * Redis 기반 저장소를 사용하여 토큰의 생성, 조회, 삭제, TTL 관리를 제공합니다.
 * 
 * CrudRepository를 상속받아 기본적인 CRUD 연산을 지원하며,
 * Redis의 특성을 활용한 자동 만료 기능을 제공합니다.
 */
@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {
}
