package com.bookora.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bookora.dto.PaymentLinkDto;
import com.bookora.exception.ConflictException;
import com.bookora.exception.ResourceNotFoundException;
import com.bookora.exception.ServiceUnavailableException;
import com.bookora.model.AppUser;
import com.bookora.model.CustomerOrder;
import com.bookora.model.OrderStatus;
import com.bookora.model.Payment;
import com.bookora.model.PaymentStatus;
import com.bookora.repository.OrderRepository;
import com.bookora.repository.PaymentRepository;
import com.bookora.repository.ProcessedWebhookEventRepository;
import com.bookora.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

	private static final String EMAIL = "ana@example.com";
	private static final String NUMBER = "BK-2026-001234";

	@Mock
	private PaymentGateway gateway;
	@Mock
	private OrderRepository orders;
	@Mock
	private PaymentRepository payments;
	@Mock
	private ProcessedWebhookEventRepository processedEvents;
	@Mock
	private UserRepository users;
	@Mock
	private OrderCancellation cancellation;

	private PaymentServiceImpl service;
	private AppUser user;
	private CustomerOrder order;

	@BeforeEach
	void setUp() {
		service = new PaymentServiceImpl(gateway, orders, payments, processedEvents, users, cancellation);
		user = mock(AppUser.class);
		order = new CustomerOrder(user, NUMBER, "EUR", null);
		lenient().when(user.getId()).thenReturn(1L);
		lenient().when(gateway.isConfigured()).thenReturn(true);
		lenient().when(users.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(user));
		lenient().when(orders.findByOrderNumberAndUserId(NUMBER, 1L)).thenReturn(Optional.of(order));
	}

	private PaymentGateway.Event completed(boolean paid) {
		return new PaymentGateway.Event("evt_1", "checkout.session.completed", "cs_1", "pi_1", paid);
	}

	private Payment pendingPayment(Instant expiresAt) {
		return new Payment(order, "cs_1", expiresAt);
	}

	@Test
	void paymentNeedsTheProviderToBeSetUp() {
		when(gateway.isConfigured()).thenReturn(false);

		assertThatThrownBy(() -> service.startPayment(EMAIL, NUMBER)).isInstanceOf(ServiceUnavailableException.class);
	}

	@Test
	void anotherCustomersOrderIsNotFound() {
		when(orders.findByOrderNumberAndUserId(NUMBER, 1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.startPayment(EMAIL, NUMBER)).isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void aPaidOrderCannotBePaidAgain() {
		order.markPaid();

		assertThatThrownBy(() -> service.startPayment(EMAIL, NUMBER)).isInstanceOf(ConflictException.class);
		verify(gateway, never()).createSession(any(), any());
	}

	@Test
	void startingPaymentOpensASessionAndRemembersIt() {
		when(gateway.createSession(any(), any())).thenReturn(new PaymentGateway.Session("cs_1", "https://pay.test/cs_1"));

		PaymentLinkDto link = service.startPayment(EMAIL, NUMBER);

		assertThat(link.url()).isEqualTo("https://pay.test/cs_1");
		verify(payments).save(any(Payment.class));
	}

	@Test
	void anOpenSessionIsReusedInsteadOfOpeningAnother() {
		when(payments.findFirstByOrderIdAndStatusOrderByIdDesc(any(), any()))
				.thenReturn(Optional.of(pendingPayment(Instant.now().plusSeconds(600))));
		when(gateway.findOpenSessionUrl("cs_1")).thenReturn(Optional.of("https://pay.test/cs_1"));

		assertThat(service.startPayment(EMAIL, NUMBER).url()).isEqualTo("https://pay.test/cs_1");
		verify(gateway, never()).createSession(any(), any());
	}

	@Test
	void aPaidCheckoutMarksThePaymentAndTheOrderPaid() {
		Payment payment = pendingPayment(Instant.now().plusSeconds(600));
		when(gateway.readEvent("body", "sig")).thenReturn(completed(true));
		when(payments.findByProviderSessionId("cs_1")).thenReturn(Optional.of(payment));

		service.handleWebhook("body", "sig");

		assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
		assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCEEDED);
		verify(processedEvents).save(any());
	}

	@Test
	void aRepeatedEventChangesNothing() {
		when(gateway.readEvent("body", "sig")).thenReturn(completed(true));
		when(processedEvents.existsByProviderAndProviderEventId("STRIPE", "evt_1")).thenReturn(true);

		service.handleWebhook("body", "sig");

		assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
		verify(processedEvents, never()).save(any());
		verify(payments, never()).findByProviderSessionId(any());
	}

	@Test
	void aCheckoutThatIsNotPaidYetLeavesTheOrderAlone() {
		when(gateway.readEvent("body", "sig")).thenReturn(completed(false));

		service.handleWebhook("body", "sig");

		assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
	}

	@Test
	void anExpiredSessionCancelsTheOrderAndReleasesItsStock() {
		Payment payment = pendingPayment(Instant.now().minusSeconds(5));
		when(gateway.readEvent("body", "sig"))
				.thenReturn(new PaymentGateway.Event("evt_2", "checkout.session.expired", "cs_1", null, false));
		when(payments.findByProviderSessionId("cs_1")).thenReturn(Optional.of(payment));

		service.handleWebhook("body", "sig");

		assertThat(payment.getStatus()).isEqualTo(PaymentStatus.EXPIRED);
		verify(cancellation).cancel(order);
	}

	@Test
	void anExpiredSessionKeepsTheOrderWhenAnotherSessionIsStillOpen() {
		Payment payment = pendingPayment(Instant.now().minusSeconds(5));
		when(gateway.readEvent("body", "sig"))
				.thenReturn(new PaymentGateway.Event("evt_2", "checkout.session.expired", "cs_1", null, false));
		when(payments.findByProviderSessionId("cs_1")).thenReturn(Optional.of(payment));
		when(payments.existsByOrderIdAndStatusAndExpiresAtAfter(any(), any(), any())).thenReturn(true);

		service.handleWebhook("body", "sig");

		verify(cancellation, never()).cancel(any());
	}

	@Test
	void aPaymentForACancelledOrderDoesNotBringItBack() {
		order.markCancelled();
		when(gateway.readEvent("body", "sig")).thenReturn(completed(true));
		when(payments.findByProviderSessionId("cs_1")).thenReturn(Optional.of(pendingPayment(Instant.now())));

		service.handleWebhook("body", "sig");

		assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
	}

	@Test
	void staleOrdersAreCancelled() {
		when(orders.findStaleUnpaid(any(), any())).thenReturn(java.util.List.of(order));

		assertThat(service.cancelStaleOrders()).isEqualTo(1);
		verify(cancellation).cancel(order);
	}

}
