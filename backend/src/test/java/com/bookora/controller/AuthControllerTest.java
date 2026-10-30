package com.bookora.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** Runs against the real database; every test is rolled back. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

	@Autowired
	private MockMvc mvc;

	private String json(String email, String password) {
		return """
				{"email":"%s","password":"%s","firstName":"Ana","lastName":"Pop"}
				""".formatted(email, password);
	}

	@Test
	void registerThenLoginThenMe() throws Exception {
		String email = "test-" + UUID.randomUUID() + "@example.com";

		mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(json(email, "secret-pass")))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.email").value(email))
				.andExpect(jsonPath("$.role").value("CUSTOMER"));

		MockHttpSession session = new MockHttpSession();
		mvc.perform(post("/api/auth/login").with(csrf()).session(session).contentType(MediaType.APPLICATION_JSON)
				.content(json(email, "secret-pass")))
				.andExpect(status().isOk());

		mvc.perform(get("/api/auth/me").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value(email));
	}

	@Test
	void loginWithWrongPasswordIsUnauthorized() throws Exception {
		String email = "test-" + UUID.randomUUID() + "@example.com";
		mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(json(email, "secret-pass")));

		mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(json(email, "wrong-pass")))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void registerWithInvalidDataReturnsFieldErrors() throws Exception {
		mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(json("not-an-email", "short")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.email").exists())
				.andExpect(jsonPath("$.errors.password").exists());
	}

	@Test
	void postWithoutCsrfTokenIsForbidden() throws Exception {
		mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content(json("a@example.com", "secret-pass")))
				.andExpect(status().isForbidden());
	}

	@Test
	void csrfEndpointHandsOutTheTokenCookie() throws Exception {
		mvc.perform(get("/api/auth/csrf"))
				.andExpect(status().isNoContent())
				.andExpect(cookie().exists("XSRF-TOKEN"));
	}

	@Test
	void meWithoutLoginIsUnauthorized() throws Exception {
		mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
	}

}
