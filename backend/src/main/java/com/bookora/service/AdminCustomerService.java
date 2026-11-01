package com.bookora.service;

import com.bookora.dto.AdminCustomerDto;
import com.bookora.dto.PageDto;

public interface AdminCustomerService {

	PageDto<AdminCustomerDto> list(int page, int size);

	/** Turns a customer account on or off. An administrator cannot change an administrator account, their own included. */
	AdminCustomerDto setEnabled(Long id, boolean enabled);

}
