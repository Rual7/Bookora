package com.bookora.service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.bookora.dto.AdminBookDto;
import com.bookora.dto.AdminBookRequest;
import com.bookora.dto.AdminBookRowDto;
import com.bookora.dto.AdminProductDto;
import com.bookora.dto.AdminProductRequest;
import com.bookora.dto.BookImageDto;
import com.bookora.dto.EbookFileDto;
import com.bookora.dto.PageDto;
import com.bookora.exception.BadRequestException;
import com.bookora.exception.FieldException;
import com.bookora.exception.ResourceNotFoundException;
import com.bookora.model.Author;
import com.bookora.model.Book;
import com.bookora.model.Category;
import com.bookora.model.EbookFile;
import com.bookora.model.Product;
import com.bookora.model.ProductFormat;
import com.bookora.repository.AuthorRepository;
import com.bookora.repository.BookRepository;
import com.bookora.repository.CategoryRepository;
import com.bookora.repository.EbookFileRepository;
import com.bookora.repository.ProductRepository;

@Service
public class AdminBookServiceImpl implements AdminBookService {

	private static final int MAX_IMAGES = 8;

	private final BookRepository books;
	private final ProductRepository products;
	private final AuthorRepository authors;
	private final CategoryRepository categories;
	private final EbookFileRepository ebookFiles;
	private final CoverStorage covers;

	public AdminBookServiceImpl(BookRepository books, ProductRepository products, AuthorRepository authors,
			CategoryRepository categories, EbookFileRepository ebookFiles, CoverStorage covers) {
		this.books = books;
		this.products = products;
		this.authors = authors;
		this.categories = categories;
		this.ebookFiles = ebookFiles;
		this.covers = covers;
	}

	@Override
	@Transactional(readOnly = true)
	public PageDto<AdminBookRowDto> list(String query, int page, int size) {
		String term = LikePattern.contains(query);
		return PageDto.of(books.searchAll(term, PageRequest.of(page, size)).map(AdminBookServiceImpl::toRow));
	}

	@Override
	@Transactional(readOnly = true)
	public AdminBookDto get(Long id) {
		return toDto(find(id));
	}

	@Override
	@Transactional
	public AdminBookDto create(AdminBookRequest request) {
		Book book = books.save(new Book(request.title().trim(), uniqueSlug(request.title())));
		apply(book, request);
		return toDto(book);
	}

	@Override
	@Transactional
	public AdminBookDto update(Long id, AdminBookRequest request) {
		Book book = find(id);
		apply(book, request);
		return toDto(book);
	}

	@Override
	@Transactional
	public AdminBookDto addImages(Long id, List<MultipartFile> images) {
		Book book = find(id);
		if (images.isEmpty()) {
			throw new BadRequestException("Choose at least one image");
		}
		if (book.getImages().size() + images.size() > MAX_IMAGES) {
			throw new BadRequestException("A book can have at most " + MAX_IMAGES + " images");
		}
		for (MultipartFile image : images) {
			book.addImage(covers.store(image));
		}
		return toDto(books.saveAndFlush(book));
	}

	@Override
	@Transactional
	public AdminBookDto removeImage(Long id, Long imageId) {
		Book book = find(id);
		String path = book.removeImage(imageId);
		if (path == null) {
			throw new ResourceNotFoundException("Image not found");
		}
		covers.delete(path);
		return toDto(books.saveAndFlush(book));
	}

	@Override
	@Transactional
	public AdminBookDto makeMainImage(Long id, Long imageId) {
		Book book = find(id);
		if (!book.makeMainImage(imageId)) {
			throw new ResourceNotFoundException("Image not found");
		}
		return toDto(books.saveAndFlush(book));
	}

	private Book find(Long id) {
		return books.findById(id).orElseThrow(() -> new ResourceNotFoundException("Book not found"));
	}

	private void apply(Book book, AdminBookRequest request) {
		book.update(request.title().trim(), blankToNull(request.description()), blankToNull(request.publisher()),
				request.publishedOn(), blankToNull(request.languageCode()), request.pageCount(), request.featured(),
				request.active());

		book.getAuthors().clear();
		for (String name : request.authors()) {
			book.getAuthors().add(findOrCreateAuthor(name.trim()));
		}

		List<Long> categoryIds = request.categoryIds() == null ? List.of() : request.categoryIds().stream().distinct().toList();
		List<Category> chosen = categories.findAllById(categoryIds);
		if (chosen.size() != categoryIds.size()) {
			throw new FieldException("categoryIds", "One of the categories does not exist");
		}
		book.getCategories().clear();
		book.getCategories().addAll(chosen);

		syncProducts(book, request.products());
	}

	private Author findOrCreateAuthor(String name) {
		return authors.findFirstByNameIgnoreCase(name).orElseGet(() -> authors.save(new Author(name)));
	}

	/** One product per format. A format left out of the request is hidden, never deleted, because old orders point to it. */
	private void syncProducts(Book book, List<AdminProductRequest> requested) {
		Set<ProductFormat> seen = EnumSet.noneOf(ProductFormat.class);
		for (AdminProductRequest item : requested) {
			if (!seen.add(item.format())) {
				throw new FieldException("products", "A book can have only one product per format");
			}
		}

		Map<ProductFormat, Product> existing = book.getProducts().stream()
				.collect(Collectors.toMap(Product::getFormat, Function.identity()));
		for (int index = 0; index < requested.size(); index++) {
			AdminProductRequest item = requested.get(index);
			Product product = existing.get(item.format());
			boolean added = product == null;
			if (added) {
				product = new Product(book, "BK-" + book.getId() + "-" + item.format().name(), item.format());
			}
			String isbn = blankToNull(item.isbn());
			checkIsbnIsFree(isbn, product, "products[" + index + "].isbn");
			product.update(isbn, item.price(), item.stockQuantity(), item.active());
			if (added) {
				book.getProducts().add(products.save(product));
			}
		}
		existing.values().stream().filter(p -> !seen.contains(p.getFormat())).forEach(Product::deactivate);
	}

	private void checkIsbnIsFree(String isbn, Product product, String field) {
		if (isbn == null) {
			return;
		}
		products.findByIsbn(isbn).filter(other -> !other.getId().equals(product.getId())).ifPresent(other -> {
			throw new FieldException(field, "Another product already uses this ISBN");
		});
	}

	private String uniqueSlug(String title) {
		String base = Normalizer.normalize(title, Normalizer.Form.NFD)
				.replaceAll("\\p{M}", "")
				.toLowerCase(Locale.ROOT)
				.replaceAll("[^a-z0-9]+", "-")
				.replaceAll("^-+|-+$", "");
		if (base.isEmpty()) {
			base = "book";
		}
		String slug = base;
		for (int n = 2; books.existsBySlug(slug); n++) {
			slug = base + "-" + n;
		}
		return slug;
	}

	private static AdminBookRowDto toRow(Book book) {
		List<ProductFormat> formats = book.getProducts().stream()
				.filter(Product::isActive)
				.map(Product::getFormat)
				.sorted()
				.toList();
		return new AdminBookRowDto(book.getId(), book.getTitle(), book.getSlug(), book.getCoverImagePath(),
				book.getAuthors().stream().map(Author::getName).toList(), formats, book.isFeatured(), book.isActive());
	}

	private AdminBookDto toDto(Book book) {
		List<Product> sorted = new ArrayList<>(book.getProducts());
		sorted.sort(Comparator.comparing(Product::getFormat));
		Map<Long, EbookFile> files = ebookFiles
				.findByProductIdIn(sorted.stream().map(Product::getId).toList()).stream()
				.collect(Collectors.toMap(f -> f.getProduct().getId(), Function.identity()));
		List<AdminProductDto> productDtos = sorted.stream()
				.map(p -> new AdminProductDto(p.getId(), p.getSku(), p.getFormat(), p.getIsbn(), p.getPrice(),
						p.getStockQuantity(), p.isActive(), toDto(files.get(p.getId()))))
				.toList();
		List<BookImageDto> images = book.getImages().stream().map(i -> new BookImageDto(i.getId(), i.getPath())).toList();
		return new AdminBookDto(book.getId(), book.getTitle(), book.getSlug(), book.getDescription(),
				book.getPublisher(), book.getPublishedOn(), book.getLanguageCode(), book.getPageCount(),
				book.isFeatured(), book.isActive(), book.getCoverImagePath(), images,
				book.getAuthors().stream().map(Author::getName).toList(),
				book.getCategories().stream().map(Category::getId).toList(), productDtos);
	}

	private static EbookFileDto toDto(EbookFile file) {
		return file == null ? null : new EbookFileDto(file.getOriginalFilename(), file.getFileSizeBytes(), file.getScanStatus());
	}

	private static String blankToNull(String text) {
		return text == null || text.isBlank() ? null : text.trim();
	}

}
