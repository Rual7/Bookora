package com.bookora.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/** Uses the real limit (100 a minute); it has its own context so no other test shares its counters. */
@SpringBootTest(properties = "bookora.rate-limit.requests-per-minute=100")
@AutoConfigureMockMvc
class RateLimitTest {

	@Autowired
	private MockMvc mvc;

	private static RequestPostProcessor from(String address) {
		return request -> {
			request.setRemoteAddr(address);
			return request;
		};
	}

	@Test
	void theHundredAndFirstRequestInAMinuteIsRefused() throws Exception {
		for (int i = 0; i < 100; i++) {
			mvc.perform(get("/api/categories").with(from("10.1.0.1"))).andExpect(status().isOk());
		}

		mvc.perform(get("/api/categories").with(from("10.1.0.1")))
				.andExpect(status().isTooManyRequests())
				.andExpect(header().exists("Retry-After"))
				.andExpect(jsonPath("$.status").value(429));
	}

	@Test
	void anotherAddressIsNotAffected() throws Exception {
		for (int i = 0; i < 101; i++) {
			mvc.perform(get("/api/categories").with(from("10.1.0.2")));
		}

		mvc.perform(get("/api/categories").with(from("10.1.0.3"))).andExpect(status().isOk());
	}

	@Test
	void coverImagesDoNotCountAgainstTheLimit() throws Exception {
		for (int i = 0; i < 150; i++) {
			mvc.perform(get("/api/covers/missing.jpg").with(from("10.1.0.4"))).andExpect(status().isNotFound());
		}

		mvc.perform(get("/api/categories").with(from("10.1.0.4"))).andExpect(status().isOk());
	}

}
