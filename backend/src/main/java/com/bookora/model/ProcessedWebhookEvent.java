package com.bookora.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** A webhook event we already handled. The database refuses the same (provider, event id) twice. */
@Entity
@Table(name = "processed_webhook_event")
public class ProcessedWebhookEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String provider;

	@Column(name = "provider_event_id", nullable = false)
	private String providerEventId;

	@Column(name = "event_type", nullable = false)
	private String eventType;

	@Column(name = "processed_at", nullable = false, updatable = false)
	private Instant processedAt = Instant.now();

	protected ProcessedWebhookEvent() {
	}

	public ProcessedWebhookEvent(String provider, String providerEventId, String eventType) {
		this.provider = provider;
		this.providerEventId = providerEventId;
		this.eventType = eventType;
	}

}
