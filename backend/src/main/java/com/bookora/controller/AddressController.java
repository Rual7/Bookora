package com.bookora.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.bookora.dto.AddressDto;
import com.bookora.dto.AddressRequest;
import com.bookora.service.AddressService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

	private final AddressService addressService;

	public AddressController(AddressService addressService) {
		this.addressService = addressService;
	}

	@GetMapping
	public List<AddressDto> list(Authentication auth) {
		return addressService.list(auth.getName());
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public AddressDto create(Authentication auth, @Valid @RequestBody AddressRequest request) {
		return addressService.create(auth.getName(), request);
	}

	@PutMapping("/{id}")
	public AddressDto update(Authentication auth, @PathVariable Long id, @Valid @RequestBody AddressRequest request) {
		return addressService.update(auth.getName(), id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(Authentication auth, @PathVariable Long id) {
		addressService.delete(auth.getName(), id);
	}

}
