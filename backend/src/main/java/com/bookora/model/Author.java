package com.bookora.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "author")
public class Author extends BaseEntity {

	@Column(nullable = false)
	private String name;

	@Column(columnDefinition = "text")
	private String biography;

	protected Author() {
	}

	public Author(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getBiography() {
		return biography;
	}

}
