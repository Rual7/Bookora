package com.bookora.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/** Checks what an uploaded file really is. The name and the browser's content type are only hints; the first bytes decide. */
final class FileChecks {

	private FileChecks() {
	}

	static boolean hasAllowedExtension(String filename, String... allowed) {
		if (filename == null || !filename.contains(".")) {
			return false;
		}
		String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
		return Arrays.asList(allowed).contains(extension);
	}

	static boolean looksLikeJpeg(byte[] bytes) {
		return startsWith(bytes, 0xFF, 0xD8, 0xFF);
	}

	static boolean looksLikePng(byte[] bytes) {
		return startsWith(bytes, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A);
	}

	/** A PDF starts with "%PDF-" and a well-formed one has an end-of-file marker near the end. */
	static boolean looksLikePdf(byte[] bytes) {
		if (!startsWith(bytes, '%', 'P', 'D', 'F', '-')) {
			return false;
		}
		int from = Math.max(0, bytes.length - 1024);
		String tail = new String(bytes, from, bytes.length - from, StandardCharsets.ISO_8859_1);
		return tail.contains("%%EOF");
	}

	/** An EPUB is a ZIP whose first entry is a file called "mimetype" holding "application/epub+zip". */
	static boolean looksLikeEpub(byte[] bytes) {
		if (!startsWith(bytes, 'P', 'K', 0x03, 0x04)) {
			return false;
		}
		try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(bytes))) {
			ZipEntry first = zip.getNextEntry();
			if (first == null || !first.getName().equals("mimetype")) {
				return false;
			}
			String content = new String(zip.readNBytes(64), StandardCharsets.US_ASCII);
			return content.equals("application/epub+zip");
		} catch (IOException _) {
			return false;
		}
	}

	private static boolean startsWith(byte[] bytes, int... expected) {
		if (bytes.length < expected.length) {
			return false;
		}
		for (int i = 0; i < expected.length; i++) {
			if ((bytes[i] & 0xFF) != expected[i]) {
				return false;
			}
		}
		return true;
	}

}
