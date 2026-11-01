package com.bookora.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bookora.model.Payment;
import com.bookora.model.PaymentStatus;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

	Optional<Payment> findByProviderSessionId(String providerSessionId);

	Optional<Payment> findFirstByOrderIdAndStatusOrderByIdDesc(Long orderId, PaymentStatus status);

	boolean existsByOrderIdAndStatusAndExpiresAtAfter(Long orderId, PaymentStatus status, Instant now);

}
