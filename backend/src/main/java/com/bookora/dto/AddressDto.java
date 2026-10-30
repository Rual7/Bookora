package com.bookora.dto;

public record AddressDto(
		Long id,
		String label,
		String recipientName,
		String phone,
		String country,
		String city,
		String countyRegion,
		String postalCode,
		String streetAddress,
		String buildingDetails,
		boolean defaultAddress) {
}
