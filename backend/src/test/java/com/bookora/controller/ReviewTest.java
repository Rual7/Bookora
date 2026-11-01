package com.bookora.controller;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;

import com.bookora.model.Product;
import com.bookora.model.ProductFormat;
import com.bookora.repository.OrderRepository;
import com.bookora.repository.ProductRepository;
import com.jayway.jsonpath.JsonPath;

class ReviewTest extends ApiTestBase {

	private static final String REVIEW = """
			{"rating":5,"title":"Loved it","comment":"A wonderful read."}
			""";

	@Autowired
	private ProductRepository products;

	@Autowired
	private OrderRepository orders;

	private Product ebook() {
		return products.findAll().stream().filter(p -> p.getFormat() == ProductFormat.EPUB).findFirst().orElseThrow();
	}

	private void review(MockHttpSession who, String slug, String body, int expectedStatus) throws Exception {
		mvc.perform(post("/api/books/" + slug + "/reviews").with(csrf()).session(who)
				.contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().is(expectedStatus));
	}

	private long pendingReviewIdOf(MockHttpSession admin, String slug) throws Exception {
		String json = mvc.perform(get("/api/admin/reviews").session(admin)).andReturn().getResponse().getContentAsString();
		List<Integer> ids = JsonPath.read(json, "$.content[?(@.bookSlug=='" + slug + "')].id");
		return ids.get(0);
	}

	private void moderate(MockHttpSession admin, long id, String status) throws Exception {
		mvc.perform(put("/api/admin/reviews/" + id + "/status").with(csrf()).session(admin)
				.contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"" + status + "\"}"))
				.andExpect(status().isOk());
	}

	@Test
	void visitorsCannotReview() throws Exception {
		mvc.perform(post("/api/books/any/reviews").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(REVIEW))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void ratingAndCommentAreChecked() throws Exception {
		MockHttpSession reader = registerAndLogin("Ana");
		String slug = ebook().getBook().getSlug();

		review(reader, slug, "{\"rating\":6,\"comment\":\"Too high\"}", 400);
		review(reader, slug, "{\"rating\":0,\"comment\":\"Too low\"}", 400);
		review(reader, slug, "{\"rating\":4,\"comment\":\"  \"}", 400);
		review(reader, "no-such-book", REVIEW, 404);
	}

	@Test
	void aReviewWaitsForApprovalAndOnlyOneIsAllowedPerBook() throws Exception {
		MockHttpSession admin = adminSession();
		MockHttpSession reader = registerAndLogin("Ana");
		String slug = ebook().getBook().getSlug();

		review(reader, slug, REVIEW, 201);
		review(reader, slug, REVIEW, 409);

		mvc.perform(get("/api/books/" + slug)).andExpect(jsonPath("$.reviews.length()").value(0));
		moderate(admin, pendingReviewIdOf(admin, slug), "APPROVED");
		mvc.perform(get("/api/books/" + slug))
				.andExpect(jsonPath("$.reviews[0].comment").value("A wonderful read."))
				.andExpect(jsonPath("$.reviews[0].authorName").value("Ana"))
				.andExpect(jsonPath("$.reviews[0].verifiedPurchase").value(false))
				.andExpect(jsonPath("$.reviewCount").value(1))
				.andExpect(jsonPath("$.averageRating").value(5.0));
	}

	@Test
	void aRejectedReviewStaysHidden() throws Exception {
		MockHttpSession admin = adminSession();
		MockHttpSession reader = registerAndLogin("Bot");
		String slug = ebook().getBook().getSlug();

		review(reader, slug, REVIEW, 201);
		moderate(admin, pendingReviewIdOf(admin, slug), "REJECTED");

		mvc.perform(get("/api/books/" + slug)).andExpect(jsonPath("$.reviews.length()").value(0));
		mvc.perform(get("/api/admin/reviews").param("status", "REJECTED").session(admin))
				.andExpect(jsonPath("$.content[*].bookSlug", hasItem(slug)));
	}

	@Test
	void aBuyersReviewIsMarkedAsAVerifiedPurchase() throws Exception {
		MockHttpSession admin = adminSession();
		MockHttpSession buyer = registerAndLogin("Buyer");
		Product product = ebook();
		String slug = product.getBook().getSlug();

		mvc.perform(post("/api/cart/items").with(csrf()).session(buyer).contentType(MediaType.APPLICATION_JSON)
				.content("{\"productId\":%d,\"quantity\":1}".formatted(product.getId()))).andExpect(status().isOk());
		String order = mvc.perform(post("/api/checkout").with(csrf()).session(buyer)
				.contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andReturn().getResponse().getContentAsString();
		orders.findByOrderNumber(JsonPath.read(order, "$.orderNumber")).orElseThrow().markPaid();

		review(buyer, slug, REVIEW, 201);
		mvc.perform(get("/api/admin/reviews").session(admin)).andExpect(jsonPath("$.content[0].verifiedPurchase").value(true));
		moderate(admin, pendingReviewIdOf(admin, slug), "APPROVED");

		mvc.perform(get("/api/books/" + slug)).andExpect(jsonPath("$.reviews[0].verifiedPurchase").value(true));
	}

	@Test
	void customersCannotModerate() throws Exception {
		mvc.perform(put("/api/admin/reviews/1/status").with(csrf()).session(registerAndLogin("Eve"))
				.contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"APPROVED\"}"))
				.andExpect(status().isForbidden());
	}

}
