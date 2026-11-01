package com.bookora.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.bookora.dto.AdminBookDto;
import com.bookora.dto.AdminBookRequest;
import com.bookora.dto.AdminBookRowDto;
import com.bookora.dto.PageDto;

public interface AdminBookService {

	PageDto<AdminBookRowDto> list(String query, int page, int size);

	AdminBookDto get(Long id);

	AdminBookDto create(AdminBookRequest request);

	AdminBookDto update(Long id, AdminBookRequest request);

	/** Adds pictures after the ones the book already has; the first picture of a book is its cover. */
	AdminBookDto addImages(Long id, List<MultipartFile> images);

	AdminBookDto removeImage(Long id, Long imageId);

	/** Makes the picture the cover of the book. */
	AdminBookDto makeMainImage(Long id, Long imageId);

}
