package com.bookora.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.bookora.exception.ResourceNotFoundException;
import com.bookora.model.BookSort;
import com.bookora.model.ProductFormat;
import com.bookora.repository.BookRepository;
import com.bookora.repository.ReviewRepository;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

	@Mock
	private BookRepository books;

	@Mock
	private ReviewRepository reviews;

	private BookServiceImpl service() {
		return new BookServiceImpl(books, reviews, new BookMapper());
	}

	@Test
	void getBySlugThrowsWhenBookIsMissing() {
		when(books.findBySlugAndActiveTrue("nope")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service().getBySlug("nope"))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("nope");
	}

	@Test
	void featuredReturnsEmptyListWhenNothingIsFeatured() {
		when(books.findByFeaturedTrueAndActiveTrueOrderByTitleAsc()).thenReturn(List.of());

		assertThat(service().findFeatured()).isEmpty();
	}

	@Test
	void searchTurnsBlankFiltersIntoNoFilterAndEscapesWildcards() {
		var pageable = PageRequest.of(0, 12);
		when(books.search(any(), any(), any(), any(), any())).thenReturn(new PageImpl<>(List.of()));

		service().search("  50%_Off ", " ", null, BookSort.TITLE, pageable);

		verify(books).search(eq("%50\\%\\_off%"), eq(""), eq(List.of(ProductFormat.values())), eq("TITLE"), eq(pageable));
	}

	@Test
	void ebookFilterMeansPdfAndEpub() {
		var pageable = PageRequest.of(0, 12);
		when(books.search(any(), any(), any(), any(), any())).thenReturn(new PageImpl<>(List.of()));

		service().search(null, null, "ebook", BookSort.PRICE_ASC, pageable);

		verify(books).search(eq(""), eq(""), eq(List.of(ProductFormat.PDF, ProductFormat.EPUB)), eq("PRICE_ASC"), eq(pageable));
	}

}
