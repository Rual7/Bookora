package com.bookora.dto;

public record ShippingAddressDto(
		String recipientName,
		String phone,
		String country,
		String city,
		String countyRegion,
		String postalCode,
		String streetAddress,
		String buildingDetails) {
}
