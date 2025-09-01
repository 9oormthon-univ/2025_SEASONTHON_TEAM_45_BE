package org.carefreepass.com.carefreepassserver.domain.hospital.repository;

import org.carefreepass.com.carefreepassserver.domain.hospital.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HospitalRepository extends JpaRepository<Hospital, Long> {
}
