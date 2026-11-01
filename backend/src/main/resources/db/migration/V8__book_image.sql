CREATE TABLE book_image (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    book_id    BIGINT       NOT NULL REFERENCES book (id) ON DELETE CASCADE,
    path       VARCHAR(500) NOT NULL,
    position   INTEGER      NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX ix_book_image_book ON book_image (book_id, position);

-- A cover that was already set becomes the first image of its book.
INSERT INTO book_image (book_id, path, position)
SELECT id, cover_image_path, 0 FROM book WHERE cover_image_path IS NOT NULL;
