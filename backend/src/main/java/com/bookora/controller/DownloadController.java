package com.bookora.controller;

import java.util.List;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bookora.dto.LibraryItemDto;
import com.bookora.service.EbookService;
import com.bookora.service.EbookService.Download;

@RestController
public class DownloadController {

	private final EbookService ebookService;

	public DownloadController(EbookService ebookService) {
		this.ebookService = ebookService;
	}

	@GetMapping("/api/library")
	public List<LibraryItemDto> library(Authentication authentication) {
		return ebookService.library(authentication.getName());
	}

	/** {@code inline=true} lets the browser show a PDF in a tab instead of saving it; other types are always saved. */
	@GetMapping("/api/downloads/{orderItemId}")
	public ResponseEntity<Resource> download(@PathVariable Long orderItemId,
			@RequestParam(defaultValue = "false") boolean inline, Authentication authentication) {
		Download download = ebookService.prepareDownload(authentication.getName(), orderItemId);
		boolean showInBrowser = inline && download.mimeType().equals(MediaType.APPLICATION_PDF_VALUE);
		ContentDisposition disposition = (showInBrowser ? ContentDisposition.inline() : ContentDisposition.attachment())
				.filename(download.filename()).build();
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(download.mimeType()))
				.header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
				.header(HttpHeaders.CACHE_CONTROL, "private, no-store")
				.body(new FileSystemResource(download.path()));
	}

}
