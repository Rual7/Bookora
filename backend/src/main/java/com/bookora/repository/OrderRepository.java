package com.bookora.repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bookora.model.CustomerOrder;
import com.bookora.model.OrderItem;
import com.bookora.model.OrderStatus;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {

	List<CustomerOrder> findByUserIdOrderByCreatedAtDesc(Long userId);

	@Query("""
			select i from OrderItem i join fetch i.order o join fetch i.product p join fetch p.book
			where o.user.id = :userId
			  and i.productFormat in (com.bookora.model.ProductFormat.PDF, com.bookora.model.ProductFormat.EPUB)
			  and o.status in (com.bookora.model.OrderStatus.PAID, com.bookora.model.OrderStatus.SHIPPED,
			      com.bookora.model.OrderStatus.DELIVERED)
			order by o.paidAt desc, i.id desc
			""")
	List<OrderItem> findPaidEbookItems(@Param("userId") Long userId);

	@Query("select i from OrderItem i where i.id = :itemId and i.order.user.id = :userId")
	Optional<OrderItem> findItemOfUser(@Param("itemId") Long itemId, @Param("userId") Long userId);

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

	/** Ids of the customers who paid for any format of the book; used for the "Verified purchase" mark. */
	@Query("""
			select distinct o.user.id from CustomerOrder o join o.items i
			where i.product.book.id = :bookId
			  and o.status in (com.bookora.model.OrderStatus.PAID, com.bookora.model.OrderStatus.SHIPPED,
			      com.bookora.model.OrderStatus.DELIVERED)
			""")
	List<Long> findBuyerIds(@Param("bookId") Long bookId);

	@Query("""
			select count(o) > 0 from CustomerOrder o join o.items i
			where o.user.id = :userId and i.product.book.id = :bookId
			  and o.status in (com.bookora.model.OrderStatus.PAID, com.bookora.model.OrderStatus.SHIPPED,
			      com.bookora.model.OrderStatus.DELIVERED)
			""")
	boolean hasBought(@Param("userId") Long userId, @Param("bookId") Long bookId);

	@Query("select o.user.id, count(o) from CustomerOrder o where o.user.id in :userIds group by o.user.id")
	List<Object[]> countByUsers(@Param("userIds") Collection<Long> userIds);

	Page<CustomerOrder> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

	Page<CustomerOrder> findAllByOrderByCreatedAtDesc(Pageable pageable);

	Optional<CustomerOrder> findByOrderNumber(String orderNumber);

	@Query(value = "select nextval('order_number_seq')", nativeQuery = true)
	long nextOrderSequence();

}
