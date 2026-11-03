package com.bookora.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

import org.junit.jupiter.api.Test;

class SecurityHeadersTest extends ApiTestBase {

	@Test
	void everyResponseCarriesTheSecurityHeaders() throws Exception {
		mvc.perform(get("/api/categories"))
				.andExpect(header().string("X-Content-Type-Options", "nosniff"))
				.andExpect(header().string("X-Frame-Options", "DENY"))
				.andExpect(header().string("Referrer-Policy", "strict-origin-when-cross-origin"))
				.andExpect(header().string("Permissions-Policy", containsString("camera=()")))
				.andExpect(header().string("Content-Security-Policy", containsString("frame-ancestors 'none'")));
	}

	@Test
	void errorResponsesCarryThemToo() throws Exception {
		mvc.perform(get("/api/orders"))
				.andExpect(header().string("X-Content-Type-Options", "nosniff"))
				.andExpect(header().exists("Content-Security-Policy"));
	}

}
