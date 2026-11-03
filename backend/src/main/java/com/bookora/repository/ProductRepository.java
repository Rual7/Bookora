package com.bookora.repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bookora.model.Product;

import jakarta.persistence.LockModeType;

public interface ProductRepository extends JpaRepository<Product, Long> {

	Optional<Product> findByIsbn(String isbn);

	/** Locks the rows (always in id order, so two checkouts cannot block each other forever) while stock is checked and reduced. */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select p from Product p where p.id in :ids order by p.id")
	List<Product> findAllForUpdate(@Param("ids") Collection<Long> ids);

	@Query("""
			select new com.bookora.repository.DigitalProduct(p.id, b.slug, p.format) from Product p join p.book b
			where p.active = true and b.active = true
			  and p.format in (com.bookora.model.ProductFormat.PDF, com.bookora.model.ProductFormat.EPUB)
			order by p.id
			""")
	List<DigitalProduct> findDigitalOnSale();

	/** The locked products by id. */
	default Map<Long, Product> lockAllById(Collection<Long> ids) {
		return findAllForUpdate(ids).stream().collect(Collectors.toMap(Product::getId, Function.identity()));
	}

}
