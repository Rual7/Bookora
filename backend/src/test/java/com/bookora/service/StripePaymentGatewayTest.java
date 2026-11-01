package com.bookora.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;

import com.bookora.exception.BadRequestException;

public class StripePaymentGatewayTest {

	public static final String SECRET = "whsec_test_secret";
	public static final String PAYLOAD = """
			{"id":"evt_1","type":"checkout.session.completed",
			 "data":{"object":{"id":"cs_1","payment_intent":"pi_1","payment_status":"paid"}}}""";

	private final StripePaymentGateway gateway = new StripePaymentGateway("sk_test_x", SECRET, "http://localhost:5173");

	/** Builds the Stripe-Signature header the way Stripe does: an HMAC-SHA256 of "timestamp.payload". */
	public static String sign(String payload, String secret, long timestamp) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
			byte[] hash = mac.doFinal((timestamp + "." + payload).getBytes(StandardCharsets.UTF_8));
			return "t=" + timestamp + ",v1=" + HexFormat.of().formatHex(hash);
		} catch (Exception problem) {
			throw new IllegalStateException(problem);
		}
	}

	@Test
	void aCorrectlySignedEventIsRead() {
		PaymentGateway.Event event = gateway.readEvent(PAYLOAD, sign(PAYLOAD, SECRET, Instant.now().getEpochSecond()));

		assertThat(event.id()).isEqualTo("evt_1");
		assertThat(event.type()).isEqualTo("checkout.session.completed");
		assertThat(event.sessionId()).isEqualTo("cs_1");
		assertThat(event.paymentId()).isEqualTo("pi_1");
		assertThat(event.paid()).isTrue();
	}

	@Test
	void aWrongSignatureIsRejected() {
		String header = sign(PAYLOAD, "whsec_someone_else", Instant.now().getEpochSecond());

		assertThatThrownBy(() -> gateway.readEvent(PAYLOAD, header)).isInstanceOf(BadRequestException.class);
	}

	@Test
	void aChangedBodyIsRejected() {
		String header = sign(PAYLOAD, SECRET, Instant.now().getEpochSecond());

		assertThatThrownBy(() -> gateway.readEvent(PAYLOAD.replace("paid", "unpaid"), header))
				.isInstanceOf(BadRequestException.class);
	}

	@Test
	void anOldSignedEventIsRejectedSoItCannotBeReplayed() {
		String header = sign(PAYLOAD, SECRET, Instant.now().minusSeconds(3600).getEpochSecond());

		assertThatThrownBy(() -> gateway.readEvent(PAYLOAD, header)).isInstanceOf(BadRequestException.class);
	}

	@Test
	void aMissingOrBrokenHeaderIsRejected() {
		assertThatThrownBy(() -> gateway.readEvent(PAYLOAD, null)).isInstanceOf(BadRequestException.class);
		assertThatThrownBy(() -> gateway.readEvent(PAYLOAD, "garbage")).isInstanceOf(BadRequestException.class);
	}

	@Test
	void withoutAWebhookSecretNothingIsAccepted() {
		StripePaymentGateway unconfigured = new StripePaymentGateway("", "", "http://localhost:5173");
		String header = sign(PAYLOAD, SECRET, Instant.now().getEpochSecond());

		assertThatThrownBy(() -> unconfigured.readEvent(PAYLOAD, header)).isInstanceOf(BadRequestException.class);
		assertThat(unconfigured.isConfigured()).isFalse();
	}

}
