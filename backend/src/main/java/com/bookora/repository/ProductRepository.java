package com.bookora.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bookora.model.Product;

import jakarta.persistence.LockModeType;

public interface ProductRepository extends JpaRepository<Product, Long> {

	/** Locks the rows (always in id order, so two checkouts cannot block each other forever) while stock is checked and reduced. */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select p from Product p where p.id in :ids order by p.id")
	List<Product> findAllForUpdate(@Param("ids") Collection<Long> ids);

}
