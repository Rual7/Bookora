package com.bookora.controller;

import static com.bookora.service.StripePaymentGatewayTest.SECRET;
import static com.bookora.service.StripePaymentGatewayTest.sign;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import com.bookora.model.CustomerOrder;
import com.bookora.model.OrderStatus;
import com.bookora.model.Payment;
import com.bookora.model.PaymentStatus;
import com.bookora.model.Product;
import com.bookora.model.ProductFormat;
import com.bookora.repository.OrderRepository;
import com.bookora.repository.PaymentRepository;
import com.bookora.repository.ProductRepository;
import com.bookora.service.PaymentService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/** The webhook endpoint with real signatures against the database. Stripe itself is never called. */
@SpringBootTest(properties = "bookora.stripe.webhook-secret=" + SECRET)
@AutoConfigureMockMvc
@Transactional
class StripeWebhookTest {

	@Autowired
	private MockMvc mvc;
	@Autowired
	private ProductRepository products;
	@Autowired
	private OrderRepository orders;
	@Autowired
	private PaymentRepository payments;
	@Autowired
	private PaymentService paymentService;
	@Autowired
	private JdbcTemplate jdbc;
	@PersistenceContext
	private EntityManager entityManager;

	private MockHttpSession session;
	private Product paperback;
	private Product ebook;

	@BeforeEach
	void signIn() throws Exception {
		String body = """
				{"email":"%s","password":"secret-pass","firstName":"Ana","lastName":"Pop"}
				""".formatted("pay-" + UUID.randomUUID() + "@example.com");
		mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body));
		session = new MockHttpSession();
		mvc.perform(post("/api/auth/login").with(csrf()).session(session).contentType(MediaType.APPLICATION_JSON)
				.content(body));
		paperback = products.findAll().stream().filter(p -> p.getFormat() == ProductFormat.PAPERBACK).findFirst()
				.orElseThrow();
		ebook = products.findAll().stream().filter(p -> p.getFormat() == ProductFormat.EPUB).findFirst().orElseThrow();
	}

	/** Puts a product in the cart, checks out and gives back the order together with an open payment session. */
	private CustomerOrder orderWithPayment(Product product, int quantity, String sessionId) throws Exception {
		mvc.perform(post("/api/cart/items").with(csrf()).session(session).contentType(MediaType.APPLICATION_JSON)
				.content("{\"productId\":%d,\"quantity\":%d}".formatted(product.getId(), quantity)));
		if (product.getFormat() != ProductFormat.EPUB) {
			mvc.perform(post("/api/addresses").with(csrf()).session(session).contentType(MediaType.APPLICATION_JSON)
					.content("""
							{"recipientName":"Ana Pop","phone":"070","country":"Romania","city":"Iasi",
							"postalCode":"700000","streetAddress":"Strada Lunga 1","defaultAddress":true}"""));
		}
		String response = mvc.perform(post("/api/checkout").with(csrf()).session(session)
				.contentType(MediaType.APPLICATION_JSON)
				.content(product.getFormat() == ProductFormat.EPUB ? "{}" : "{\"addressId\":" + firstAddressId() + "}"))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		String number = response.replaceAll(".*\"orderNumber\":\"([^\"]+)\".*", "$1");
		CustomerOrder order = orders.findAll().stream().filter(o -> o.getOrderNumber().equals(number)).findFirst()
				.orElseThrow();
		payments.save(new Payment(order, sessionId, Instant.now().plusSeconds(1800)));
		return order;
	}

	private String firstAddressId() throws Exception {
		String list = mvc.perform(MockMvcRequestBuilders
				.get("/api/addresses").session(session)).andReturn().getResponse().getContentAsString();
		return list.replaceAll(".*\"id\":(\\d+).*", "$1");
	}

	private void sendEvent(String id, String type, String sessionId, String signature) throws Exception {
		String payload = """
				{"id":"%s","type":"%s","data":{"object":{"id":"%s","payment_intent":"pi_1","payment_status":"paid"}}}
				""".formatted(id, type, sessionId);
		String header = signature != null ? signature : sign(payload, SECRET, Instant.now().getEpochSecond());
		mvc.perform(post("/api/webhooks/stripe").header("Stripe-Signature", header)
				.contentType(MediaType.APPLICATION_JSON).content(payload))
				.andExpect(status().isOk());
	}

	@Test
	void aWebhookWithoutASignatureIsRefused() throws Exception {
		mvc.perform(post("/api/webhooks/stripe").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void aWebhookWithAWrongSignatureIsRefusedAndChangesNothing() throws Exception {
		CustomerOrder order = orderWithPayment(ebook, 1, "cs_wrong_signature");
		String payload = "{\"id\":\"evt_x\",\"type\":\"checkout.session.completed\",\"data\":{\"object\":{\"id\":\"cs_wrong_signature\",\"payment_status\":\"paid\"}}}";

		mvc.perform(post("/api/webhooks/stripe").header("Stripe-Signature", sign(payload, "whsec_other", Instant.now().getEpochSecond()))
				.contentType(MediaType.APPLICATION_JSON).content(payload))
				.andExpect(status().isBadRequest());

		assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
	}

	@Test
	void aSignedCompletedEventMarksTheOrderPaidAndASecondDeliveryChangesNothing() throws Exception {
		CustomerOrder order = orderWithPayment(ebook, 1, "cs_paid");

		sendEvent("evt_paid_1", "checkout.session.completed", "cs_paid", null);
		assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
		assertThat(order.getPaidAt()).isNotNull();
		assertThat(payments.findByProviderSessionId("cs_paid").orElseThrow().getStatus())
				.isEqualTo(PaymentStatus.SUCCEEDED);

		// Stripe delivers the same event again: it is acknowledged and ignored.
		sendEvent("evt_paid_1", "checkout.session.completed", "cs_paid", null);
		assertThat(jdbc.queryForObject("select count(*) from processed_webhook_event where provider_event_id = 'evt_paid_1'",
				Integer.class)).isEqualTo(1);
	}

	@Test
	void anExpiredSessionCancelsTheOrderAndPutsTheBooksBackInStock() throws Exception {
		int stockBefore = paperback.getStockQuantity();
		CustomerOrder order = orderWithPayment(paperback, 2, "cs_expired");
		assertThat(paperback.getStockQuantity()).isEqualTo(stockBefore - 2);

		sendEvent("evt_expired_1", "checkout.session.expired", "cs_expired", null);

		assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
		assertThat(paperback.getStockQuantity()).isEqualTo(stockBefore);
	}

	@Test
	void staleUnpaidOrdersAreCancelledButOrdersWithAnOpenSessionAreNot() throws Exception {
		int stockBefore = paperback.getStockQuantity();
		CustomerOrder abandoned = orderWithPayment(paperback, 1, "cs_abandoned");
		String orderNumber = abandoned.getOrderNumber();
		Long userId = abandoned.getUser().getId();
		jdbc.update("update payment set expires_at = now() - interval '10 minutes' where provider_session_id = 'cs_abandoned'");
		jdbc.update("update customer_order set created_at = now() - interval '2 hours' where order_number = ?",
				abandoned.getOrderNumber());
		// The updates above went straight to the database, so the persistence context is emptied and reloads from it.
		entityManager.flush();
		entityManager.clear();

		assertThat(paymentService.cancelStaleOrders()).isGreaterThanOrEqualTo(1);

		CustomerOrder reloaded = orders.findByOrderNumberAndUserId(orderNumber, userId)
				.orElseThrow();
		assertThat(reloaded.getStatus()).isEqualTo(OrderStatus.CANCELLED);
		assertThat(products.findById(paperback.getId()).orElseThrow().getStockQuantity()).isEqualTo(stockBefore);
	}

	@Test
	void payingWhenStripeIsNotSetUpAnswers503() throws Exception {
		CustomerOrder order = orderWithPayment(ebook, 1, "cs_unused");

		mvc.perform(post("/api/orders/" + order.getOrderNumber() + "/payment").with(csrf()).session(session))
				.andExpect(status().isServiceUnavailable());
	}

	@Test
	void payingRequiresASignedInCustomer() throws Exception {
		mvc.perform(post("/api/orders/BK-2026-000001/payment").with(csrf())).andExpect(status().isUnauthorized());
	}

}
