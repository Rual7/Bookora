package com.bookora.controller;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.ResultActions;

import com.bookora.exception.ServiceUnavailableException;
import com.bookora.service.TurnstileCaptchaService;
import com.sun.net.httpserver.HttpServer;

/** Runs the real verification code against a small local server that plays the part of Cloudflare. */
class CaptchaTest extends ApiTestBase {

	private static final HttpServer CLOUDFLARE = startFakeCloudflare();

	@DynamicPropertySource
	static void turnstile(DynamicPropertyRegistry registry) {
		registry.add("bookora.turnstile.secret", () -> "test-secret");
		registry.add("bookora.turnstile.verify-url", () -> "http://localhost:" + CLOUDFLARE.getAddress().getPort() + "/siteverify");
	}

	/** Accepts the token "good" and refuses everything else. */
	private static HttpServer startFakeCloudflare() {
		try {
			HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
			server.createContext("/siteverify", exchange -> {
				String form = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
				boolean good = form.contains("secret=test-secret") && form.contains("response=good");
				byte[] answer = ("{\"success\":" + good + "}").getBytes(StandardCharsets.UTF_8);
				exchange.getResponseHeaders().add("Content-Type", "application/json");
				exchange.sendResponseHeaders(200, answer.length);
				exchange.getResponseBody().write(answer);
				exchange.close();
			});
			server.start();
			return server;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private ResultActions register(String token) throws Exception {
		return register(token, new MockHttpSession());
	}

	private ResultActions register(String token, MockHttpSession session) throws Exception {
		var request = post("/api/auth/register").with(csrf()).session(session).contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email":"%s@example.com","password":"secret-pass","firstName":"Ana","lastName":"Pop"}
						""".formatted(UUID.randomUUID()));
		if (token != null) {
			request.header("X-Captcha-Token", token);
		}
		return mvc.perform(request);
	}

	@Test
	void registrationNeedsAToken() throws Exception {
		register(null).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value(containsString("verification")));
	}

	@Test
	void aRefusedTokenStopsRegistration() throws Exception {
		register("bad").andExpect(status().isBadRequest());
	}

	@Test
	void aGoodTokenLetsRegistrationThroughAndSignsTheCustomerIn() throws Exception {
		MockHttpSession session = new MockHttpSession();
		register("good", session).andExpect(status().isCreated());

		mvc.perform(get("/api/auth/me").session(session)).andExpect(status().isOk());
	}

	@Test
	void signInChecksTheTokenBeforeThePassword() throws Exception {
		String body = "{\"email\":\"nobody@example.com\",\"password\":\"whatever-pass\"}";

		mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isBadRequest());
		mvc.perform(post("/api/auth/login").with(csrf()).header("X-Captcha-Token", "good")
				.contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void theContactFormNeedsAToken() throws Exception {
		String body = "{\"name\":\"Ana\",\"email\":\"ana@example.com\",\"message\":\"Hello\"}";

		mvc.perform(post("/api/contact").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isBadRequest());
		mvc.perform(post("/api/contact").with(csrf()).header("X-Captcha-Token", "good")
				.contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isCreated());
	}

	@Test
	void anUnreachableCloudflareIsReportedAsUnavailable() {
		var service = new TurnstileCaptchaService("test-secret", "http://localhost:1/siteverify");

		assertThrows(ServiceUnavailableException.class, () -> service.verify("good", "127.0.0.1"));
	}

}
