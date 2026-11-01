package com.bookora.service;

import com.bookora.dto.AdminOrderDto;
import com.bookora.dto.AdminOrderRowDto;
import com.bookora.dto.PageDto;
import com.bookora.model.OrderStatus;

public interface AdminOrderService {

	/** All orders, newest first; {@code status} may be null for every status. */
	PageDto<AdminOrderRowDto> list(OrderStatus status, int page, int size);

	AdminOrderDto get(String orderNumber);

	/** Moves a paid order forward: PAID to SHIPPED to DELIVERED. Other changes are refused. */
	AdminOrderDto changeStatus(String orderNumber, OrderStatus next);

}
