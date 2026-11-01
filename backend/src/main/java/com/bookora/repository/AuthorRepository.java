package com.bookora.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bookora.model.Author;

public interface AuthorRepository extends JpaRepository<Author, Long> {

	Optional<Author> findFirstByNameIgnoreCase(String name);

}
