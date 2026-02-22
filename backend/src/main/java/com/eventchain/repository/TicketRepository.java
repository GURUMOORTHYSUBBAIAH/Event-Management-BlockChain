package com.eventchain.repository;

import com.eventchain.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByEventId(Long eventId);
    List<Ticket> findByUserId(Long userId);
    Optional<Ticket> findByEventIdAndTokenId(Long eventId, Long tokenId);
    long countByEventId(Long eventId);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.event.id = :eventId AND t.checkedIn = :checkedIn")
    long countByEventIdAndCheckedIn(@Param("eventId") Long eventId, @Param("checkedIn") Boolean checkedIn);
}
