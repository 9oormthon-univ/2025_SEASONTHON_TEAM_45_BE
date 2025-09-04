package org.carefreepass.com.carefreepassserver.domain.hospital.repository;

import java.util.List;
import java.util.Optional;
import org.carefreepass.com.carefreepassserver.domain.hospital.entity.Hospital;
import org.carefreepass.com.carefreepassserver.domain.hospital.entity.HospitalDepartment;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 병원 진료과 리포지토리
 * 병원별 진료과 정보에 대한 데이터베이스 접근을 담당합니다.
 */
public interface HospitalDepartmentRepository extends JpaRepository<HospitalDepartment, Long> {

    /**
     * 병원의 활성화된 진료과 목록 조회
     * 
     * @param hospital 병원 엔티티
     * @return 활성화된 진료과 목록
     */
    List<HospitalDepartment> findByHospitalAndActiveTrue(Hospital hospital);



    /**
     * 병원ID와 진료과명으로 활성화된 진료과 조회
     * 
     * @param hospital 병원 엔티티
     * @param name 진료과명
     * @return 활성화된 진료과 엔티티 (Optional)
     */
    Optional<HospitalDepartment> findByHospitalAndNameAndActiveTrue(Hospital hospital, String name);

    /**
     * 병원의 진료과명 중복 확인
     * 
     * @param hospital 병원 엔티티
     * @param name 진료과명
     * @return 중복 여부
     */
    boolean existsByHospitalAndName(Hospital hospital, String name);

    /**
     * 모든 활성화된 진료과 조회 (전체 병원)
     * 
     * @return 모든 활성화된 진료과 목록
     */
    List<HospitalDepartment> findByActiveTrue();

}