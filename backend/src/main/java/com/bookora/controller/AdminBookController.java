package com.bookora.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bookora.dto.AdminBookDto;
import com.bookora.dto.AdminBookRequest;
import com.bookora.dto.AdminBookRowDto;
import com.bookora.dto.EbookUploadDto;
import com.bookora.dto.PageDto;
import com.bookora.service.AdminAudit;
import com.bookora.service.AdminBookService;
import com.bookora.service.EbookService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/books")
public class AdminBookController {

	private final AdminBookService bookService;
	private final EbookService ebookService;
	private final AdminAudit audit;

	public AdminBookController(AdminBookService bookService, EbookService ebookService, AdminAudit audit) {
		this.bookService = bookService;
		this.ebookService = ebookService;
		this.audit = audit;
	}

	@GetMapping
	public PageDto<AdminBookRowDto> list(@RequestParam(required = false) String q,
			@RequestParam(defaultValue = "0") int page) {
		return bookService.list(q, AdminPaging.page(page), AdminPaging.PAGE_SIZE);
	}

	@GetMapping("/{id}")
	public AdminBookDto get(@PathVariable Long id) {
		return bookService.get(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public AdminBookDto create(@Valid @RequestBody AdminBookRequest request, Authentication admin) {
		AdminBookDto created = bookService.create(request);
		audit.record(admin, "book.create", created.id());
		return created;
	}

	@PutMapping("/{id}")
	public AdminBookDto update(@PathVariable Long id, @Valid @RequestBody AdminBookRequest request,
			Authentication admin) {
		AdminBookDto updated = bookService.update(id, request);
		audit.record(admin, "book.update", id);
		return updated;
	}

	@PostMapping("/{id}/images")
	public AdminBookDto uploadImages(@PathVariable Long id, @RequestParam("files") List<MultipartFile> files,
			Authentication admin) {
		AdminBookDto updated = bookService.addImages(id, files);
		audit.record(admin, "book.image.add", id);
		return updated;
	}

	@DeleteMapping("/{id}/images/{imageId}")
	public AdminBookDto removeImage(@PathVariable Long id, @PathVariable Long imageId, Authentication admin) {
		AdminBookDto updated = bookService.removeImage(id, imageId);
		audit.record(admin, "book.image.remove", id);
		return updated;
	}

	@PutMapping("/{id}/images/{imageId}/main")
	public AdminBookDto makeMainImage(@PathVariable Long id, @PathVariable Long imageId, Authentication admin) {
		AdminBookDto updated = bookService.makeMainImage(id, imageId);
		audit.record(admin, "book.image.main", id);
		return updated;
	}

	@PostMapping("/{id}/products/{productId}/ebook")
	public EbookUploadDto uploadEbook(@PathVariable Long id, @PathVariable Long productId,
			@RequestParam("file") MultipartFile file, Authentication admin) {
		EbookService.Upload upload = ebookService.upload(id, productId, file);
		audit.record(admin, upload.keptPrevious() ? "ebook.upload.refused" : "ebook.upload", productId);
		return new EbookUploadDto(bookService.get(id), upload.keptPrevious() ? upload.file() : null);
	}

}
