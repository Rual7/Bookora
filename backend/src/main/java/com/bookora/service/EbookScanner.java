package com.bookora.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.stereotype.Component;

import com.bookora.model.ProductFormat;
import com.bookora.model.ScanStatus;

/**
 * A simple content check, not an antivirus. It rejects the things a book never needs: scripts and launch actions
 * inside a PDF, and programs inside an EPUB. A file that passes is CLEAN only in that sense.
 */
@Component
public class EbookScanner {

	private static final List<String> PDF_ACTIVE_MARKERS = List.of("/JavaScript", "/JS", "/Launch", "/EmbeddedFile");
	private static final List<String> PROGRAM_EXTENSIONS = List.of(".exe", ".dll", ".bat", ".cmd", ".scr", ".msi", ".sh");
	private static final int MAX_EPUB_ENTRIES = 5000;

	public ScanStatus scan(ProductFormat format, byte[] content) {
		boolean safe = format == ProductFormat.PDF ? pdfIsPlain(content) : epubIsPlain(content);
		return safe ? ScanStatus.CLEAN : ScanStatus.REJECTED;
	}

	private static boolean pdfIsPlain(byte[] content) {
		String text = new String(content, StandardCharsets.ISO_8859_1);
		return PDF_ACTIVE_MARKERS.stream().noneMatch(text::contains);
	}

	private static boolean epubIsPlain(byte[] content) {
		try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(content))) {
			int entries = 0;
			ZipEntry entry;
			while ((entry = zip.getNextEntry()) != null) {
				String name = entry.getName().toLowerCase(Locale.ROOT);
				if (++entries > MAX_EPUB_ENTRIES || name.contains("..") || PROGRAM_EXTENSIONS.stream().anyMatch(name::endsWith)) {
					return false;
				}
			}
			return true;
		} catch (IOException _) {
			return false;
		}
	}

}
