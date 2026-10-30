package com.bookora.service;

import com.bookora.dto.ContactRequest;

public interface ContactService {

	/** Saves the message so the shop owner can read it later. */
	void receive(ContactRequest request);

}
