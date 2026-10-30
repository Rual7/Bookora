package com.bookora.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** Orders the seeded catalog in every way the website offers. Rolled back after each test. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BookSortingTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private JdbcTemplate jdbc;

	private final ObjectMapper json = new ObjectMapper();

	private JsonNode books(String sort) throws Exception {
		String body = mvc.perform(get("/api/books").param("sort", sort).param("size", "50"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		return json.readTree(body).get("content");
	}

	@Test
	void priceAscendingPutsTheCheapestBookFirst() throws Exception {
		List<BigDecimal> prices = new ArrayList<>();
		books("price-asc").forEach(book -> prices.add(book.get("price").decimalValue()));

		assertThat(prices).isSortedAccordingTo(BigDecimal::compareTo);
	}

	@Test
	void priceDescendingPutsTheMostExpensiveBookFirst() throws Exception {
		List<BigDecimal> prices = new ArrayList<>();
		books("price-desc").forEach(book -> prices.add(book.get("price").decimalValue()));

		assertThat(prices).isSortedAccordingTo((a, b) -> b.compareTo(a));
	}

	@Test
	void ratingPutsTheBestReviewedBookFirstAndUnreviewedBooksLast() throws Exception {
		jdbc.update("INSERT INTO app_user (email, password_hash, first_name, last_name) "
				+ "VALUES ('sort-test@example.com', 'x', 'Test', 'User')");
		rate("frankenstein", 5);
		rate("dracula", 3);

		JsonNode ordered = books("rating");

		assertThat(ordered.get(0).get("slug").asString()).isEqualTo("frankenstein");
		assertThat(ordered.get(1).get("slug").asString()).isEqualTo("dracula");
	}

	@Test
	void anUnknownSortFallsBackToTitleOrder() throws Exception {
		List<String> titles = new ArrayList<>();
		books("nonsense").forEach(book -> titles.add(book.get("title").asString()));

		assertThat(titles).isSortedAccordingTo(String.CASE_INSENSITIVE_ORDER);
	}

	private void rate(String bookSlug, int rating) {
		jdbc.update("INSERT INTO review (user_id, book_id, rating, status) "
				+ "SELECT u.id, b.id, ?, 'APPROVED' FROM app_user u, book b "
				+ "WHERE u.email = 'sort-test@example.com' AND b.slug = ?", rating, bookSlug);
	}

}
