package com.bookora.service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.bookora.dto.BookDetailDto;
import com.bookora.dto.BookSummaryDto;
import com.bookora.dto.BookSummaryDto.Availability;
import com.bookora.dto.CategoryDto;
import com.bookora.dto.ProductDto;
import com.bookora.dto.ReviewDto;
import com.bookora.model.Book;
import com.bookora.model.BookImage;
import com.bookora.model.Product;
import com.bookora.model.ProductFormat;
import com.bookora.model.Review;
import com.bookora.repository.RatingSummary;

@Component
public class BookMapper {

	static final String CURRENCY = "EUR";

	public BookSummaryDto toSummary(Book book) {
		return toSummary(book, List.of(ProductFormat.values()));
	}

	/** Price and availability only look at the given formats, so an "E-books" search shows the e-book price. */
	public BookSummaryDto toSummary(Book book, List<ProductFormat> formats) {
		List<Product> products = activeProducts(book).stream()
				.filter(p -> formats.contains(p.getFormat()))
				.toList();
		BigDecimal price = products.stream()
				.map(Product::getPrice)
				.min(Comparator.naturalOrder())
				.orElse(null);
		return new BookSummaryDto(book.getId(), book.getTitle(), book.getSlug(), book.getCoverImagePath(),
				book.getAuthorNames(), price, CURRENCY, availability(products));
	}

	public BookDetailDto toDetail(Book book, RatingSummary rating, List<Review> reviews, Set<Long> buyerIds) {
		List<CategoryDto> categories = book.getCategories().stream().map(CategoryDto::from).toList();
		List<ProductDto> products = activeProducts(book).stream()
				.map(p -> new ProductDto(p.getId(), p.getSku(), p.getFormat(), p.getIsbn(), p.getPrice(), CURRENCY,
						p.getFormat().isDigital() || p.getStockQuantity() > 0))
				.toList();
		List<ReviewDto> reviewDtos = reviews.stream()
				.map(r -> new ReviewDto(r.getRating(), r.getTitle(), r.getComment(), r.getUser().getFirstName(),
						buyerIds.contains(r.getUser().getId()), r.getCreatedAt()))
				.toList();
		return new BookDetailDto(book.getId(), book.getTitle(), book.getSlug(), book.getDescription(),
				book.getCoverImagePath(), book.getImages().stream().map(BookImage::getPath).toList(),
				book.getPublisher(), book.getPublishedOn(), book.getLanguageCode(),
				book.getPageCount(), book.getAuthorNames(), categories, products,
				rating == null ? null : rating.getAverageRating(),
				rating == null ? 0 : rating.getReviewCount(), reviewDtos);
	}

	private List<Product> activeProducts(Book book) {
		return book.getProducts().stream()
				.filter(Product::isActive)
				.sorted(Comparator.comparing(Product::getPrice))
				.toList();
	}

	private Availability availability(List<Product> products) {
		boolean printedInStock = products.stream()
				.anyMatch(p -> !p.getFormat().isDigital() && p.getStockQuantity() > 0);
		if (printedInStock) {
			return Availability.IN_STOCK;
		}
		boolean digital = products.stream().anyMatch(p -> p.getFormat().isDigital());
		return digital ? Availability.INSTANT_DOWNLOAD : Availability.OUT_OF_STOCK;
	}

}
