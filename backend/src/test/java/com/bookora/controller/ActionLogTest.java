package com.bookora.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;

import com.bookora.model.Product;
import com.bookora.model.ProductFormat;
import com.bookora.repository.ProductRepository;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

/** The log file is the record of what users did, so the actions must be in it, by user id and without private data. */
class ActionLogTest extends ApiTestBase {

	private static final String PASSWORD = "secret-pass";

	@Autowired
	private ProductRepository products;

	private final ListAppender<ILoggingEvent> logged = new ListAppender<>();
	private final Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

	@BeforeEach
	void startCapturing() {
		logged.start();
		root.addAppender(logged);
	}

	@AfterEach
	void stopCapturing() {
		root.detachAppender(logged);
	}

	private List<String> lines() {
		return logged.list.stream().map(ILoggingEvent::getFormattedMessage).toList();
	}

	private void assertLogged(String text) {
		assertTrue(lines().stream().anyMatch(line -> line.contains(text)), "Expected a log line with '" + text + "' in " + lines());
	}

	@Test
	void registeringSigningInAndSigningOutAreLoggedByUserId() throws Exception {
		MockHttpSession session = registerAndLogin("Logger");

		mvc.perform(post("/api/auth/logout").with(csrf()).session(session));

		assertLogged("User registered: id=");
		assertLogged("User signed in: userId=");
		assertLogged("User signed out: userId=");
	}

	@Test
	void aFailedSignInIsLoggedWithTheClientButNotTheEmailOrPassword() throws Exception {
		String email = "nobody-" + UUID.randomUUID() + "@example.com";

		mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, PASSWORD)));

		assertLogged("Failed sign-in: client=");
		assertNoPrivateData(email);
	}

	@Test
	void cartAndWishlistChangesAreLogged() throws Exception {
		String email = "shopper-" + UUID.randomUUID() + "@example.com";
		mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"%s\",\"password\":\"%s\",\"firstName\":\"Ana\",\"lastName\":\"Pop\"}".formatted(email, PASSWORD)));
		MockHttpSession session = login(email);
		Product paperback = products.findAll().stream().filter(p -> p.getFormat() == ProductFormat.PAPERBACK).findFirst()
				.orElseThrow();

		mvc.perform(post("/api/cart/items").with(csrf()).session(session).contentType(MediaType.APPLICATION_JSON)
				.content("{\"productId\":%d,\"quantity\":1}".formatted(paperback.getId())));
		mvc.perform(put("/api/cart/items/" + paperback.getId()).with(csrf()).session(session)
				.contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":2}"));
		mvc.perform(delete("/api/cart/items/" + paperback.getId()).with(csrf()).session(session));
		mvc.perform(put("/api/wishlist/" + paperback.getBook().getId()).with(csrf()).session(session));
		mvc.perform(delete("/api/wishlist/" + paperback.getBook().getId()).with(csrf()).session(session));

		assertLogged("Cart item added: userId=");
		assertLogged("Cart quantity changed: userId=");
		assertLogged("Cart item removed: userId=");
		assertLogged("Wishlist book added: userId=");
		assertLogged("Wishlist book removed: userId=");
		assertNoPrivateData(email);
	}

	private void assertNoPrivateData(String email) {
		assertFalse(lines().stream().anyMatch(line -> line.contains(email) || line.contains(PASSWORD)),
				"A log line contains the email or the password: " + lines());
	}

}
