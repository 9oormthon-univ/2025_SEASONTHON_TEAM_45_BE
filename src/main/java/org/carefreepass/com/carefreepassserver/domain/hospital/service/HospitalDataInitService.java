package org.carefreepass.com.carefreepassserver.domain.hospital.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.carefreepass.com.carefreepassserver.domain.hospital.entity.Hospital;
import org.carefreepass.com.carefreepassserver.domain.hospital.entity.HospitalDepartment;
import org.carefreepass.com.carefreepassserver.domain.hospital.repository.HospitalDepartmentRepository;
import org.carefreepass.com.carefreepassserver.domain.hospital.repository.HospitalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


// 병원 및 진료과 초기 데이터 생성 서비스
@Service
@RequiredArgsConstructor
@Slf4j
public class HospitalDataInitService {
    
    private final HospitalRepository hospitalRepository;
    private final HospitalDepartmentRepository hospitalDepartmentRepository;
    
    // 애플리케이션 시작 시 병원 데이터 확인/초기화
    @PostConstruct
    @Transactional
    public void initializeHospitalData() {
        try {
            // 병원이 없으면 생성
            if (hospitalRepository.count() == 0) {
                createMainHospital();
                log.info("병원 데이터 초기화가 완료되었습니다.");
            } else {
                Hospital hospital = hospitalRepository.findById(1L)
                    .orElse(null);
                if (hospital != null) {
                    log.info("기존 병원 확인됨: {}", hospital.getName());
                }
                log.info("병원 데이터가 이미 존재합니다.");
            }
            
        } catch (Exception e) {
            log.error("병원 데이터 초기화 중 오류 발생", e);
        }
    }
    
    // 메인 병원 생성
    private Hospital createMainHospital() {
        Hospital hospital = Hospital.createHospital(
            "서울대병원",
            "서울특별시 종로구 대학로 103"
        );
        
        Hospital savedHospital = hospitalRepository.save(hospital);
        log.info("병원 생성 완료: {} (ID: {})", savedHospital.getName(), savedHospital.getId());
        
        return savedHospital;
    }
    
}