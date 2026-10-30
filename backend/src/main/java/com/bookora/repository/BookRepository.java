package com.bookora.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bookora.model.Book;
import com.bookora.model.ProductFormat;

public interface BookRepository extends JpaRepository<Book, Long> {

	List<Book> findByFeaturedTrueAndActiveTrueOrderByTitleAsc();

	@Query(value = "select average_rating as averageRating, review_count as reviewCount "
			+ "from book_rating_view where book_id = :bookId", nativeQuery = true)
	Optional<RatingSummary> findRating(@Param("bookId") Long bookId);

	Optional<Book> findBySlugAndActiveTrue(String slug);

	/**
	 * Empty strings mean "no filter" so the query never binds untyped nulls.
	 * {@code query} must already be lower-cased and wrapped in % wildcards.
	 * A book matches when it has an active product in one of the given formats.
	 * {@code sort} is a {@link com.bookora.model.BookSort} name. Each CASE below is empty unless it is the chosen
	 * order, so only that one decides; books with no reviews come last when sorting by rating.
	 */
	@Query("""
			select b from Book b
			where b.active = true
			  and (:query = '' or lower(b.title) like :query)
			  and (:category = '' or exists (select 1 from b.categories c where c.slug = :category))
			  and exists (select 1 from b.products p where p.active = true and p.format in :formats)
			order by
			  case when :sort = 'PRICE_ASC' then
			    (select min(p1.price) from Product p1 where p1.book = b and p1.active = true and p1.format in :formats)
			  end asc,
			  case when :sort = 'PRICE_DESC' then
			    (select min(p2.price) from Product p2 where p2.book = b and p2.active = true and p2.format in :formats)
			  end desc,
			  case when :sort = 'RATING' then
			    (select avg(r1.rating) from Review r1
			     where r1.book = b and r1.status = com.bookora.model.ReviewStatus.APPROVED)
			  end desc nulls last,
			  case when :sort = 'RATING' then
			    (select count(r2) from Review r2
			     where r2.book = b and r2.status = com.bookora.model.ReviewStatus.APPROVED)
			  end desc,
			  case when :sort = 'NEWEST' then b.createdAt end desc,
			  b.title asc
			""")
	Page<Book> search(@Param("query") String query, @Param("category") String category,
			@Param("formats") List<ProductFormat> formats, @Param("sort") String sort, Pageable pageable);

}
