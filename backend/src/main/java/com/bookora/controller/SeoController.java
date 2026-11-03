package com.bookora.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bookora.repository.BookRepository;

/** The two files search engines ask for first. They are built here because the book pages change with the catalog. */
@RestController
public class SeoController {

	private static final List<String> PAGES = List.of("/", "/books", "/categories", "/about");
	private static final List<String> PRIVATE_PAGES = List.of("/admin", "/cart", "/checkout", "/orders", "/ebooks",
			"/addresses", "/login", "/register");

	private final BookRepository books;
	private final String baseUrl;

	public SeoController(BookRepository books, @Value("${bookora.public-base-url}") String baseUrl) {
		this.books = books;
		this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
	}

	@GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
	public String sitemap() {
		StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
				.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
		PAGES.forEach(path -> addUrl(xml, path));
		books.findActiveSlugs().forEach(slug -> addUrl(xml, "/books/" + slug));
		return xml.append("</urlset>\n").toString();
	}

	@GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
	public String robots() {
		StringBuilder text = new StringBuilder("User-agent: *\nAllow: /\nDisallow: /api/\n");
		PRIVATE_PAGES.forEach(path -> text.append("Disallow: ").append(path).append('\n'));
		return text.append("\nSitemap: ").append(baseUrl).append("/sitemap.xml\n").toString();
	}

	private void addUrl(StringBuilder xml, String path) {
		xml.append("  <url><loc>").append(baseUrl).append(path).append("</loc></url>\n");
	}

}
