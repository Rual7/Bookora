package com.bookora.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bookora.dto.AdminOrderDto;
import com.bookora.dto.AdminOrderRowDto;
import com.bookora.dto.OrderDto;
import com.bookora.dto.PageDto;
import com.bookora.exception.ConflictException;
import com.bookora.exception.ResourceNotFoundException;
import com.bookora.model.AppUser;
import com.bookora.model.CustomerOrder;
import com.bookora.model.OrderItem;
import com.bookora.model.OrderStatus;
import com.bookora.repository.OrderRepository;

@Service
public class AdminOrderServiceImpl implements AdminOrderService {

	private final OrderRepository orders;

	public AdminOrderServiceImpl(OrderRepository orders) {
		this.orders = orders;
	}

	@Override
	@Transactional(readOnly = true)
	public PageDto<AdminOrderRowDto> list(OrderStatus status, int page, int size) {
		PageRequest paging = PageRequest.of(page, size);
		Page<CustomerOrder> found = status == null
				? orders.findAllByOrderByCreatedAtDesc(paging)
				: orders.findByStatusOrderByCreatedAtDesc(status, paging);
		return PageDto.of(found.map(AdminOrderServiceImpl::toRow));
	}

	@Override
	@Transactional(readOnly = true)
	public AdminOrderDto get(String orderNumber) {
		return toDto(find(orderNumber));
	}

	@Override
	@Transactional
	public AdminOrderDto changeStatus(String orderNumber, OrderStatus next) {
		CustomerOrder order = find(orderNumber);
		boolean hasShipping = order.getShippingAddress() != null;
		switch (order.getStatus()) {
			case PAID -> {
				if (next == OrderStatus.SHIPPED && hasShipping) {
					order.markShipped();
				} else if (next == OrderStatus.DELIVERED && !hasShipping) {
					order.markDelivered();
				} else {
					throw refused(order, next);
				}
			}
			case SHIPPED -> {
				if (next != OrderStatus.DELIVERED) {
					throw refused(order, next);
				}
				order.markDelivered();
			}
			default -> throw refused(order, next);
		}
		return toDto(order);
	}

	private CustomerOrder find(String orderNumber) {
		return orders.findByOrderNumber(orderNumber).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
	}

	private static ConflictException refused(CustomerOrder order, OrderStatus next) {
		return new ConflictException("An order that is " + order.getStatus() + " cannot be set to " + next);
	}

	private static AdminOrderRowDto toRow(CustomerOrder order) {
		AppUser customer = order.getUser();
		int itemCount = order.getItems().stream().mapToInt(OrderItem::getQuantity).sum();
		return new AdminOrderRowDto(order.getOrderNumber(), customer.getFullName(), customer.getEmail(),
				order.getStatus(), order.getCreatedAt(), itemCount, order.getTotal(), order.getCurrency());
	}

	private static AdminOrderDto toDto(CustomerOrder order) {
		OrderDto dto = OrderServiceImpl.toDto(order);
		return new AdminOrderDto(dto, order.getUser().getFullName(), order.getUser().getEmail());
	}

}
