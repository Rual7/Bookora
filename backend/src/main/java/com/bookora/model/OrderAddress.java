package com.bookora.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** The address as it was when the order was placed; later edits to the saved address do not change old orders. */
@Entity
@Table(name = "order_address")
public class OrderAddress {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "recipient_name", nullable = false)
	private String recipientName;

	@Column(nullable = false)
	private String phone;

	@Column(nullable = false)
	private String country;

	@Column(nullable = false)
	private String city;

	@Column(name = "county_region")
	private String countyRegion;

	@Column(name = "postal_code", nullable = false)
	private String postalCode;

	@Column(name = "street_address", nullable = false)
	private String streetAddress;

	@Column(name = "building_details")
	private String buildingDetails;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt = Instant.now();

	protected OrderAddress() {
	}

	public OrderAddress(Address address) {
		this.recipientName = address.getRecipientName();
		this.phone = address.getPhone();
		this.country = address.getCountry();
		this.city = address.getCity();
		this.countyRegion = address.getCountyRegion();
		this.postalCode = address.getPostalCode();
		this.streetAddress = address.getStreetAddress();
		this.buildingDetails = address.getBuildingDetails();
	}

	public String getRecipientName() {
		return recipientName;
	}

	public String getPhone() {
		return phone;
	}

	public String getCountry() {
		return country;
	}

	public String getCity() {
		return city;
	}

	public String getCountyRegion() {
		return countyRegion;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public String getStreetAddress() {
		return streetAddress;
	}

	public String getBuildingDetails() {
		return buildingDetails;
	}

}
