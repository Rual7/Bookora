package com.bookora.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.bookora.model.Product;
import com.bookora.model.ProductFormat;
import com.bookora.repository.ProductRepository;

/** Runs against the seeded database; every test is rolled back. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CartAndWishlistTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ProductRepository products;

	private MockHttpSession session;
	private Product paperback;
	private Product ebook;

	@BeforeEach
	void signIn() throws Exception {
		String email = "cart-" + UUID.randomUUID() + "@example.com";
		String body = """
				{"email":"%s","password":"secret-pass","firstName":"Ana","lastName":"Pop"}
				""".formatted(email);
		mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body));
		session = new MockHttpSession();
		mvc.perform(post("/api/auth/login").with(csrf()).session(session).contentType(MediaType.APPLICATION_JSON)
				.content(body));

		paperback = products.findAll().stream().filter(p -> p.getFormat() == ProductFormat.PAPERBACK).findFirst()
				.orElseThrow();
		ebook = products.findAll().stream().filter(p -> p.getFormat() == ProductFormat.EPUB).findFirst()
				.orElseThrow();
	}

	private String addBody(Long productId, int quantity) {
		return "{\"productId\":%d,\"quantity\":%d}".formatted(productId, quantity);
	}

	@Test
	void cartRequiresLogin() throws Exception {
		mvc.perform(get("/api/cart")).andExpect(status().isUnauthorized());
	}

	@Test
	void addingTheSameBookTwiceRaisesTheQuantityAndTotalsAreComputedOnTheServer() throws Exception {
		mvc.perform(post("/api/cart/items").with(csrf()).session(session).contentType(MediaType.APPLICATION_JSON)
				.content(addBody(paperback.getId(), 1))).andExpect(status().isOk());

		mvc.perform(post("/api/cart/items").with(csrf()).session(session).contentType(MediaType.APPLICATION_JSON)
				.content(addBody(paperback.getId(), 2)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items.length()").value(1))
				.andExpect(jsonPath("$.items[0].quantity").value(3))
				.andExpect(jsonPath("$.itemCount").value(3))
				.andExpect(jsonPath("$.subtotal").value(paperback.getPrice().multiply(java.math.BigDecimal.valueOf(3))
						.doubleValue()));
	}

	@Test
	void anEbookCanOnlyBeInTheCartOnce() throws Exception {
		mvc.perform(post("/api/cart/items").with(csrf()).session(session).contentType(MediaType.APPLICATION_JSON)
				.content(addBody(ebook.getId(), 1)));

		mvc.perform(post("/api/cart/items").with(csrf()).session(session).contentType(MediaType.APPLICATION_JSON)
				.content(addBody(ebook.getId(), 1)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[0].quantity").value(1));
	}

	@Test
	void quantityAboveTheStockIsRejected() throws Exception {
		mvc.perform(post("/api/cart/items").with(csrf()).session(session).contentType(MediaType.APPLICATION_JSON)
				.content(addBody(paperback.getId(), 1)));

		mvc.perform(put("/api/cart/items/" + paperback.getId()).with(csrf()).session(session)
				.contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":11}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void unknownProductIsNotFound() throws Exception {
		mvc.perform(post("/api/cart/items").with(csrf()).session(session).contentType(MediaType.APPLICATION_JSON)
				.content(addBody(999999L, 1)))
				.andExpect(status().isNotFound());
	}

	@Test
	void removingTheLastItemEmptiesTheCart() throws Exception {
		mvc.perform(post("/api/cart/items").with(csrf()).session(session).contentType(MediaType.APPLICATION_JSON)
				.content(addBody(paperback.getId(), 1)));

		mvc.perform(delete("/api/cart/items/" + paperback.getId()).with(csrf()).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items.length()").value(0))
				.andExpect(jsonPath("$.itemCount").value(0));
	}

	@Test
	void wishlistAddListRemove() throws Exception {
		Long bookId = paperback.getBook().getId();

		mvc.perform(put("/api/wishlist/" + bookId).with(csrf()).session(session)).andExpect(status().isNoContent());
		mvc.perform(put("/api/wishlist/" + bookId).with(csrf()).session(session)).andExpect(status().isNoContent());

		mvc.perform(get("/api/wishlist").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].id").value(bookId));

		mvc.perform(delete("/api/wishlist/" + bookId).with(csrf()).session(session))
				.andExpect(status().isNoContent());
		mvc.perform(get("/api/wishlist").session(session)).andExpect(jsonPath("$.length()").value(0));
	}

}
