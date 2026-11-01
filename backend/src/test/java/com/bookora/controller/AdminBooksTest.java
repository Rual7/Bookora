package com.bookora.controller;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;

import com.bookora.repository.CategoryRepository;
import com.jayway.jsonpath.JsonPath;

class AdminBooksTest extends ApiTestBase {

	@Autowired
	private CategoryRepository categories;

	private String bookJson(String title, String products) {
		long categoryId = categories.findAll().get(0).getId();
		return """
				{"title":"%s","description":"A test book","publisher":"Test House","languageCode":"en","pageCount":120,
				"featured":false,"active":true,"authors":["Test Author"],"categoryIds":[%d],"products":%s}
				""".formatted(title, categoryId, products);
	}

	private static final String PAPERBACK = """
			[{"format":"PAPERBACK","isbn":"","price":12.90,"stockQuantity":5,"active":true}]
			""";

	private String createBook(MockHttpSession admin, String title) throws Exception {
		return mvc.perform(post("/api/admin/books").with(csrf()).session(admin).contentType(MediaType.APPLICATION_JSON)
				.content(bookJson(title, PAPERBACK)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
	}

	private static String idOf(String json) {
		return json.replaceAll("^\\{\"id\":(\\d+).*", "$1");
	}

	private static byte[] png() throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(new BufferedImage(40, 60, BufferedImage.TYPE_INT_ARGB), "png", out);
		return out.toByteArray();
	}

	@Test
	void onlyAdministratorsReachTheAdminApi() throws Exception {
		mvc.perform(get("/api/admin/books")).andExpect(status().isUnauthorized());
		mvc.perform(get("/api/admin/books").session(registerAndLogin("Ana"))).andExpect(status().isForbidden());
		mvc.perform(get("/api/admin/books").session(adminSession())).andExpect(status().isOk());
	}

	@Test
	void adminCreatesABookWithAGeneratedSlugAndSku() throws Exception {
		MockHttpSession admin = adminSession();
		String title = "Café Stories " + UUID.randomUUID().toString().substring(0, 8);

		mvc.perform(post("/api/admin/books").with(csrf()).session(admin).contentType(MediaType.APPLICATION_JSON)
				.content(bookJson(title, PAPERBACK)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.slug").value(startsWith("cafe-stories-")))
				.andExpect(jsonPath("$.authors[0]").value("Test Author"))
				.andExpect(jsonPath("$.products[0].sku").value(startsWith("BK-")))
				.andExpect(jsonPath("$.products[0].isbn").value(nullValue()));
	}

	@Test
	void aBookNeedsATitleAnAuthorAndAProduct() throws Exception {
		mvc.perform(post("/api/admin/books").with(csrf()).session(adminSession()).contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"\",\"featured\":false,\"active\":true,\"authors\":[],\"products\":[]}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.title").value("Enter a title"))
				.andExpect(jsonPath("$.errors.authors").exists())
				.andExpect(jsonPath("$.errors.products").exists());
	}

	@Test
	void twoProductsOfTheSameFormatAreRefused() throws Exception {
		String twice = """
				[{"format":"PAPERBACK","price":10,"stockQuantity":1,"active":true},
				 {"format":"PAPERBACK","price":11,"stockQuantity":1,"active":true}]
				""";
		mvc.perform(post("/api/admin/books").with(csrf()).session(adminSession()).contentType(MediaType.APPLICATION_JSON)
				.content(bookJson("Twice", twice)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void aFormatLeftOutOfAnUpdateIsHiddenNotDeleted() throws Exception {
		MockHttpSession admin = adminSession();
		String both = """
				[{"format":"PAPERBACK","price":12.90,"stockQuantity":5,"active":true},
				 {"format":"EPUB","price":6.90,"stockQuantity":0,"active":true}]
				""";
		String created = mvc.perform(post("/api/admin/books").with(csrf()).session(admin)
				.contentType(MediaType.APPLICATION_JSON).content(bookJson("Both Formats", both)))
				.andReturn().getResponse().getContentAsString();

		mvc.perform(put("/api/admin/books/" + idOf(created)).with(csrf()).session(admin)
				.contentType(MediaType.APPLICATION_JSON).content(bookJson("Both Formats", PAPERBACK)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.products.length()").value(2))
				.andExpect(jsonPath("$.products[?(@.format=='EPUB')].active").value(false));
	}

	@Test
	void hiddenBooksStayInTheAdminListButLeaveTheShop() throws Exception {
		MockHttpSession admin = adminSession();
		String title = "Hidden " + UUID.randomUUID().toString().substring(0, 8);
		String created = createBook(admin, title);
		String hidden = bookJson(title, PAPERBACK).replace("\"active\":true,\"authors\"", "\"active\":false,\"authors\"");

		mvc.perform(put("/api/admin/books/" + idOf(created)).with(csrf()).session(admin)
				.contentType(MediaType.APPLICATION_JSON).content(hidden))
				.andExpect(status().isOk());

		mvc.perform(get("/api/admin/books").param("q", title).session(admin))
				.andExpect(jsonPath("$.content[*].title", hasItem(title)));
		mvc.perform(get("/api/books").param("q", title))
				.andExpect(jsonPath("$.totalElements").value(0));
	}

	private MockMultipartFile image(String name) throws Exception {
		return new MockMultipartFile("files", name, "image/png", png());
	}

	@Test
	void imagesAreReEncodedAsJpegAndTheFirstOneIsTheCover() throws Exception {
		MockHttpSession admin = adminSession();
		String id = idOf(createBook(admin, "Covered " + UUID.randomUUID().toString().substring(0, 8)));

		String response = mvc.perform(multipart("/api/admin/books/" + id + "/images")
				.file(image("one.png")).file(image("two.png"))
				.with(csrf()).session(admin))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.images.length()").value(2))
				.andExpect(jsonPath("$.coverImagePath").value(startsWith("/api/covers/")))
				.andReturn().getResponse().getContentAsString();
		String path = JsonPath.read(response, "$.images[0].path");
		assertEquals(path, JsonPath.read(response, "$.coverImagePath"));

		byte[] served = mvc.perform(get(path))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.IMAGE_JPEG))
				.andReturn().getResponse().getContentAsByteArray();
		assertEquals((byte) 0xFF, served[0]);
		assertEquals((byte) 0xD8, served[1]);
	}

	@Test
	void anotherImageCanBecomeTheCoverAndImagesCanBeRemoved() throws Exception {
		MockHttpSession admin = adminSession();
		String id = idOf(createBook(admin, "Gallery " + UUID.randomUUID().toString().substring(0, 8)));
		String added = mvc.perform(multipart("/api/admin/books/" + id + "/images")
				.file(image("one.png")).file(image("two.png")).with(csrf()).session(admin))
				.andReturn().getResponse().getContentAsString();
		int secondId = JsonPath.read(added, "$.images[1].id");
		String secondPath = JsonPath.read(added, "$.images[1].path");

		mvc.perform(put("/api/admin/books/" + id + "/images/" + secondId + "/main").with(csrf()).session(admin))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.coverImagePath").value(secondPath))
				.andExpect(jsonPath("$.images[0].id").value(secondId));

		mvc.perform(delete("/api/admin/books/" + id + "/images/" + secondId).with(csrf()).session(admin))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.images.length()").value(1));
		mvc.perform(get(secondPath)).andExpect(status().isNotFound());
		mvc.perform(delete("/api/admin/books/" + id + "/images/" + secondId).with(csrf()).session(admin))
				.andExpect(status().isNotFound());
	}

	@Test
	void publicBookPageListsEveryImage() throws Exception {
		MockHttpSession admin = adminSession();
		String created = createBook(admin, "Shown " + UUID.randomUUID().toString().substring(0, 8));
		String slug = JsonPath.read(created, "$.slug");
		mvc.perform(multipart("/api/admin/books/" + idOf(created) + "/images")
				.file(image("one.png")).file(image("two.png")).file(image("three.png")).with(csrf()).session(admin));

		mvc.perform(get("/api/books/" + slug))
				.andExpect(jsonPath("$.images.length()").value(3));
	}

	@Test
	void aBookHasAtMostEightImages() throws Exception {
		MockHttpSession admin = adminSession();
		String id = idOf(createBook(admin, "Crowded " + UUID.randomUUID().toString().substring(0, 8)));
		var upload = multipart("/api/admin/books/" + id + "/images");
		for (int i = 0; i < 9; i++) {
			upload.file(image("p" + i + ".png"));
		}

		mvc.perform(upload.with(csrf()).session(admin)).andExpect(status().isBadRequest());
	}

	@Test
	void aFileThatIsNotAnImageIsRefused() throws Exception {
		MockHttpSession admin = adminSession();
		String id = idOf(createBook(admin, "Fake " + UUID.randomUUID().toString().substring(0, 8)));

		mvc.perform(multipart("/api/admin/books/" + id + "/images")
				.file(new MockMultipartFile("files", "cover.png", "image/png", "<script>alert(1)</script>".getBytes()))
				.with(csrf()).session(admin))
				.andExpect(status().isBadRequest());
		mvc.perform(multipart("/api/admin/books/" + id + "/images")
				.file(new MockMultipartFile("files", "cover.gif", "image/gif", png()))
				.with(csrf()).session(admin))
				.andExpect(status().isBadRequest());
	}

	@Test
	void errorsNameTheFieldTheyBelongTo() throws Exception {
		MockHttpSession admin = adminSession();
		String isbn = "978" + (System.nanoTime() % 1_000_000_000L);
		String withIsbn = "[{\"format\":\"PAPERBACK\",\"isbn\":\"" + isbn + "\",\"price\":9.90,\"stockQuantity\":1,\"active\":true}]";
		mvc.perform(post("/api/admin/books").with(csrf()).session(admin).contentType(MediaType.APPLICATION_JSON)
				.content(bookJson("First " + UUID.randomUUID(), withIsbn)))
				.andExpect(status().isCreated());

		mvc.perform(post("/api/admin/books").with(csrf()).session(admin).contentType(MediaType.APPLICATION_JSON)
				.content(bookJson("Second " + UUID.randomUUID(), withIsbn)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors['products[0].isbn']").value("Another product already uses this ISBN"));

		String noPrice = "[{\"format\":\"PAPERBACK\",\"stockQuantity\":1,\"active\":true}]";
		mvc.perform(post("/api/admin/books").with(csrf()).session(admin).contentType(MediaType.APPLICATION_JSON)
				.content(bookJson("Third " + UUID.randomUUID(), noPrice)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors['products[0].price']").value("Enter a price"));
	}
	@Test
	void unknownCoverNamesAreNotFound() throws Exception {
		mvc.perform(get("/api/covers/not-a-cover.jpg")).andExpect(status().isNotFound());
		mvc.perform(get("/api/covers/0123456789abcdef0123456789abcdef.jpg")).andExpect(status().isNotFound());
	}

}
