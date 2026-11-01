package com.bookora.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Every few minutes cancels unpaid orders that nobody is paying for, so their stock comes back. */
@Component
public class StaleOrderSweeper {

	private static final Logger log = LoggerFactory.getLogger(StaleOrderSweeper.class);

	private final PaymentService paymentService;

	public StaleOrderSweeper(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	@Scheduled(initialDelayString = "PT1M", fixedDelayString = "PT5M")
	void run() {
		int cancelled = paymentService.cancelStaleOrders();
		if (cancelled > 0) {
			log.info("Stale unpaid orders cancelled: count={}", cancelled);
		}
	}

}
