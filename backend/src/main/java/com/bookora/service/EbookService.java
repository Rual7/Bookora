package com.bookora.service;

import java.nio.file.Path;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.bookora.dto.EbookFileDto;
import com.bookora.dto.LibraryItemDto;

public interface EbookService {

	/** Stores the e-book of a product, replacing the previous file, and sets its scan status. */
	EbookFileDto upload(Long bookId, Long productId, MultipartFile file);

	/** The e-books the customer has paid for, newest first. */
	List<LibraryItemDto> library(String email);

	/** Checks that the signed-in customer paid for the order line, records the download and returns the file to send. */
	Download prepareDownload(String email, Long orderItemId);

	record Download(Path path, String filename, String mimeType) {
	}

}
