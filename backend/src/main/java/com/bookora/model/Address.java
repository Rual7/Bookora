package com.bookora.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** A delivery address the customer saved in their account. */
@Entity
@Table(name = "address")
public class Address extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private AppUser user;

	private String label;

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

	@Column(name = "is_default", nullable = false)
	private boolean defaultAddress;

	protected Address() {
	}

	public Address(AppUser user) {
		this.user = user;
	}

	public AppUser getUser() {
		return user;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getRecipientName() {
		return recipientName;
	}

	public void setRecipientName(String recipientName) {
		this.recipientName = recipientName;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCountyRegion() {
		return countyRegion;
	}

	public void setCountyRegion(String countyRegion) {
		this.countyRegion = countyRegion;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getStreetAddress() {
		return streetAddress;
	}

	public void setStreetAddress(String streetAddress) {
		this.streetAddress = streetAddress;
	}

	public String getBuildingDetails() {
		return buildingDetails;
	}

	public void setBuildingDetails(String buildingDetails) {
		this.buildingDetails = buildingDetails;
	}

	public boolean isDefaultAddress() {
		return defaultAddress;
	}

	public void setDefaultAddress(boolean defaultAddress) {
		this.defaultAddress = defaultAddress;
	}

}
