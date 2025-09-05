package org.carefreepass.com.carefreepassserver.domain.hospital.repository;

import java.util.List;
import java.util.Optional;
import org.carefreepass.com.carefreepassserver.domain.hospital.entity.Hospital;
import org.carefreepass.com.carefreepassserver.domain.hospital.entity.HospitalDepartment;
import org.springframework.data.jpa.repository.JpaRepository;

// 병원 진료과 리포지토리
public interface HospitalDepartmentRepository extends JpaRepository<HospitalDepartment, Long> {

    // 병원의 활성화된 진료과 목록 조회
    List<HospitalDepartment> findByHospitalAndActiveTrue(Hospital hospital);



    // 병원ID와 진료과명으로 활성화된 진료과 조회
    Optional<HospitalDepartment> findByHospitalAndNameAndActiveTrue(Hospital hospital, String name);

    // 병원의 진료과명 중복 확인
    boolean existsByHospitalAndName(Hospital hospital, String name);

    // 모든 활성화된 진료과 조회 (전체 병원)
    List<HospitalDepartment> findByActiveTrue();

}