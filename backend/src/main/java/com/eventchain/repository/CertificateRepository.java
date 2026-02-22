package com.eventchain.repository;

import com.eventchain.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    Optional<Certificate> findByCertificateId(String certificateId);
    Optional<Certificate> findByTicketId(Long ticketId);
    long countByEventId(Long eventId);
}
