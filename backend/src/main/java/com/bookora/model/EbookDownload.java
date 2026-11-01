package com.bookora.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** One row for every time a customer downloaded an e-book. */
@Entity
@Table(name = "ebook_download")
public class EbookDownload {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "order_item_id", nullable = false)
	private OrderItem orderItem;

	@Column(name = "downloaded_at", nullable = false)
	private Instant downloadedAt = Instant.now();

	protected EbookDownload() {
	}

	public EbookDownload(OrderItem orderItem) {
		this.orderItem = orderItem;
	}

	public Long getId() {
		return id;
	}

	public OrderItem getOrderItem() {
		return orderItem;
	}

	public Instant getDownloadedAt() {
		return downloadedAt;
	}

}
