package com.bookora.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bookora.model.EbookFile;

public interface EbookFileRepository extends JpaRepository<EbookFile, Long> {

	Optional<EbookFile> findByProductId(Long productId);

	List<EbookFile> findByProductIdIn(Collection<Long> productIds);

}
