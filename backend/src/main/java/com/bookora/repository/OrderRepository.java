package com.bookora.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bookora.model.CustomerOrder;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {

	List<CustomerOrder> findByUserIdOrderByCreatedAtDesc(Long userId);

	Optional<CustomerOrder> findByOrderNumberAndUserId(String orderNumber, Long userId);

	@Query(value = "select nextval('order_number_seq')", nativeQuery = true)
	long nextOrderSequence();

}
