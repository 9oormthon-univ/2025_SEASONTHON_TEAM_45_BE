package org.carefreepass.com.carefreepassserver.domain.auth.repository;

import org.carefreepass.com.carefreepassserver.domain.auth.entity.domain.RefreshToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {
}
