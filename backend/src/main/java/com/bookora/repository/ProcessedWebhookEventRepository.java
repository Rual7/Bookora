package com.bookora.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bookora.model.ProcessedWebhookEvent;

public interface ProcessedWebhookEventRepository extends JpaRepository<ProcessedWebhookEvent, Long> {

	boolean existsByProviderAndProviderEventId(String provider, String providerEventId);

}
