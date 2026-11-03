package com.bookora.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bookora.dto.AdminCustomerDto;
import com.bookora.dto.PageDto;
import com.bookora.exception.BadRequestException;
import com.bookora.exception.ResourceNotFoundException;
import com.bookora.model.AppUser;
import com.bookora.model.Role;
import com.bookora.repository.OrderCount;
import com.bookora.repository.OrderRepository;
import com.bookora.repository.UserRepository;

@Service
public class AdminCustomerServiceImpl implements AdminCustomerService {

	private final UserRepository users;
	private final OrderRepository orders;

	public AdminCustomerServiceImpl(UserRepository users, OrderRepository orders) {
		this.users = users;
		this.orders = orders;
	}

	@Override
	@Transactional(readOnly = true)
	public PageDto<AdminCustomerDto> list(int page, int size) {
		var found = users.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
		Map<Long, Long> orderCounts = countOrders(found.getContent());
		return PageDto.of(found.map(user -> toDto(user, orderCounts.getOrDefault(user.getId(), 0L))));
	}

	@Override
	@Transactional
	public AdminCustomerDto setEnabled(Long id, boolean enabled) {
		AppUser user = users.findById(id).orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
		if (user.getRole() == Role.ADMIN) {
			throw new BadRequestException("Administrator accounts cannot be changed here");
		}
		user.setEnabled(enabled);
		return toDto(user, countOrders(List.of(user)).getOrDefault(id, 0L));
	}

	private Map<Long, Long> countOrders(List<AppUser> customers) {
		if (customers.isEmpty()) {
			return Map.of();
		}
		return orders.countByUsers(customers.stream().map(AppUser::getId).toList()).stream()
				.collect(Collectors.toMap(OrderCount::userId, OrderCount::count));
	}

	private static AdminCustomerDto toDto(AppUser user, long orderCount) {
		return new AdminCustomerDto(user.getId(), user.getFullName(), user.getEmail(), user.getRole(), user.isEnabled(),
				(int) orderCount, user.getCreatedAt());
	}

}
