package com.bookora.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.bookora.model.AppUser;
import com.bookora.repository.UserRepository;

/** Shared setup for tests that call the API on the seeded database; every test is rolled back and files go to a temp folder. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
abstract class ApiTestBase {

	private static final Path STORAGE = createStorage();

	@DynamicPropertySource
	static void storage(DynamicPropertyRegistry registry) {
		registry.add("bookora.storage.covers", () -> STORAGE.resolve("covers").toString());
		registry.add("bookora.storage.ebooks", () -> STORAGE.resolve("ebooks").toString());
	}

	private static Path createStorage() {
		try {
			return Files.createTempDirectory("bookora-test-");
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Autowired
	protected MockMvc mvc;

	@Autowired
	protected UserRepository users;

	@Autowired
	private PasswordEncoder passwordEncoder;

	protected MockHttpSession registerAndLogin(String firstName) throws Exception {
		String email = firstName.toLowerCase() + "-" + UUID.randomUUID() + "@example.com";
		String body = """
				{"email":"%s","password":"secret-pass","firstName":"%s","lastName":"Pop"}
				""".formatted(email, firstName);
		mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body));
		return login(email);
	}

	protected MockHttpSession adminSession() throws Exception {
		String email = "admin-" + UUID.randomUUID() + "@example.com";
		users.save(AppUser.admin(email, passwordEncoder.encode("secret-pass"), "Ada", "Admin"));
		return login(email);
	}

	protected MockHttpSession login(String email) throws Exception {
		MockHttpSession session = new MockHttpSession();
		mvc.perform(post("/api/auth/login").with(csrf()).session(session).contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"%s\",\"password\":\"secret-pass\"}".formatted(email)));
		return session;
	}

}
