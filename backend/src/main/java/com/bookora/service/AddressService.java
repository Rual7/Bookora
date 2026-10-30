package com.bookora.service;

import java.util.List;

import com.bookora.dto.AddressDto;
import com.bookora.dto.AddressRequest;

public interface AddressService {

	List<AddressDto> list(String email);

	AddressDto create(String email, AddressRequest request);

	AddressDto update(String email, Long addressId, AddressRequest request);

	void delete(String email, Long addressId);

}
