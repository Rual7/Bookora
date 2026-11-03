package com.bookora.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.bookora.repository.BookRepository;

class SeoTest extends ApiTestBase {

	@Autowired
	private BookRepository books;

	@Test
	void theSitemapListsThePagesAndEveryActiveBookForVisitors() throws Exception {
		String slug = books.findActiveSlugs().get(0);

		mvc.perform(get("/sitemap.xml"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith("application/xml"))
				.andExpect(content().string(containsString("<loc>http://localhost:5173/books</loc>")))
				.andExpect(content().string(containsString("<loc>http://localhost:5173/books/" + slug + "</loc>")))
				.andExpect(content().string(not(containsString("/admin"))));
	}

	@Test
	void robotsKeepsPrivatePagesOutAndPointsToTheSitemap() throws Exception {
		mvc.perform(get("/robots.txt"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Disallow: /api/")))
				.andExpect(content().string(containsString("Disallow: /cart")))
				.andExpect(content().string(containsString("Sitemap: http://localhost:5173/sitemap.xml")));
	}

}
