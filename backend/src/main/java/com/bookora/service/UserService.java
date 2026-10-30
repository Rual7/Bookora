package com.bookora.service;

import com.bookora.dto.RegisterRequest;
import com.bookora.dto.UserDto;
import com.bookora.exception.ConflictException;
import com.bookora.exception.ResourceNotFoundException;

public interface UserService {

	/** @throws ConflictException if the email is already registered */
	UserDto register(RegisterRequest request);

	/** @throws ResourceNotFoundException if there is no such user */
	UserDto getByEmail(String email);

}
