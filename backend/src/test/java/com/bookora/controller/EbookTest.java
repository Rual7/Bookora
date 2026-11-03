package com.bookora.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;

import com.bookora.repository.CategoryRepository;
import com.bookora.repository.OrderRepository;
import com.jayway.jsonpath.JsonPath;

class EbookTest extends ApiTestBase {

	private static final byte[] PDF = "%PDF-1.4\n1 0 obj<<>>endobj\ntrailer<<>>\n%%EOF\n".getBytes(StandardCharsets.ISO_8859_1);
	private static final byte[] PDF_WITH_SCRIPT = "%PDF-1.4\n1 0 obj<</S/JavaScript /JS (app.alert(1))>>endobj\n%%EOF\n"
			.getBytes(StandardCharsets.ISO_8859_1);

	@Autowired
	private CategoryRepository categories;

	@Autowired
	private OrderRepository orders;

	private int bookId;
	private int productId;

	private void createEbookProduct(MockHttpSession admin, String format) throws Exception {
		String body = """
				{"title":"Digital %s","featured":false,"active":true,"authors":["Test Author"],"categoryIds":[%d],
				"products":[{"format":"%s","price":6.90,"stockQuantity":0,"active":true}]}
				""".formatted(UUID.randomUUID(), categories.findAll().get(0).getId(), format);
		String json = mvc.perform(post("/api/admin/books").with(csrf()).session(admin)
				.contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		bookId = JsonPath.read(json, "$.id");
		productId = JsonPath.read(json, "$.products[0].id");
	}

	private void upload(MockHttpSession admin, String filename, byte[] bytes, int expectedStatus) throws Exception {
		mvc.perform(multipart("/api/admin/books/" + bookId + "/products/" + productId + "/ebook")
				.file(new MockMultipartFile("file", filename, "application/octet-stream", bytes))
				.with(csrf()).session(admin))
				.andExpect(status().is(expectedStatus));
	}

	/** Buys the e-book as a new customer and returns the session and the id of the order line. */
	private Object[] buy(boolean paid) throws Exception {
		MockHttpSession customer = registerAndLogin("Buyer");
		mvc.perform(post("/api/cart/items").with(csrf()).session(customer).contentType(MediaType.APPLICATION_JSON)
				.content("{\"productId\":%d,\"quantity\":1}".formatted(productId))).andExpect(status().isOk());
		String order = mvc.perform(post("/api/checkout").with(csrf()).session(customer)
				.contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		if (paid) {
			orders.findByOrderNumber(JsonPath.read(order, "$.orderNumber")).orElseThrow().markPaid();
		}
		int itemId = JsonPath.read(order, "$.items[0].id");
		return new Object[] { customer, itemId };
	}

	private static byte[] epub(boolean withProgram) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (ZipOutputStream zip = new ZipOutputStream(out)) {
			zip.putNextEntry(new ZipEntry("mimetype"));
			zip.write("application/epub+zip".getBytes(StandardCharsets.US_ASCII));
			zip.closeEntry();
			zip.putNextEntry(new ZipEntry(withProgram ? "OEBPS/run.exe" : "OEBPS/chapter.xhtml"));
			zip.write("x".getBytes(StandardCharsets.US_ASCII));
			zip.closeEntry();
		}
		return out.toByteArray();
	}

	@Test
	void aValidPdfIsStoredAsCleanAndAPaidBuyerCanDownloadIt() throws Exception {
		MockHttpSession admin = adminSession();
		createEbookProduct(admin, "PDF");
		upload(admin, "book.pdf", PDF, 200);
		Object[] buyer = buy(true);

		mvc.perform(get("/api/downloads/" + buyer[1]).session((MockHttpSession) buyer[0]))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_PDF))
				.andExpect(header().string("Content-Disposition", containsString("attachment")))
				.andExpect(content().bytes(PDF));
	}

	@Test
	void theLibraryListsOnlyPaidEbooksAndMarksAvailability() throws Exception {
		MockHttpSession admin = adminSession();
		createEbookProduct(admin, "PDF");
		Object[] unpaid = buy(false);
		mvc.perform(get("/api/library").session((MockHttpSession) unpaid[0]))
				.andExpect(jsonPath("$.length()").value(0));

		Object[] paid = buy(true);
		mvc.perform(get("/api/library").session((MockHttpSession) paid[0]))
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].format").value("PDF"))
				.andExpect(jsonPath("$[0].available").value(false));

		upload(admin, "book.pdf", PDF, 200);
		mvc.perform(get("/api/library").session((MockHttpSession) paid[0]))
				.andExpect(jsonPath("$[0].available").value(true))
				.andExpect(jsonPath("$[0].orderItemId").value(paid[1]));
		mvc.perform(get("/api/library")).andExpect(status().isUnauthorized());
	}

	@Test
	void aPdfCanBeShownInTheBrowserButAnEpubIsAlwaysSaved() throws Exception {
		MockHttpSession admin = adminSession();
		createEbookProduct(admin, "PDF");
		upload(admin, "book.pdf", PDF, 200);
		Object[] pdfBuyer = buy(true);
		mvc.perform(get("/api/downloads/" + pdfBuyer[1]).param("inline", "true").session((MockHttpSession) pdfBuyer[0]))
				.andExpect(header().string("Content-Disposition", startsWith("inline")));

		createEbookProduct(admin, "EPUB");
		upload(admin, "book.epub", epub(false), 200);
		Object[] epubBuyer = buy(true);
		mvc.perform(get("/api/downloads/" + epubBuyer[1]).param("inline", "true").session((MockHttpSession) epubBuyer[0]))
				.andExpect(header().string("Content-Disposition", startsWith("attachment")));
	}

	@Test
	void aRefusedFileDoesNotReplaceTheOneThatWasThereBeforeAndIsReported() throws Exception {
		MockHttpSession admin = adminSession();
		createEbookProduct(admin, "EPUB");
		mvc.perform(multipart("/api/admin/books/" + bookId + "/products/" + productId + "/ebook")
				.file(new MockMultipartFile("file", "good.epub", "application/octet-stream", epub(false)))
				.with(csrf()).session(admin))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.refused").doesNotExist())
				.andExpect(jsonPath("$.book.products[0].ebook.scanStatus").value("CLEAN"));

		mvc.perform(multipart("/api/admin/books/" + bookId + "/products/" + productId + "/ebook")
				.file(new MockMultipartFile("file", "bad.epub", "application/octet-stream", epub(true)))
				.with(csrf()).session(admin))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.refused.originalFilename").value("bad.epub"))
				.andExpect(jsonPath("$.refused.scanStatus").value("REJECTED"))
				.andExpect(jsonPath("$.book.products[0].ebook.originalFilename").value("good.epub"))
				.andExpect(jsonPath("$.book.products[0].ebook.scanStatus").value("CLEAN"));

		Object[] buyer = buy(true);
		mvc.perform(get("/api/downloads/" + buyer[1]).session((MockHttpSession) buyer[0])).andExpect(status().isOk());
	}

	@Test
	void aPdfWithAScriptIsRejectedAndCannotBeDownloaded() throws Exception {
		MockHttpSession admin = adminSession();
		createEbookProduct(admin, "PDF");
		upload(admin, "book.pdf", PDF_WITH_SCRIPT, 200);
		Object[] buyer = buy(true);

		mvc.perform(get("/api/admin/books/" + bookId).session(admin))
				.andExpect(jsonPath("$.products[0].ebook.scanStatus").value("REJECTED"));
		mvc.perform(get("/api/downloads/" + buyer[1]).session((MockHttpSession) buyer[0]))
				.andExpect(status().isConflict());
	}

	@Test
	void theRealTypeMustMatchTheNameAndTheProduct() throws Exception {
		MockHttpSession admin = adminSession();
		createEbookProduct(admin, "PDF");

		upload(admin, "book.pdf", "just text".getBytes(), 400);
		upload(admin, "book.exe", PDF, 400);
		upload(admin, "book.epub", PDF, 400);
	}

	@Test
	void aFileOverTwelveMegabytesIsRefused() throws Exception {
		MockHttpSession admin = adminSession();
		createEbookProduct(admin, "PDF");

		upload(admin, "big.pdf", new byte[12 * 1024 * 1024 + 1], 400);
	}

	@Test
	void anUnpaidOrderCannotDownload() throws Exception {
		MockHttpSession admin = adminSession();
		createEbookProduct(admin, "PDF");
		upload(admin, "book.pdf", PDF, 200);
		Object[] buyer = buy(false);

		mvc.perform(get("/api/downloads/" + buyer[1]).session((MockHttpSession) buyer[0]))
				.andExpect(status().isConflict());
	}

	@Test
	void anotherCustomerCannotDownloadSomeoneElsesPurchase() throws Exception {
		MockHttpSession admin = adminSession();
		createEbookProduct(admin, "PDF");
		upload(admin, "book.pdf", PDF, 200);
		Object[] buyer = buy(true);

		mvc.perform(get("/api/downloads/" + buyer[1]).session(registerAndLogin("Eve")))
				.andExpect(status().isNotFound());
		mvc.perform(get("/api/downloads/" + buyer[1])).andExpect(status().isUnauthorized());
	}

	@Test
	void customersCannotUpload() throws Exception {
		MockHttpSession admin = adminSession();
		createEbookProduct(admin, "PDF");

		mvc.perform(multipart("/api/admin/books/" + bookId + "/products/" + productId + "/ebook")
				.file(new MockMultipartFile("file", "book.pdf", "application/pdf", PDF))
				.with(csrf()).session(registerAndLogin("Mallory")))
				.andExpect(status().isForbidden());
	}

}
