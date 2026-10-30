-- The shop now sells in EUR. The demo prices were written for RON, so they are reset to EUR prices ending in .90.
-- Orders placed before this keep the currency and amounts they were made with.
UPDATE product SET price = ROUND(price * 0.25) - 0.10;

ALTER TABLE customer_order ALTER COLUMN currency SET DEFAULT 'EUR';
