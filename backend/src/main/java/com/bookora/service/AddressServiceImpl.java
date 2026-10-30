package com.bookora.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bookora.dto.AddressDto;
import com.bookora.dto.AddressRequest;
import com.bookora.exception.ResourceNotFoundException;
import com.bookora.model.Address;
import com.bookora.model.AppUser;
import com.bookora.repository.AddressRepository;
import com.bookora.repository.UserRepository;

@Service
@Transactional
public class AddressServiceImpl implements AddressService {

	private final AddressRepository addresses;
	private final UserRepository users;

	public AddressServiceImpl(AddressRepository addresses, UserRepository users) {
		this.addresses = addresses;
		this.users = users;
	}

	@Override
	@Transactional(readOnly = true)
	public List<AddressDto> list(String email) {
		return addresses.findByUserIdOrderByDefaultAddressDescIdAsc(findUser(email).getId()).stream()
				.map(AddressServiceImpl::toDto)
				.toList();
	}

	@Override
	public AddressDto create(String email, AddressRequest request) {
		AppUser user = findUser(email);
		List<Address> saved = addresses.findByUserIdOrderByDefaultAddressDescIdAsc(user.getId());

		Address address = new Address(user);
		copyFields(address, request);
		// The first address a customer saves is the default one.
		if (request.defaultAddress() || saved.isEmpty()) {
			makeDefault(address, saved);
		}
		return toDto(addresses.save(address));
	}

	@Override
	public AddressDto update(String email, Long addressId, AddressRequest request) {
		AppUser user = findUser(email);
		Address address = findOwned(user, addressId);

		copyFields(address, request);
		if (request.defaultAddress()) {
			makeDefault(address, addresses.findByUserIdOrderByDefaultAddressDescIdAsc(user.getId()));
		}
		return toDto(address);
	}

	@Override
	public void delete(String email, Long addressId) {
		AppUser user = findUser(email);
		Address address = findOwned(user, addressId);
		addresses.delete(address);

		if (address.isDefaultAddress()) {
			addresses.findByUserIdOrderByDefaultAddressDescIdAsc(user.getId()).stream()
					.filter(other -> !other.getId().equals(addressId))
					.findFirst()
					.ifPresent(other -> other.setDefaultAddress(true));
		}
	}

	private AppUser findUser(String email) {
		return users.findByEmailIgnoreCase(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
	}

	private Address findOwned(AppUser user, Long addressId) {
		return addresses.findByIdAndUserId(addressId, user.getId())
				.orElseThrow(() -> new ResourceNotFoundException("Address not found"));
	}

	private void makeDefault(Address address, List<Address> all) {
		all.forEach(other -> other.setDefaultAddress(false));
		address.setDefaultAddress(true);
	}

	private void copyFields(Address address, AddressRequest request) {
		address.setLabel(blankToNull(request.label()));
		address.setRecipientName(request.recipientName().trim());
		address.setPhone(request.phone().trim());
		address.setCountry(request.country().trim());
		address.setCity(request.city().trim());
		address.setCountyRegion(blankToNull(request.countyRegion()));
		address.setPostalCode(request.postalCode().trim());
		address.setStreetAddress(request.streetAddress().trim());
		address.setBuildingDetails(blankToNull(request.buildingDetails()));
	}

	private static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	private static AddressDto toDto(Address a) {
		return new AddressDto(a.getId(), a.getLabel(), a.getRecipientName(), a.getPhone(), a.getCountry(), a.getCity(),
				a.getCountyRegion(), a.getPostalCode(), a.getStreetAddress(), a.getBuildingDetails(),
				a.isDefaultAddress());
	}

}
