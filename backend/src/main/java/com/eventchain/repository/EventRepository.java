package com.eventchain.repository;

import com.eventchain.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    Page<Event> findByStatus(String status, Pageable pageable);
    List<Event> findByStatusOrderByEventDateAsc(String status);
    List<Event> findByEventDateAfterAndStatus(LocalDateTime date, String status);
}
