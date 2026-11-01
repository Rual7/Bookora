package com.bookora.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "contact_message")
public class ContactMessage extends BaseEntity {

	private String name;

	private String email;

	private String message;

	@Column(nullable = false)
	private boolean handled;

	protected ContactMessage() {
	}

	public ContactMessage(String name, String email, String message) {
		this.name = name;
		this.email = email;
		this.message = message;
	}

	public void setHandled(boolean handled) {
		this.handled = handled;
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

	public boolean isHandled() {
		return handled;
	}

}
