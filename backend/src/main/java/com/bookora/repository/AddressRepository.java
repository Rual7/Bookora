package com.bookora.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bookora.model.Address;

public interface AddressRepository extends JpaRepository<Address, Long> {

	List<Address> findByUserIdOrderByDefaultAddressDescIdAsc(Long userId);

	Optional<Address> findByIdAndUserId(Long id, Long userId);

}
