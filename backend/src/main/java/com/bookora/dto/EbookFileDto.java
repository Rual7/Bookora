package com.bookora.dto;

import com.bookora.model.ScanStatus;

public record EbookFileDto(String originalFilename, long sizeBytes, ScanStatus scanStatus) {
}
