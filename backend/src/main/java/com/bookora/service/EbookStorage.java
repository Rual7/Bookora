package com.bookora.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Keeps e-book files on disk under random names, outside the web root. Nothing here is served directly:
 * downloads go through the controller, which checks who bought the book.
 */
@Component
public class EbookStorage {

	private static final Pattern STORAGE_KEY = Pattern.compile("[0-9a-f]{32}");

	private final Path directory;

	public EbookStorage(@Value("${bookora.storage.ebooks}") String directory) {
		this.directory = Path.of(directory).toAbsolutePath().normalize();
	}

	/** Writes the file and returns the key it is stored under. */
	public String save(byte[] content) {
		String key = UUID.randomUUID().toString().replace("-", "");
		try {
			Files.createDirectories(directory);
			Files.write(directory.resolve(key), content);
		} catch (IOException e) {
			throw new UncheckedIOException("Could not save the e-book", e);
		}
		return key;
	}

	public Path find(String key) {
		if (!STORAGE_KEY.matcher(key).matches()) {
			throw new IllegalArgumentException("Invalid storage key");
		}
		return directory.resolve(key);
	}

	public void delete(String key) {
		try {
			Files.deleteIfExists(find(key));
		} catch (IOException e) {
			throw new UncheckedIOException("Could not remove the e-book", e);
		}
	}

}
