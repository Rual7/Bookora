package com.bookora.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bookora.dto.OrderDto;
import com.bookora.exception.BadRequestException;
import com.bookora.exception.ConflictException;
import com.bookora.model.Address;
import com.bookora.model.AppUser;
import com.bookora.model.Book;
import com.bookora.model.Cart;
import com.bookora.model.CustomerOrder;
import com.bookora.model.Product;
import com.bookora.model.ProductFormat;
import com.bookora.repository.AddressRepository;
import com.bookora.repository.CartRepository;
import com.bookora.repository.OrderRepository;
import com.bookora.repository.ProductRepository;
import com.bookora.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

	private static final String EMAIL = "ana@example.com";

	@Mock
	private OrderRepository orders;
	@Mock
	private CartRepository carts;
	@Mock
	private ProductRepository products;
	@Mock
	private AddressRepository addresses;
	@Mock
	private UserRepository users;

	private OrderServiceImpl service;
	private AppUser user;
	private Cart cart;

	@BeforeEach
	void setUp() {
		service = new OrderServiceImpl(orders, carts, products, addresses, users);
		user = mock(AppUser.class);
		cart = new Cart(user);
		lenient().when(user.getId()).thenReturn(1L);
		lenient().when(users.findAndLockByEmail(EMAIL)).thenReturn(Optional.of(user));
		lenient().when(carts.findByUserId(1L)).thenReturn(Optional.of(cart));
		lenient().when(orders.save(any(CustomerOrder.class))).thenAnswer(call -> call.getArgument(0));
	}

	private Product product(long id, ProductFormat format, String price, int stock) {
		Product product = mock(Product.class);
		Book book = mock(Book.class);
		lenient().when(book.getTitle()).thenReturn("Dracula");
		lenient().when(book.isActive()).thenReturn(true);
		lenient().when(product.getId()).thenReturn(id);
		lenient().when(product.getBook()).thenReturn(book);
		lenient().when(product.getFormat()).thenReturn(format);
		lenient().when(product.getPrice()).thenReturn(new BigDecimal(price));
		lenient().when(product.getStockQuantity()).thenReturn(stock);
		lenient().when(product.isActive()).thenReturn(true);
		lenient().when(products.findAllForUpdate(any())).thenReturn(List.of(product));
		return product;
	}

	@Test
	void emptyCartIsRejected() {
		assertThatThrownBy(() -> service.checkout(EMAIL, null)).isInstanceOf(BadRequestException.class);
		verify(orders, never()).save(any());
	}

	@Test
	void notEnoughStockIsRejectedAndNothingIsSaved() {
		Product printed = product(5L, ProductFormat.PAPERBACK, "40.00", 1);
		cart.addItem(printed, 2);

		assertThatThrownBy(() -> service.checkout(EMAIL, 1L)).isInstanceOf(ConflictException.class);
		verify(printed, never()).reduceStock(any(Integer.class));
		verify(orders, never()).save(any());
	}

	@Test
	void printedBooksNeedADeliveryAddress() {
		Product printed = product(5L, ProductFormat.PAPERBACK, "40.00", 5);
		cart.addItem(printed, 1);

		assertThatThrownBy(() -> service.checkout(EMAIL, null)).isInstanceOf(BadRequestException.class);
		verify(orders, never()).save(any());
	}

	@Test
	void anotherCustomersAddressIsNotUsable() {
		Product printed = product(5L, ProductFormat.PAPERBACK, "40.00", 5);
		cart.addItem(printed, 1);
		when(addresses.findByIdAndUserId(9L, 1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.checkout(EMAIL, 9L))
				.isInstanceOf(com.bookora.exception.ResourceNotFoundException.class);
	}

	@Test
	void ebookOrderNeedsNoAddressAndLeavesStockAlone() {
		Product ebook = product(7L, ProductFormat.EPUB, "19.50", 0);
		cart.addItem(ebook, 1);

		OrderDto order = service.checkout(EMAIL, null);

		assertThat(order.shippingAddress()).isNull();
		assertThat(order.total()).isEqualByComparingTo("19.50");
		assertThat(cart.getItems()).isEmpty();
		verify(ebook, never()).reduceStock(any(Integer.class));
	}

	@Test
	void deliveryIsFreeWhenTheOrderIsAbove40() {
		Product printed = product(5L, ProductFormat.HARDCOVER, "20.50", 5);
		cart.addItem(printed, 2);
		when(addresses.findByIdAndUserId(3L, 1L)).thenReturn(Optional.of(new Address(user)));

		OrderDto order = service.checkout(EMAIL, 3L);

		assertThat(order.shippingCost()).isEqualByComparingTo("0");
		assertThat(order.total()).isEqualByComparingTo("41.00");
	}

	@Test
	void deliveryStaysPaidAtExactly40() {
		Product printed = product(5L, ProductFormat.HARDCOVER, "20.00", 5);
		cart.addItem(printed, 2);
		when(addresses.findByIdAndUserId(3L, 1L)).thenReturn(Optional.of(new Address(user)));

		assertThat(service.checkout(EMAIL, 3L).shippingCost()).isEqualByComparingTo("2.50");
	}

	@Test
	void priceAndStockComeFromTheProductAndTheCartIsEmptied() {
		Product printed = product(5L, ProductFormat.HARDCOVER, "12.50", 5);
		cart.addItem(printed, 2);
		Address address = new Address(user);
		address.setRecipientName("Ana Pop");
		when(addresses.findByIdAndUserId(3L, 1L)).thenReturn(Optional.of(address));
		when(orders.nextOrderSequence()).thenReturn(1234L);

		OrderDto order = service.checkout(EMAIL, 3L);

		assertThat(order.orderNumber()).endsWith("-001234");
		assertThat(order.shippingCost()).isEqualByComparingTo("2.50");
		assertThat(order.total()).isEqualByComparingTo("27.50");
		assertThat(order.itemCount()).isEqualTo(2);
		assertThat(order.shippingAddress().recipientName()).isEqualTo("Ana Pop");
		assertThat(cart.getItems()).isEmpty();
		verify(printed).reduceStock(2);

		ArgumentCaptor<CustomerOrder> saved = ArgumentCaptor.forClass(CustomerOrder.class);
		verify(orders).save(saved.capture());
		assertThat(saved.getValue().getItems().get(0).getUnitPrice()).isEqualByComparingTo("12.50");
	}

}
