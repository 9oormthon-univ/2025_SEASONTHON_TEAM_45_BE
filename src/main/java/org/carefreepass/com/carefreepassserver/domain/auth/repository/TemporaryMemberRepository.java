package org.carefreepass.com.carefreepassserver.domain.auth.repository;

import org.carefreepass.com.carefreepassserver.domain.auth.entity.TemporaryMember;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TemporaryMemberRepository extends CrudRepository<TemporaryMember, String> {
}
