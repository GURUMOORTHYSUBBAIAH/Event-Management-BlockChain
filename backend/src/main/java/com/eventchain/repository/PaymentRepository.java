package com.eventchain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.eventchain.entity.Payment;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);
    Optional<Payment> findByApplicationId(Long applicationId);
    
    @Query("SELECT p FROM Payment p WHERE p.application.event.id = :eventId AND p.status = 'COMPLETED'")
    List<Payment> findCompletedByEventId(@Param("eventId") Long eventId);
}
