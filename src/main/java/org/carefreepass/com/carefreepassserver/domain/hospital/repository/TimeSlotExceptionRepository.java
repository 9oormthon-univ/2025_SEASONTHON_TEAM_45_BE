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

// 시간대 예외 리포지토리
public interface TimeSlotExceptionRepository extends JpaRepository<TimeSlotException, Long> {

    // 진료과의 특정 날짜 차단된 시간 목록 조회
    @Query("SELECT tse.exceptionTime FROM TimeSlotException tse WHERE tse.hospitalDepartment = :department AND tse.exceptionDate = :date AND tse.blocked = true ORDER BY tse.exceptionTime")
    List<LocalTime> findBlockedTimesByDepartmentAndDate(@Param("department") HospitalDepartment hospitalDepartment, @Param("date") LocalDate exceptionDate);

    // 진료과의 특정 날짜 모든 시간 예외 조회
    List<TimeSlotException> findByHospitalDepartmentAndExceptionDate(HospitalDepartment hospitalDepartment, LocalDate exceptionDate);

    // 특정 진료과, 날짜, 시간의 예외 조회
    Optional<TimeSlotException> findByHospitalDepartmentAndExceptionDateAndExceptionTime(
            HospitalDepartment hospitalDepartment, LocalDate exceptionDate, LocalTime exceptionTime);

    // 특정 진료과, 날짜, 시간의 차단 여부 확인
    boolean existsByHospitalDepartmentAndExceptionDateAndExceptionTimeAndBlockedTrue(
            HospitalDepartment hospitalDepartment, LocalDate exceptionDate, LocalTime exceptionTime);


}