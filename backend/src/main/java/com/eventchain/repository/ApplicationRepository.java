package com.eventchain.repository;

import com.eventchain.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByEventIdOrderByCreatedAtAsc(Long eventId);
    List<Application> findByEventIdAndStatus(Long eventId, String status);
    Optional<Application> findByUserIdAndEventId(Long userId, Long eventId);
    boolean existsByUserIdAndEventId(Long userId, Long eventId);
    List<Application> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Application a WHERE a.event.id = :eventId AND a.status = 'APPLIED' ORDER BY a.createdAt")
    List<Application> findApplicantsForLottery(@Param("eventId") Long eventId);
}
