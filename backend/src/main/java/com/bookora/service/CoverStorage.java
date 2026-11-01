package com.bookora.service;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.bookora.exception.BadRequestException;
import com.bookora.exception.ResourceNotFoundException;

/**
 * Keeps cover images on disk, outside the web root. Every upload is decoded and written again as a fresh JPEG,
 * so metadata, trailing bytes and anything else hidden in the original file are dropped.
 */
@Component
public class CoverStorage {

	public static final String URL_PREFIX = "/api/covers/";

	private static final Pattern STORED_NAME = Pattern.compile("[0-9a-f]{32}\\.jpg");
	private static final int MAX_WIDTH = 800;
	private static final int MAX_HEIGHT = 1200;
	private static final int MAX_SOURCE_SIDE = 8000;

	private final Path directory;

	public CoverStorage(@Value("${bookora.storage.covers}") String directory) {
		this.directory = Path.of(directory).toAbsolutePath().normalize();
	}

	/** Stores the uploaded image and returns the public path that serves it. */
	public String store(MultipartFile upload) {
		byte[] bytes = readBytes(upload);
		if (!FileChecks.hasAllowedExtension(upload.getOriginalFilename(), "jpg", "jpeg", "png")
				|| !(FileChecks.looksLikeJpeg(bytes) || FileChecks.looksLikePng(bytes))) {
			throw new BadRequestException("The cover must be a JPG or PNG image");
		}
		byte[] jpeg = toJpeg(decode(bytes));

		String name = UUID.randomUUID().toString().replace("-", "") + ".jpg";
		try {
			Files.createDirectories(directory);
			Files.write(directory.resolve(name), jpeg);
		} catch (IOException e) {
			throw new UncheckedIOException("Could not save the cover", e);
		}
		return URL_PREFIX + name;
	}

	public Path find(String name) {
		if (!STORED_NAME.matcher(name).matches() || !Files.isRegularFile(directory.resolve(name))) {
			throw new ResourceNotFoundException("Cover not found");
		}
		return directory.resolve(name);
	}

	/** Removes a file this class stored; other paths (the bundled demo covers) are left alone. */
	public void delete(String coverPath) {
		if (coverPath == null || !coverPath.startsWith(URL_PREFIX)) {
			return;
		}
		String name = coverPath.substring(URL_PREFIX.length());
		if (!STORED_NAME.matcher(name).matches()) {
			return;
		}
		try {
			Files.deleteIfExists(directory.resolve(name));
		} catch (IOException e) {
			throw new UncheckedIOException("Could not remove the cover", e);
		}
	}

	private static byte[] readBytes(MultipartFile upload) {
		try {
			return upload.getBytes();
		} catch (IOException e) {
			throw new BadRequestException("The file could not be read");
		}
	}

	private static BufferedImage decode(byte[] bytes) {
		try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
			Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
			if (!readers.hasNext()) {
				throw new BadRequestException("The cover must be a JPG or PNG image");
			}
			ImageReader reader = readers.next();
			try {
				reader.setInput(input);
				if (reader.getWidth(0) > MAX_SOURCE_SIDE || reader.getHeight(0) > MAX_SOURCE_SIDE) {
					throw new BadRequestException("The image is too large; use one under 8000 pixels on each side");
				}
				return reader.read(0);
			} finally {
				reader.dispose();
			}
		} catch (IOException e) {
			throw new BadRequestException("The cover must be a JPG or PNG image");
		}
	}

	private static byte[] toJpeg(BufferedImage source) {
		double scale = Math.min(1.0, Math.min((double) MAX_WIDTH / source.getWidth(), (double) MAX_HEIGHT / source.getHeight()));
		int width = Math.max(1, (int) Math.round(source.getWidth() * scale));
		int height = Math.max(1, (int) Math.round(source.getHeight() * scale));

		BufferedImage flat = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = flat.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, width, height);
		graphics.drawImage(source, 0, 0, width, height, null);
		graphics.dispose();

		ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
		ImageWriteParam params = writer.getDefaultWriteParam();
		params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		params.setCompressionQuality(0.85f);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (ImageOutputStream output = ImageIO.createImageOutputStream(out)) {
			writer.setOutput(output);
			writer.write(null, new IIOImage(flat, null, null), params);
		} catch (IOException e) {
			throw new UncheckedIOException("Could not encode the cover", e);
		} finally {
			writer.dispose();
		}
		return out.toByteArray();
	}

}
