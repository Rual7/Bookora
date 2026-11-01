package com.bookora.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.bookora.model.CustomerOrder;
import com.bookora.model.OrderItem;
import com.bookora.model.Product;
import com.bookora.repository.ProductRepository;

/** Cancels an unpaid order and puts its printed books back in stock. Call it inside a transaction. */
@Component
public class OrderCancellation {

	private static final Logger log = LoggerFactory.getLogger(OrderCancellation.class);

	private final ProductRepository products;

	public OrderCancellation(ProductRepository products) {
		this.products = products;
	}

	public void cancel(CustomerOrder order) {
		List<Long> ids = order.getItems().stream().map(item -> item.getProduct().getId()).toList();
		// Locked like at checkout, so a purchase that happens at the same moment sees the right stock.
		List<Product> locked = products.findAllForUpdate(ids);

		for (OrderItem item : order.getItems()) {
			if (item.getProductFormat().isDigital()) continue;
			locked.stream()
					.filter(product -> product.getId().equals(item.getProduct().getId()))
					.findFirst()
					.ifPresent(product -> product.restoreStock(item.getQuantity()));
		}
		order.markCancelled();
		log.info("Order cancelled and stock released: number={}", order.getOrderNumber());
	}

}
