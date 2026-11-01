package com.bookora.service;

import com.bookora.dto.AdminMessageDto;
import com.bookora.dto.PageDto;

public interface AdminMessageService {

	PageDto<AdminMessageDto> list(int page, int size);

	AdminMessageDto setHandled(Long id, boolean handled);

}
