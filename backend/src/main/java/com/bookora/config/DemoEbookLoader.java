package com.bookora.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.bookora.model.ProductFormat;
import com.bookora.repository.DigitalProduct;
import com.bookora.repository.ProductRepository;
import com.bookora.service.EbookService;

/**
 * Gives the demo e-books their files, from the small PDF and EPUB copies bundled in {@code demo/ebooks}.
 * It runs at start-up and only fills what is missing, so a file an administrator uploaded is kept.
 */
@Component
@ConditionalOnBooleanProperty("bookora.demo.ebooks")
public class DemoEbookLoader implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(DemoEbookLoader.class);

	private final ProductRepository products;
	private final EbookService ebooks;

	public DemoEbookLoader(ProductRepository products, EbookService ebooks) {
		this.products = products;
		this.ebooks = ebooks;
	}

	@Override
	public void run(ApplicationArguments args) throws IOException {
		int attached = 0;
		for (DigitalProduct product : products.findDigitalOnSale()) {
			String filename = product.bookSlug() + (product.format() == ProductFormat.PDF ? ".pdf" : ".epub");
			ClassPathResource file = new ClassPathResource("demo/ebooks/" + filename);
			if (file.exists() && !ebooks.hasStoredFile(product.productId())) {
				ebooks.attach(product.productId(), filename, file.getContentAsByteArray());
				attached++;
			}
		}
		if (attached > 0) {
			log.info("Demo e-book files attached: count={}", attached);
		}
	}

}
