package org.carefreepass.com.carefreepassserver.domain.notification.repository;

import java.util.List;
import java.util.Optional;
import org.carefreepass.com.carefreepassserver.domain.notification.entity.DeviceToken;
import org.carefreepass.com.carefreepassserver.golbal.domain.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    Optional<DeviceToken> findByMemberIdAndStatus(Long memberId, Status status);

    List<DeviceToken> findByMemberIdAndStatusOrderByLastUsedAtDesc(Long memberId, Status status);

    Optional<DeviceToken> findByFcmTokenAndStatus(String fcmToken, Status status);

    @Modifying
    @Query("UPDATE DeviceToken d SET d.status = :status WHERE d.member.id = :memberId")
    int deactivateAllTokensByMemberId(@Param("memberId") Long memberId, @Param("status") Status status);

    @Modifying
    @Query("UPDATE DeviceToken d SET d.status = :newStatus WHERE d.fcmToken = :fcmToken")
    int updateTokenStatus(@Param("fcmToken") String fcmToken, @Param("newStatus") Status newStatus);

    boolean existsByFcmTokenAndStatus(String fcmToken, Status status);

    @Query("SELECT d FROM DeviceToken d WHERE d.member.id = :memberId AND d.status = :status")
    Optional<DeviceToken> findActiveTokenByMemberId(@Param("memberId") Long memberId, @Param("status") Status status);
}