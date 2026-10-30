package com.bookora.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bookora.dto.CategoryDto;
import com.bookora.repository.CategoryRepository;

@Service
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

	private final CategoryRepository categories;

	public CategoryServiceImpl(CategoryRepository categories) {
		this.categories = categories;
	}

	@Override
	public List<CategoryDto> findAll() {
		return categories.findAllByOrderByIdAsc().stream()
				.map(c -> new CategoryDto(c.getId(), c.getName(), c.getSlug(), c.getDescription()))
				.toList();
	}

}
