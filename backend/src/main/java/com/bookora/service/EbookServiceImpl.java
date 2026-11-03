package com.bookora.service;

import java.io.IOException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.bookora.dto.EbookFileDto;
import com.bookora.dto.LibraryItemDto;
import com.bookora.exception.BadRequestException;
import com.bookora.exception.ConflictException;
import com.bookora.exception.ResourceNotFoundException;
import com.bookora.model.AppUser;
import com.bookora.model.EbookDownload;
import com.bookora.model.EbookFile;
import com.bookora.model.OrderItem;
import com.bookora.model.Product;
import com.bookora.model.ProductFormat;
import com.bookora.model.ScanStatus;
import com.bookora.repository.EbookDownloadRepository;
import com.bookora.repository.EbookFileRepository;
import com.bookora.repository.OrderRepository;
import com.bookora.repository.ProductRepository;
import com.bookora.repository.UserRepository;

@Service
public class EbookServiceImpl implements EbookService {

	private static final Logger log = LoggerFactory.getLogger(EbookServiceImpl.class);

	private static final long MAX_BYTES = 12L * 1024 * 1024;

	private final ProductRepository products;
	private final EbookFileRepository ebookFiles;
	private final EbookDownloadRepository downloads;
	private final OrderRepository orders;
	private final UserRepository users;
	private final EbookStorage storage;
	private final EbookScanner scanner;

	public EbookServiceImpl(ProductRepository products, EbookFileRepository ebookFiles,
			EbookDownloadRepository downloads, OrderRepository orders, UserRepository users, EbookStorage storage,
			EbookScanner scanner) {
		this.products = products;
		this.ebookFiles = ebookFiles;
		this.downloads = downloads;
		this.orders = orders;
		this.users = users;
		this.storage = storage;
		this.scanner = scanner;
	}

	@Override
	@Transactional
	public EbookFileDto upload(Long bookId, Long productId, MultipartFile file) {
		Product product = products.findById(productId)
				.filter(p -> p.getBook().getId().equals(bookId))
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));
		return store(product, file.getOriginalFilename(), readBytes(file));
	}

	@Override
	@Transactional
	public EbookFileDto attach(Long productId, String filename, byte[] content) {
		Product product = products.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product not found"));
		return store(product, filename, content);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean hasStoredFile(Long productId) {
		return ebookFiles.findByProductId(productId).map(file -> storage.exists(file.getStorageKey())).orElse(false);
	}

	private EbookFileDto store(Product product, String filename, byte[] content) {
		if (!product.getFormat().isDigital()) {
			throw new BadRequestException("Only an e-book product can have a file");
		}
		checkFile(product.getFormat(), filename, content);

		ScanStatus status = scanner.scan(product.getFormat(), content);
		String key = storage.save(content);
		String name = displayName(filename);
		String mimeType = mimeTypeOf(product.getFormat());
		String hash = sha256(content);

		EbookFile existing = ebookFiles.findByProductId(product.getId()).orElse(null);
		if (existing == null) {
			existing = ebookFiles.save(new EbookFile(product, key, name, mimeType, content.length, hash, status));
		} else {
			String oldKey = existing.getStorageKey();
			existing.replaceWith(key, name, mimeType, content.length, hash, status);
			storage.delete(oldKey);
		}
		log.info("E-book stored: productId={}, size={}, scan={}", product.getId(), content.length, status);
		return new EbookFileDto(existing.getOriginalFilename(), existing.getFileSizeBytes(), existing.getScanStatus());
	}

	@Override
	@Transactional(readOnly = true)
	public List<LibraryItemDto> library(String email) {
		AppUser user = users.requireByEmail(email);
		List<OrderItem> items = orders.findPaidEbookItems(user.getId());
		Set<Long> cleanProducts = ebookFiles
				.findByProductIdIn(items.stream().map(i -> i.getProduct().getId()).toList()).stream()
				.filter(f -> f.getScanStatus() == ScanStatus.CLEAN)
				.map(f -> f.getProduct().getId())
				.collect(Collectors.toSet());
		return items.stream()
				.map(i -> new LibraryItemDto(i.getId(), i.getProductTitle(), i.getProduct().getBook().getSlug(),
						i.getProduct().getBook().getCoverImagePath(),
						i.getProduct().getBook().getAuthorNames(),
						i.getProductFormat(), i.getOrder().getPaidAt(),
						cleanProducts.contains(i.getProduct().getId())))
				.toList();
	}

	@Override
	@Transactional
	public Download prepareDownload(String email, Long orderItemId) {
		AppUser user = users.requireByEmail(email);
		// An item of someone else's order is simply "not found".
		OrderItem item = orders.findItemOfUser(orderItemId, user.getId())
				.filter(i -> i.getProductFormat().isDigital())
				.orElseThrow(() -> new ResourceNotFoundException("Download not found"));
		if (!item.getOrder().getStatus().isPaid()) {
			throw new ConflictException("This order has not been paid");
		}
		EbookFile file = ebookFiles.findByProductId(item.getProduct().getId())
				.filter(f -> f.getScanStatus() == ScanStatus.CLEAN)
				.orElseThrow(() -> new ConflictException("This e-book is not available for download yet"));

		downloads.save(new EbookDownload(item));
		log.info("E-book downloaded: userId={}, orderItemId={}", user.getId(), orderItemId);

		Path path = storage.find(file.getStorageKey());
		return new Download(path, item.getProduct().getBook().getSlug() + extensionOf(item.getProductFormat()),
				file.getMimeType());
	}

	private static byte[] readBytes(MultipartFile file) {
		if (file.isEmpty()) {
			throw new BadRequestException("Choose a file to upload");
		}
		if (file.getSize() > MAX_BYTES) {
			throw new BadRequestException("The file is larger than 12 MB");
		}
		try {
			return file.getBytes();
		} catch (IOException _) {
			throw new BadRequestException("The file could not be read");
		}
	}

	private static void checkFile(ProductFormat format, String filename, byte[] content) {
		boolean pdf = format == ProductFormat.PDF;
		String extension = pdf ? "pdf" : "epub";
		boolean realType = pdf ? FileChecks.looksLikePdf(content) : FileChecks.looksLikeEpub(content);
		if (!FileChecks.hasAllowedExtension(filename, extension) || !realType) {
			throw new BadRequestException("This product needs a valid ." + extension + " file");
		}
	}

	/** Only the last part of the name, without control characters, is kept for the admin screen. */
	private static String displayName(String filename) {
		String name = filename == null ? "ebook" : filename.replace('\\', '/');
		name = name.substring(name.lastIndexOf('/') + 1).replaceAll("\\p{Cntrl}", "");
		return name.length() > 200 ? name.substring(name.length() - 200) : name;
	}

	private static String mimeTypeOf(ProductFormat format) {
		return format == ProductFormat.PDF ? "application/pdf" : "application/epub+zip";
	}

	private static String extensionOf(ProductFormat format) {
		return format == ProductFormat.PDF ? ".pdf" : ".epub";
	}

	private static String sha256(byte[] content) {
		try {
			return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

}
