package com.bookora.controller;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

import com.bookora.model.CustomerOrder;
import com.bookora.model.Product;
import com.bookora.model.ProductFormat;
import com.bookora.repository.OrderRepository;
import com.bookora.repository.ProductRepository;
import com.jayway.jsonpath.JsonPath;

class AdminManagementTest extends ApiTestBase {

	@Autowired
	private ProductRepository products;

	@Autowired
	private OrderRepository orders;

	private String placeEbookOrder(MockHttpSession customer) throws Exception {
		Product ebook = products.findAll().stream().filter(p -> p.getFormat() == ProductFormat.EPUB).findFirst().orElseThrow();
		mvc.perform(post("/api/cart/items").with(csrf()).session(customer).contentType(MediaType.APPLICATION_JSON)
				.content("{\"productId\":%d,\"quantity\":1}".formatted(ebook.getId()))).andExpect(status().isOk());
		String order = mvc.perform(post("/api/checkout").with(csrf()).session(customer)
				.contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andReturn().getResponse().getContentAsString();
		return JsonPath.read(order, "$.orderNumber");
	}

	private void setStatus(MockHttpSession admin, String orderNumber, String status, int expected) throws Exception {
		mvc.perform(put("/api/admin/orders/" + orderNumber + "/status").with(csrf()).session(admin)
				.contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"" + status + "\"}"))
				.andExpect(status().is(expected));
	}

	@Test
	void adminSeesAllOrdersWithTheCustomer() throws Exception {
		MockHttpSession admin = adminSession();
		String number = placeEbookOrder(registerAndLogin("Ana"));

		mvc.perform(get("/api/admin/orders").param("status", "PENDING_PAYMENT").session(admin))
				.andExpect(jsonPath("$.content[*].orderNumber", hasItem(number)));
		mvc.perform(get("/api/admin/orders/" + number).session(admin))
				.andExpect(jsonPath("$.customerName").value("Ana Pop"))
				.andExpect(jsonPath("$.order.items.length()").value(1));
		mvc.perform(get("/api/admin/orders/BK-0000-000000").session(admin)).andExpect(status().isNotFound());
	}

	@Test
	void anUnpaidOrderCannotBeShippedOrDelivered() throws Exception {
		MockHttpSession admin = adminSession();
		String number = placeEbookOrder(registerAndLogin("Ana"));

		setStatus(admin, number, "SHIPPED", 409);
		setStatus(admin, number, "DELIVERED", 409);
	}

	@Test
	void anEbookOnlyOrderGoesFromPaidStraightToDelivered() throws Exception {
		MockHttpSession admin = adminSession();
		String number = placeEbookOrder(registerAndLogin("Ana"));
		orders.findByOrderNumber(number).orElseThrow().markPaid();

		setStatus(admin, number, "SHIPPED", 409);
		setStatus(admin, number, "DELIVERED", 200);
		setStatus(admin, number, "SHIPPED", 409);
	}

	@Test
	void aPrintedOrderIsShippedThenDelivered() throws Exception {
		MockHttpSession admin = adminSession();
		MockHttpSession customer = registerAndLogin("Ana");
		Product paperback = products.findAll().stream().filter(p -> p.getFormat() == ProductFormat.PAPERBACK).findFirst().orElseThrow();
		mvc.perform(post("/api/cart/items").with(csrf()).session(customer).contentType(MediaType.APPLICATION_JSON)
				.content("{\"productId\":%d,\"quantity\":1}".formatted(paperback.getId()))).andExpect(status().isOk());
		String address = mvc.perform(post("/api/addresses").with(csrf()).session(customer)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"recipientName":"Ana Pop","phone":"0700000000","country":"Romania","city":"Iasi",
						"postalCode":"700000","streetAddress":"Strada Lunga 1","defaultAddress":true}
						"""))
				.andReturn().getResponse().getContentAsString();
		String order = mvc.perform(post("/api/checkout").with(csrf()).session(customer)
				.contentType(MediaType.APPLICATION_JSON).content("{\"addressId\":" + JsonPath.read(address, "$.id") + "}"))
				.andReturn().getResponse().getContentAsString();
		String number = JsonPath.read(order, "$.orderNumber");
		CustomerOrder stored = orders.findByOrderNumber(number).orElseThrow();
		stored.markPaid();

		setStatus(admin, number, "SHIPPED", 200);
		mvc.perform(get("/api/orders/" + number).session(customer)).andExpect(jsonPath("$.status").value("SHIPPED"));
		setStatus(admin, number, "DELIVERED", 200);
		setStatus(admin, number, "PAID", 409);
	}

	@Test
	void adminReadsAndHandlesContactMessages() throws Exception {
		MockHttpSession admin = adminSession();
		mvc.perform(post("/api/contact").with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Ion\",\"email\":\"ion@example.com\",\"message\":\"Where is my book?\"}"))
				.andExpect(status().isCreated());

		String list = mvc.perform(get("/api/admin/messages").session(admin))
				.andExpect(jsonPath("$.content[0].message").value("Where is my book?"))
				.andExpect(jsonPath("$.content[0].handled").value(false))
				.andReturn().getResponse().getContentAsString();
		int id = JsonPath.read(list, "$.content[0].id");

		mvc.perform(put("/api/admin/messages/" + id + "/handled").with(csrf()).session(admin)
				.contentType(MediaType.APPLICATION_JSON).content("{\"handled\":true}"))
				.andExpect(jsonPath("$.handled").value(true));
	}

	@Test
	void aDisabledCustomerCannotSignInButAnAdministratorCannotBeDisabled() throws Exception {
		MockHttpSession admin = adminSession();
		MockHttpSession customer = registerAndLogin("Ana");
		String me = mvc.perform(get("/api/auth/me").session(customer)).andReturn().getResponse().getContentAsString();
		int customerId = JsonPath.read(me, "$.id");
		String email = JsonPath.read(me, "$.email");

		mvc.perform(put("/api/admin/customers/" + customerId + "/enabled").with(csrf()).session(admin)
				.contentType(MediaType.APPLICATION_JSON).content("{\"enabled\":false}"))
				.andExpect(jsonPath("$.enabled").value(false));
		mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"%s\",\"password\":\"secret-pass\"}".formatted(email)))
				.andExpect(status().isUnauthorized());

		String adminMe = mvc.perform(get("/api/auth/me").session(admin)).andReturn().getResponse().getContentAsString();
		mvc.perform(put("/api/admin/customers/" + (int) JsonPath.read(adminMe, "$.id") + "/enabled").with(csrf())
				.session(admin).contentType(MediaType.APPLICATION_JSON).content("{\"enabled\":false}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void customerListShowsOrderCounts() throws Exception {
		MockHttpSession admin = adminSession();
		MockHttpSession customer = registerAndLogin("Ana");
		placeEbookOrder(customer);

		List<Integer> counts = JsonPath.read(
				mvc.perform(get("/api/admin/customers").session(admin)).andReturn().getResponse().getContentAsString(),
				"$.content[?(@.name=='Ana Pop')].orderCount");
		assertTrue(counts.contains(1));
	}

}
