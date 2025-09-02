package org.carefreepass.com.carefreepassserver.domain.auth.repository;

import org.carefreepass.com.carefreepassserver.domain.auth.entity.VerificationCode;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationCodeRepository extends CrudRepository<VerificationCode, String> {
}