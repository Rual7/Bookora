package com.bookora.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bookora.exception.NotImplementedYetException;

/**
 * Reserves the routes of features that are not built yet, so the security rules and the
 * frontend can already target them. Each answers 501 until its real controller replaces it.
 */
@RestController
public class PlannedFeaturesController {

	@GetMapping("/api/admin/books")
	public Object adminBooks() {
		throw new NotImplementedYetException("Book administration");
	}

}
