package com.bookora.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressRequest(
		@Size(max = 50) String label,
		@NotBlank @Size(max = 200) String recipientName,
		@NotBlank @Size(max = 30) String phone,
		@NotBlank @Size(max = 100) String country,
		@NotBlank @Size(max = 100) String city,
		@Size(max = 100) String countyRegion,
		@NotBlank @Size(max = 20) String postalCode,
		@NotBlank @Size(max = 255) String streetAddress,
		@Size(max = 255) String buildingDetails,
		boolean defaultAddress) {
}
