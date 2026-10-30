-- Bookora schema (3NF), derived from the ERD in docs/database.

CREATE TABLE app_user (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'CUSTOMER' CHECK (role IN ('CUSTOMER', 'ADMIN')),
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE address (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id          BIGINT       NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    label            VARCHAR(50),
    recipient_name   VARCHAR(200) NOT NULL,
    phone            VARCHAR(30)  NOT NULL,
    country          VARCHAR(100) NOT NULL,
    city             VARCHAR(100) NOT NULL,
    county_region    VARCHAR(100),
    postal_code      VARCHAR(20)  NOT NULL,
    street_address   VARCHAR(255) NOT NULL,
    building_details VARCHAR(255),
    is_default       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_address_user ON address (user_id);

CREATE TABLE author (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name       VARCHAR(200) NOT NULL,
    biography  TEXT,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE category (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    slug        VARCHAR(120) NOT NULL UNIQUE,
    description TEXT,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE book (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title            VARCHAR(255) NOT NULL,
    slug             VARCHAR(280) NOT NULL UNIQUE,
    description      TEXT,
    cover_image_path VARCHAR(500),
    publisher        VARCHAR(200),
    published_on     DATE,
    language_code    VARCHAR(10),
    page_count       INTEGER CHECK (page_count > 0),
    featured         BOOLEAN      NOT NULL DEFAULT FALSE,
    active           BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE book_author (
    book_id       BIGINT   NOT NULL REFERENCES book (id) ON DELETE CASCADE,
    author_id     BIGINT   NOT NULL REFERENCES author (id),
    display_order SMALLINT NOT NULL DEFAULT 0,
    PRIMARY KEY (book_id, author_id)
);

CREATE TABLE book_category (
    book_id     BIGINT NOT NULL REFERENCES book (id) ON DELETE CASCADE,
    category_id BIGINT NOT NULL REFERENCES category (id),
    PRIMARY KEY (book_id, category_id)
);

CREATE TABLE product (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    book_id        BIGINT         NOT NULL REFERENCES book (id),
    sku            VARCHAR(50)    NOT NULL UNIQUE,
    format         VARCHAR(20)    NOT NULL CHECK (format IN ('PAPERBACK', 'HARDCOVER', 'PDF', 'EPUB')),
    isbn           VARCHAR(20) UNIQUE,
    price          NUMERIC(10, 2) NOT NULL CHECK (price >= 0),
    stock_quantity INTEGER        NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    active         BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT ck_ebook_no_stock CHECK (format IN ('PAPERBACK', 'HARDCOVER') OR stock_quantity = 0),
    CONSTRAINT uq_product_book_format UNIQUE (book_id, format)
);
CREATE INDEX idx_product_book ON product (book_id);

CREATE TABLE ebook_file (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    product_id        BIGINT       NOT NULL UNIQUE REFERENCES product (id),
    storage_key       VARCHAR(500) NOT NULL UNIQUE,
    original_filename VARCHAR(255) NOT NULL,
    mime_type         VARCHAR(100) NOT NULL,
    file_size_bytes   BIGINT       NOT NULL CHECK (file_size_bytes > 0),
    sha256_hash       CHAR(64)     NOT NULL,
    scan_status       VARCHAR(20)  NOT NULL DEFAULT 'PENDING' CHECK (scan_status IN ('PENDING', 'CLEAN', 'REJECTED')),
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE review (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id    BIGINT      NOT NULL REFERENCES app_user (id),
    book_id    BIGINT      NOT NULL REFERENCES book (id),
    rating     SMALLINT    NOT NULL CHECK (rating BETWEEN 1 AND 5),
    title      VARCHAR(150),
    comment    TEXT,
    status     VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_review_user_book UNIQUE (user_id, book_id)
);
CREATE INDEX idx_review_book ON review (book_id);

CREATE TABLE wishlist_item (
    user_id    BIGINT      NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    book_id    BIGINT      NOT NULL REFERENCES book (id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, book_id)
);

CREATE TABLE cart (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id    BIGINT      NOT NULL UNIQUE REFERENCES app_user (id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE cart_item (
    cart_id    BIGINT      NOT NULL REFERENCES cart (id) ON DELETE CASCADE,
    product_id BIGINT      NOT NULL REFERENCES product (id),
    quantity   INTEGER     NOT NULL CHECK (quantity > 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (cart_id, product_id)
);

-- Snapshot of the delivery address at order time.
CREATE TABLE order_address (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    recipient_name   VARCHAR(200) NOT NULL,
    phone            VARCHAR(30)  NOT NULL,
    country          VARCHAR(100) NOT NULL,
    city             VARCHAR(100) NOT NULL,
    county_region    VARCHAR(100),
    postal_code      VARCHAR(20)  NOT NULL,
    street_address   VARCHAR(255) NOT NULL,
    building_details VARCHAR(255),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE customer_order (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id             BIGINT         NOT NULL REFERENCES app_user (id),
    order_number        VARCHAR(50)    NOT NULL UNIQUE,
    status              VARCHAR(30)    NOT NULL DEFAULT 'PENDING_PAYMENT'
        CHECK (status IN ('PENDING_PAYMENT', 'PAID', 'SHIPPED', 'DELIVERED', 'CANCELLED')),
    shipping_address_id BIGINT UNIQUE REFERENCES order_address (id),
    shipping_cost       NUMERIC(10, 2) NOT NULL DEFAULT 0 CHECK (shipping_cost >= 0),
    currency            CHAR(3)        NOT NULL DEFAULT 'RON',
    created_at          TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ    NOT NULL DEFAULT now(),
    paid_at             TIMESTAMPTZ
);
CREATE INDEX idx_order_user ON customer_order (user_id);

CREATE TABLE order_item (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id       BIGINT         NOT NULL REFERENCES customer_order (id) ON DELETE CASCADE,
    product_id     BIGINT         NOT NULL REFERENCES product (id),
    product_title  VARCHAR(255)   NOT NULL,
    product_format VARCHAR(20)    NOT NULL,
    quantity       INTEGER        NOT NULL CHECK (quantity > 0),
    unit_price     NUMERIC(10, 2) NOT NULL CHECK (unit_price >= 0)
);
CREATE INDEX idx_order_item_order ON order_item (order_id);

CREATE TABLE payment (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id            BIGINT         NOT NULL REFERENCES customer_order (id),
    provider            VARCHAR(30)    NOT NULL,
    provider_session_id VARCHAR(255)   NOT NULL UNIQUE,
    provider_payment_id VARCHAR(255),
    amount              NUMERIC(10, 2) NOT NULL CHECK (amount >= 0),
    currency            CHAR(3)        NOT NULL,
    status              VARCHAR(30)    NOT NULL,
    expires_at          TIMESTAMPTZ,
    created_at          TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ    NOT NULL DEFAULT now(),
    paid_at             TIMESTAMPTZ
);
CREATE INDEX idx_payment_order ON payment (order_id);
-- One successful payment per order.
CREATE UNIQUE INDEX uq_payment_one_success ON payment (order_id) WHERE status = 'SUCCEEDED';

CREATE TABLE ebook_download (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_item_id BIGINT      NOT NULL REFERENCES order_item (id),
    downloaded_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE processed_webhook_event (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    provider          VARCHAR(30)  NOT NULL,
    provider_event_id VARCHAR(255) NOT NULL,
    event_type        VARCHAR(100) NOT NULL,
    processed_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_webhook_event UNIQUE (provider, provider_event_id)
);

-- Totals and ratings are computed, never stored.
CREATE VIEW order_total_view AS
SELECT o.id AS order_id,
       COALESCE(SUM(oi.quantity * oi.unit_price), 0) + o.shipping_cost AS total
FROM customer_order o
         LEFT JOIN order_item oi ON oi.order_id = o.id
GROUP BY o.id, o.shipping_cost;

CREATE VIEW book_rating_view AS
SELECT book_id, ROUND(AVG(rating), 2) AS average_rating, COUNT(*) AS review_count
FROM review
WHERE status = 'APPROVED'
GROUP BY book_id;
