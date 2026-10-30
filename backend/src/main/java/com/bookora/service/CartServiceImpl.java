package com.bookora.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bookora.dto.CartDto;
import com.bookora.dto.CartItemDto;
import com.bookora.dto.CartItemRequest;
import com.bookora.exception.BadRequestException;
import com.bookora.exception.ConflictException;
import com.bookora.exception.ResourceNotFoundException;
import com.bookora.model.AppUser;
import com.bookora.model.Book;
import com.bookora.model.Cart;
import com.bookora.model.CartItem;
import com.bookora.model.Product;
import com.bookora.repository.CartRepository;
import com.bookora.repository.ProductRepository;
import com.bookora.repository.UserRepository;

@Service
@Transactional
public class CartServiceImpl implements CartService {

	static final int MAX_PER_ITEM = 10;

	private final CartRepository carts;
	private final ProductRepository products;
	private final UserRepository users;

	public CartServiceImpl(CartRepository carts, ProductRepository products, UserRepository users) {
		this.carts = carts;
		this.products = products;
		this.users = users;
	}

	/** A product and how many of it are in a cart; the common shape of saved carts and previews. */
	private record Line(Product product, int quantity) {
	}

	@Override
	public CartDto getCart(String email) {
		return toDto(linesOf(findOrCreateCart(email)));
	}

	@Override
	public CartDto addItem(String email, Long productId, int quantity) {
		Cart cart = findOrCreateCart(email);
		Product product = findBuyableProduct(productId);
		CartItem existing = cart.findItem(productId);

		// An e-book is bought once, so adding it again changes nothing.
		int newQuantity = product.getFormat().isDigital() ? 1 : quantity + (existing == null ? 0 : existing.getQuantity());
		checkQuantity(product, newQuantity);

		if (existing == null) {
			cart.addItem(product, newQuantity);
		} else {
			existing.setQuantity(newQuantity);
		}
		return toDto(linesOf(cart));
	}

	@Override
	public CartDto setQuantity(String email, Long productId, int quantity) {
		Cart cart = findOrCreateCart(email);
		CartItem item = cart.findItem(productId);
		if (item == null) {
			throw new ResourceNotFoundException("This product is not in your cart");
		}
		checkQuantity(item.getProduct(), quantity);
		item.setQuantity(quantity);
		return toDto(linesOf(cart));
	}

	@Override
	public CartDto removeItem(String email, Long productId) {
		Cart cart = findOrCreateCart(email);
		cart.removeItem(productId);
		return toDto(linesOf(cart));
	}

	/** Prices a cart that lives in the visitor's browser. Nothing is saved; unavailable items are left out. */
	@Override
	@Transactional(readOnly = true)
	public CartDto preview(List<CartItemRequest> requested) {
		Map<Long, Integer> quantities = new LinkedHashMap<>();
		for (CartItemRequest item : requested) {
			quantities.merge(item.productId(), item.quantity(), Integer::sum);
		}

		List<Line> lines = new ArrayList<>();
		quantities.forEach((productId, quantity) -> products.findById(productId)
				.filter(this::isBuyable)
				.ifPresent(product -> {
					int allowed = Math.min(quantity, maxQuantity(product));
					if (allowed > 0) {
						lines.add(new Line(product, allowed));
					}
				}));
		return toDto(lines);
	}

	private Cart findOrCreateCart(String email) {
		// Locking the user makes simultaneous requests wait, so a first cart is never created twice.
		AppUser user = users.findAndLockByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
		return carts.findByUserId(user.getId()).orElseGet(() -> carts.save(new Cart(user)));
	}

	private Product findBuyableProduct(Long productId) {
		return products.findById(productId)
				.filter(this::isBuyable)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));
	}

	private boolean isBuyable(Product product) {
		return product.isActive() && product.getBook().isActive();
	}

	private void checkQuantity(Product product, int quantity) {
		if (quantity < 1) {
			throw new BadRequestException("Quantity must be at least 1");
		}
		if (product.getFormat().isDigital() && quantity > 1) {
			throw new BadRequestException("An e-book can only be bought once");
		}
		if (quantity > MAX_PER_ITEM) {
			throw new BadRequestException("You can order at most " + MAX_PER_ITEM + " copies of a book");
		}
		if (!product.getFormat().isDigital() && quantity > product.getStockQuantity()) {
			throw new ConflictException("Only " + product.getStockQuantity() + " in stock");
		}
	}

	private int maxQuantity(Product product) {
		return product.getFormat().isDigital() ? 1 : Math.min(product.getStockQuantity(), MAX_PER_ITEM);
	}

	private List<Line> linesOf(Cart cart) {
		return cart.getItems().stream().map(item -> new Line(item.getProduct(), item.getQuantity())).toList();
	}

	private CartDto toDto(List<Line> lines) {
		List<CartItemDto> items = lines.stream().map(this::toItemDto).toList();
		int itemCount = items.stream().mapToInt(CartItemDto::quantity).sum();
		BigDecimal subtotal = items.stream().map(CartItemDto::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
		boolean hasPrintedItems = items.stream().anyMatch(item -> !item.format().isDigital());
		BigDecimal shippingCost = Shipping.cost(subtotal, hasPrintedItems);
		return new CartDto(items, itemCount, subtotal, shippingCost, subtotal.add(shippingCost), BookMapper.CURRENCY);
	}

	private CartItemDto toItemDto(Line line) {
		Product product = line.product();
		Book book = product.getBook();
		BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(line.quantity()));
		return new CartItemDto(product.getId(), book.getId(), book.getSlug(), book.getTitle(),
				book.getAuthors().stream().map(a -> a.getName()).toList(), book.getCoverImagePath(),
				product.getFormat(), product.getPrice(), line.quantity(), lineTotal, maxQuantity(product));
	}

}
