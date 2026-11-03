package com.bookora.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bookora.service.CoverStorage;

/** Serves the cover images that administrators uploaded. */
@RestController
@RequestMapping("/api/covers")
public class CoverController {

	private final CoverStorage covers;

	public CoverController(CoverStorage covers) {
		this.covers = covers;
	}

	@GetMapping("/{name}")
	public ResponseEntity<Resource> cover(@PathVariable String name) throws IOException {
		Path file = covers.find(name);
		return ResponseEntity.ok()
				.contentType(MediaType.IMAGE_JPEG)
				.contentLength(Files.size(file))
				.cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic().immutable())
				.body(new FileSystemResource(file));
	}

}
