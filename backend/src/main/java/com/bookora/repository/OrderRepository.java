package com.bookora.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bookora.model.CustomerOrder;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {

	List<CustomerOrder> findByUserIdOrderByCreatedAtDesc(Long userId);

	Optional<CustomerOrder> findByOrderNumberAndUserId(String orderNumber, Long userId);

	/** Unpaid orders older than the cutoff that have no payment session still open. */
	@Query("""
			select o from CustomerOrder o
			where o.status = com.bookora.model.OrderStatus.PENDING_PAYMENT
			  and o.createdAt < :cutoff
			  and not exists (select p from Payment p where p.order = o
			      and p.status = com.bookora.model.PaymentStatus.PENDING and p.expiresAt > :now)
			""")
	List<CustomerOrder> findStaleUnpaid(@Param("cutoff") Instant cutoff, @Param("now") Instant now);

	@Query(value = "select nextval('order_number_seq')", nativeQuery = true)
	long nextOrderSequence();

}
