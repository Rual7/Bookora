package com.bookora.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bookora.model.EbookDownload;

public interface EbookDownloadRepository extends JpaRepository<EbookDownload, Long> {
}
