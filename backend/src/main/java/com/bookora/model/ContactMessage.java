package com.bookora.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "contact_message")
public class ContactMessage extends BaseEntity {

	private String name;

	private String email;

	private String message;

	protected ContactMessage() {
	}

	public ContactMessage(String name, String email, String message) {
		this.name = name;
		this.email = email;
		this.message = message;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public String getMessage() {
		return message;
	}

}
