package com.bookora.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bookora.model.WishlistItem;
import com.bookora.model.WishlistItemId;

public interface WishlistRepository extends JpaRepository<WishlistItem, WishlistItemId> {

	List<WishlistItem> findByIdUserId(Long userId);

}
