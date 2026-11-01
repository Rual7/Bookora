package com.bookora.model;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "ebook_file")
public class EbookFile extends BaseEntity {

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "product_id", nullable = false, unique = true)
	private Product product;

	@Column(name = "storage_key", nullable = false, unique = true)
	private String storageKey;

	@Column(name = "original_filename", nullable = false)
	private String originalFilename;

	@Column(name = "mime_type", nullable = false)
	private String mimeType;

	@Column(name = "file_size_bytes", nullable = false)
	private long fileSizeBytes;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "sha256_hash", nullable = false, length = 64)
	private String sha256Hash;

	@Enumerated(EnumType.STRING)
	@Column(name = "scan_status", nullable = false)
	private ScanStatus scanStatus = ScanStatus.PENDING;

	protected EbookFile() {
	}

	public EbookFile(Product product, String storageKey, String originalFilename, String mimeType, long fileSizeBytes,
			String sha256Hash, ScanStatus scanStatus) {
		this.product = product;
		this.storageKey = storageKey;
		this.originalFilename = originalFilename;
		this.mimeType = mimeType;
		this.fileSizeBytes = fileSizeBytes;
		this.sha256Hash = sha256Hash;
		this.scanStatus = scanStatus;
	}

	/** Swaps in a newly uploaded file; the caller removes the old file from disk. */
	public void replaceWith(String storageKey, String originalFilename, String mimeType, long fileSizeBytes,
			String sha256Hash, ScanStatus scanStatus) {
		this.storageKey = storageKey;
		this.originalFilename = originalFilename;
		this.mimeType = mimeType;
		this.fileSizeBytes = fileSizeBytes;
		this.sha256Hash = sha256Hash;
		this.scanStatus = scanStatus;
	}

	public Product getProduct() {
		return product;
	}

	public String getStorageKey() {
		return storageKey;
	}

	public String getOriginalFilename() {
		return originalFilename;
	}

	public String getMimeType() {
		return mimeType;
	}

	public long getFileSizeBytes() {
		return fileSizeBytes;
	}

	public String getSha256Hash() {
		return sha256Hash;
	}

	public ScanStatus getScanStatus() {
		return scanStatus;
	}

}
