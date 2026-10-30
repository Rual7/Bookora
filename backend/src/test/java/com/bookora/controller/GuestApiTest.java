package com.bookora.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.bookora.model.Product;
import com.bookora.model.ProductFormat;
import com.bookora.repository.ProductRepository;

/** Endpoints that visitors can use without signing in. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GuestApiTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ProductRepository products;

	private Product firstProduct(ProductFormat format) {
		return products.findAll().stream().filter(p -> p.getFormat() == format).findFirst().orElseThrow();
	}

	@Test
	void previewPricesAGuestCartOnTheServer() throws Exception {
		Product paperback = firstProduct(ProductFormat.PAPERBACK);
		String body = "{\"items\":[{\"productId\":%d,\"quantity\":2},{\"productId\":%d,\"quantity\":1},{\"productId\":999999,\"quantity\":1}]}"
				.formatted(paperback.getId(), paperback.getId());

		mvc.perform(post("/api/cart/preview").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items.length()").value(1))
				.andExpect(jsonPath("$.items[0].quantity").value(3))
				.andExpect(jsonPath("$.itemCount").value(3));
	}

	@Test
	void previewCapsAnEbookAtOneCopy() throws Exception {
		Product ebook = firstProduct(ProductFormat.EPUB);
		String body = "{\"items\":[{\"productId\":%d,\"quantity\":5}]}".formatted(ebook.getId());

		mvc.perform(post("/api/cart/preview").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(jsonPath("$.items[0].quantity").value(1));
	}

	@Test
	void booksCanBeLoadedByIdForAGuestWishlist() throws Exception {
		Long bookId = firstProduct(ProductFormat.PAPERBACK).getBook().getId();

		mvc.perform(get("/api/books/by-ids").param("ids", bookId + ",999999"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].id").value(bookId));
	}

	@Test
	void contactFormIsSavedAndValidated() throws Exception {
		mvc.perform(post("/api/contact").with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Ana\",\"email\":\"ana@example.com\",\"message\":\"Hello\"}"))
				.andExpect(status().isCreated());

		mvc.perform(post("/api/contact").with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"\",\"email\":\"nope\",\"message\":\"\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.email").exists());
	}

}
