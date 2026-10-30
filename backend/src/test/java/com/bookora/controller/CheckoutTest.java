package com.bookora.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class CheckoutTest {

	private static final String ADDRESS = """
			{"recipientName":"Ana Pop","phone":"0700000000","country":"Romania","city":"Iasi",
			"postalCode":"700000","streetAddress":"Strada Lunga 1","defaultAddress":false}
			""";

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ProductRepository products;

	private MockHttpSession session;
	private Product paperback;
	private Product ebook;

	@BeforeEach
	void signIn() throws Exception {
		session = register("order-" + UUID.randomUUID() + "@example.com");
		paperback = products.findAll().stream().filter(p -> p.getFormat() == ProductFormat.PAPERBACK).findFirst()
				.orElseThrow();
		ebook = products.findAll().stream().filter(p -> p.getFormat() == ProductFormat.EPUB).findFirst().orElseThrow();
	}

	private MockHttpSession register(String email) throws Exception {
		String body = """
				{"email":"%s","password":"secret-pass","firstName":"Ana","lastName":"Pop"}
				""".formatted(email);
		mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body));
		MockHttpSession newSession = new MockHttpSession();
		mvc.perform(post("/api/auth/login").with(csrf()).session(newSession).contentType(MediaType.APPLICATION_JSON)
				.content(body));
		return newSession;
	}

	private void addToCart(MockHttpSession who, Product product, int quantity) throws Exception {
		mvc.perform(post("/api/cart/items").with(csrf()).session(who).contentType(MediaType.APPLICATION_JSON)
				.content("{\"productId\":%d,\"quantity\":%d}".formatted(product.getId(), quantity)))
				.andExpect(status().isOk());
	}

	private String saveAddress(MockHttpSession who) throws Exception {
		String response = mvc.perform(post("/api/addresses").with(csrf()).session(who)
				.contentType(MediaType.APPLICATION_JSON).content(ADDRESS))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return response.replaceAll(".*\"id\":(\\d+).*", "$1");
	}

	@Test
	void ordersAndAddressesRequireLogin() throws Exception {
		mvc.perform(get("/api/orders")).andExpect(status().isUnauthorized());
		mvc.perform(get("/api/addresses")).andExpect(status().isUnauthorized());
		mvc.perform(post("/api/checkout").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void firstAddressBecomesTheDefaultAndDeleteHandsItOn() throws Exception {
		String first = saveAddress(session);
		String second = saveAddress(session);

		mvc.perform(get("/api/addresses").session(session))
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].defaultAddress").value(true));

		mvc.perform(delete("/api/addresses/" + first).with(csrf()).session(session))
				.andExpect(status().isNoContent());
		mvc.perform(get("/api/addresses").session(session))
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].id").value(Long.parseLong(second)))
				.andExpect(jsonPath("$[0].defaultAddress").value(true));
	}

	@Test
	void invalidAddressReturnsFieldErrors() throws Exception {
		mvc.perform(post("/api/addresses").with(csrf()).session(session).contentType(MediaType.APPLICATION_JSON)
				.content("{\"recipientName\":\"\",\"defaultAddress\":false}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.recipientName").exists())
				.andExpect(jsonPath("$.errors.city").exists());
	}

	@Test
	void checkoutCreatesTheOrderAndEmptiesTheCart() throws Exception {
		int stockBefore = paperback.getStockQuantity();
		addToCart(session, paperback, 2);
		String addressId = saveAddress(session);

		mvc.perform(post("/api/checkout").with(csrf()).session(session).contentType(MediaType.APPLICATION_JSON)
				.content("{\"addressId\":" + addressId + "}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
				.andExpect(jsonPath("$.itemCount").value(2))
				.andExpect(jsonPath("$.shippingCost").value(2.5))
				.andExpect(jsonPath("$.total").value(paperback.getPrice().doubleValue() * 2 + 2.5))
				.andExpect(jsonPath("$.currency").value("EUR"))
				.andExpect(jsonPath("$.shippingAddress.city").value("Iasi"));

		org.assertj.core.api.Assertions.assertThat(paperback.getStockQuantity()).isEqualTo(stockBefore - 2);
		mvc.perform(get("/api/cart").session(session)).andExpect(jsonPath("$.itemCount").value(0));
		mvc.perform(get("/api/orders").session(session))
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].orderNumber").value(org.hamcrest.Matchers.startsWith("BK-")));
	}

	@Test
	void ebookOnlyCheckoutNeedsNoAddress() throws Exception {
		addToCart(session, ebook, 1);

		mvc.perform(post("/api/checkout").with(csrf()).session(session).contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.shippingAddress").doesNotExist());
	}

	@Test
	void printedBooksWithoutAddressAreRejected() throws Exception {
		addToCart(session, paperback, 1);

		mvc.perform(post("/api/checkout").with(csrf()).session(session).contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void emptyCartCannotBeOrdered() throws Exception {
		mvc.perform(post("/api/checkout").with(csrf()).session(session).contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void aCustomerCannotSeeAnotherCustomersOrder() throws Exception {
		addToCart(session, ebook, 1);
		String body = mvc.perform(post("/api/checkout").with(csrf()).session(session)
				.contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andReturn().getResponse().getContentAsString();
		String orderNumber = body.replaceAll(".*\"orderNumber\":\"([^\"]+)\".*", "$1");

		mvc.perform(get("/api/orders/" + orderNumber).session(session)).andExpect(status().isOk());

		MockHttpSession other = register("other-" + UUID.randomUUID() + "@example.com");
		mvc.perform(get("/api/orders/" + orderNumber).session(other)).andExpect(status().isNotFound());
	}

}
