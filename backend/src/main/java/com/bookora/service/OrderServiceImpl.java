package com.bookora.service;

import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bookora.dto.OrderDto;
import com.bookora.dto.OrderItemDto;
import com.bookora.dto.ShippingAddressDto;
import com.bookora.exception.BadRequestException;
import com.bookora.exception.ConflictException;
import com.bookora.exception.ResourceNotFoundException;
import com.bookora.model.Address;
import com.bookora.model.AppUser;
import com.bookora.model.Cart;
import com.bookora.model.CartItem;
import com.bookora.model.CustomerOrder;
import com.bookora.model.OrderAddress;
import com.bookora.model.Product;
import com.bookora.repository.AddressRepository;
import com.bookora.repository.CartRepository;
import com.bookora.repository.OrderRepository;
import com.bookora.repository.ProductRepository;
import com.bookora.repository.UserRepository;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

	private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

	private final OrderRepository orders;
	private final CartRepository carts;
	private final ProductRepository products;
	private final AddressRepository addresses;
	private final UserRepository users;

	public OrderServiceImpl(OrderRepository orders, CartRepository carts, ProductRepository products,
			AddressRepository addresses, UserRepository users) {
		this.orders = orders;
		this.carts = carts;
		this.products = products;
		this.addresses = addresses;
		this.users = users;
	}

	@Override
	public OrderDto checkout(String email, Long addressId) {
		// Locking the user makes a double click on "Place order" wait, then find an empty cart.
		AppUser user = users.findAndLockByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
		Cart cart = carts.findByUserId(user.getId()).orElse(null);
		if (cart == null || cart.getItems().isEmpty()) {
			throw new BadRequestException("Your cart is empty");
		}

		Map<Long, Product> locked = lockProducts(cart);
		boolean needsShipping = false;
		for (CartItem item : cart.getItems()) {
			Product product = locked.get(item.getProduct().getId());
			checkAvailable(product, item.getQuantity());
			needsShipping |= !product.getFormat().isDigital();
		}

		OrderAddress shippingAddress = needsShipping ? snapshotAddress(user, addressId) : null;
		CustomerOrder order = new CustomerOrder(user, nextOrderNumber(), BookMapper.CURRENCY, shippingAddress);
		for (CartItem item : cart.getItems()) {
			Product product = locked.get(item.getProduct().getId());
			if (!product.getFormat().isDigital()) {
				product.reduceStock(item.getQuantity());
			}
			order.addItem(product, item.getQuantity());
		}
		order.setShippingCost(Shipping.cost(order.getSubtotal(), needsShipping));
		orders.save(order);
		cart.getItems().clear();

		log.info("Order placed: number={}, userId={}, items={}", order.getOrderNumber(), user.getId(),
				order.getItems().size());
		return toDto(order);
	}

	@Override
	@Transactional(readOnly = true)
	public List<OrderDto> list(String email) {
		AppUser user = findUser(email);
		return orders.findByUserIdOrderByCreatedAtDesc(user.getId()).stream().map(OrderServiceImpl::toDto).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public OrderDto get(String email, String orderNumber) {
		AppUser user = findUser(email);
		// Looking up by user as well means another customer's order number is simply "not found".
		return orders.findByOrderNumberAndUserId(orderNumber, user.getId())
				.map(OrderServiceImpl::toDto)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found"));
	}

	private AppUser findUser(String email) {
		return users.findByEmailIgnoreCase(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
	}

	private Map<Long, Product> lockProducts(Cart cart) {
		List<Long> ids = cart.getItems().stream().map(item -> item.getProduct().getId()).toList();
		return products.findAllForUpdate(ids).stream().collect(Collectors.toMap(Product::getId, Function.identity()));
	}

	private void checkAvailable(Product product, int quantity) {
		String title = product.getBook().getTitle();
		if (!product.isActive() || !product.getBook().isActive()) {
			throw new ConflictException("\"" + title + "\" is no longer available");
		}
		if (!product.getFormat().isDigital() && quantity > product.getStockQuantity()) {
			throw new ConflictException("Only " + product.getStockQuantity() + " in stock of \"" + title + "\"");
		}
	}

	private OrderAddress snapshotAddress(AppUser user, Long addressId) {
		if (addressId == null) {
			throw new BadRequestException("Choose a delivery address");
		}
		Address address = addresses.findByIdAndUserId(addressId, user.getId())
				.orElseThrow(() -> new ResourceNotFoundException("Address not found"));
		return new OrderAddress(address);
	}

	private String nextOrderNumber() {
		return "BK-%d-%06d".formatted(Year.now().getValue(), orders.nextOrderSequence());
	}

	private static OrderDto toDto(CustomerOrder order) {
		List<OrderItemDto> items = order.getItems().stream()
				.map(i -> new OrderItemDto(i.getProductTitle(), i.getProductFormat(), i.getQuantity(), i.getUnitPrice(),
						i.getLineTotal()))
				.toList();
		int itemCount = items.stream().mapToInt(OrderItemDto::quantity).sum();
		return new OrderDto(order.getOrderNumber(), order.getStatus(), order.getCreatedAt(), order.getPaidAt(), items,
				itemCount, order.getShippingCost(), order.getTotal(), order.getCurrency(),
				toDto(order.getShippingAddress()));
	}

	private static ShippingAddressDto toDto(OrderAddress a) {
		if (a == null) {
			return null;
		}
		return new ShippingAddressDto(a.getRecipientName(), a.getPhone(), a.getCountry(), a.getCity(),
				a.getCountyRegion(), a.getPostalCode(), a.getStreetAddress(), a.getBuildingDetails());
	}

}
