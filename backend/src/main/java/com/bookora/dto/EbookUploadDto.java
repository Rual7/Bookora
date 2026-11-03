package com.bookora.dto;

/** The book after an e-book upload. {@code refused} is the file that failed the check and was not stored, or null. */
public record EbookUploadDto(AdminBookDto book, EbookFileDto refused) {
}
