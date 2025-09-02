package org.carefreepass.com.carefreepassserver.domain.hospital.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.carefreepass.com.carefreepassserver.domain.hospital.entity.HospitalDepartment;
import org.carefreepass.com.carefreepassserver.domain.hospital.entity.TimeSlotException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 시간대 예외 리포지토리
 * 특정 시간대 차단 정보에 대한 데이터베이스 접근을 담당합니다.
 */
public interface TimeSlotExceptionRepository extends JpaRepository<TimeSlotException, Long> {

    /**
     * 진료과의 특정 날짜 차단된 시간 목록 조회
     * 
     * @param hospitalDepartment 진료과 엔티티
     * @param exceptionDate 조회할 날짜
     * @return 차단된 시간 목록
     */
    @Query("SELECT tse.exceptionTime FROM TimeSlotException tse WHERE tse.hospitalDepartment = :department AND tse.exceptionDate = :date AND tse.blocked = true ORDER BY tse.exceptionTime")
    List<LocalTime> findBlockedTimesByDepartmentAndDate(@Param("department") HospitalDepartment hospitalDepartment, @Param("date") LocalDate exceptionDate);

    /**
     * 진료과의 특정 날짜 모든 시간 예외 조회
     * 
     * @param hospitalDepartment 진료과 엔티티
     * @param exceptionDate 조회할 날짜
     * @return 시간 예외 목록
     */
    List<TimeSlotException> findByHospitalDepartmentAndExceptionDate(HospitalDepartment hospitalDepartment, LocalDate exceptionDate);

    /**
     * 특정 진료과, 날짜, 시간의 예외 조회
     * 
     * @param hospitalDepartment 진료과 엔티티
     * @param exceptionDate 날짜
     * @param exceptionTime 시간
     * @return 시간 예외 엔티티 (Optional)
     */
    Optional<TimeSlotException> findByHospitalDepartmentAndExceptionDateAndExceptionTime(
            HospitalDepartment hospitalDepartment, LocalDate exceptionDate, LocalTime exceptionTime);

    /**
     * 특정 진료과, 날짜, 시간의 차단 여부 확인
     * 
     * @param hospitalDepartment 진료과 엔티티
     * @param exceptionDate 날짜
     * @param exceptionTime 시간
     * @return 차단 여부
     */
    boolean existsByHospitalDepartmentAndExceptionDateAndExceptionTimeAndBlockedTrue(
            HospitalDepartment hospitalDepartment, LocalDate exceptionDate, LocalTime exceptionTime);


}