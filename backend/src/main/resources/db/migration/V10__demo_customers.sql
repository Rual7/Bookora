-- Demo customers and what they did in the shop: addresses, orders in every status, payments, e-book downloads,
-- reviews, wishlists, carts and contact messages. All of it is invented for the demo.
-- Every demo account uses the password written in the README; the emails use example.com, which can never receive mail.
-- Prices, titles and totals are read from the catalog, so they always match the products.

INSERT INTO app_user (email, password_hash, first_name, last_name, enabled, created_at, updated_at)
SELECT m.email, '$2a$10$p/4UGNJNLJh9ooRmvYlzK.sK3Kmg6vECMb1FjIxblGvEDHRvgqFUe', m.first_name, m.last_name, m.enabled,
       now() - make_interval(days => m.days_ago), now() - make_interval(days => m.days_ago)
FROM (VALUES
    ('ana.popescu@example.com', 'Ana', 'Popescu', TRUE, 300),
    ('mihai.ionescu@example.com', 'Mihai', 'Ionescu', TRUE, 290),
    ('elena.dumitrescu@example.com', 'Elena', 'Dumitrescu', TRUE, 270),
    ('andrei.stan@example.com', 'Andrei', 'Stan', TRUE, 260),
    ('ioana.georgescu@example.com', 'Ioana', 'Georgescu', TRUE, 240),
    ('vlad.tudor@example.com', 'Vlad', 'Tudor', TRUE, 230),
    ('radu.constantin@example.com', 'Radu', 'Constantin', TRUE, 200),
    ('maria.stoica@example.com', 'Maria', 'Stoica', TRUE, 190),
    ('alexandru.marin@example.com', 'Alexandru', 'Marin', TRUE, 170),
    ('cristina.rusu@example.com', 'Cristina', 'Rusu', TRUE, 160),
    ('paul.neagu@example.com', 'Paul', 'Neagu', FALSE, 120),
    ('diana.matei@example.com', 'Diana', 'Matei', TRUE, 9)
) AS m (email, first_name, last_name, enabled, days_ago);

-- Vlad only buys e-books, so he has no address. Diana has just joined and has not ordered yet.
INSERT INTO address (user_id, label, recipient_name, phone, country, city, county_region, postal_code, street_address,
                     building_details, is_default)
SELECT u.id, m.label, m.recipient, m.phone, 'Romania', m.city, m.county, m.postal_code, m.street, m.building, m.is_default
FROM (VALUES
    ('ana.popescu@example.com', 'Home', 'Ana Popescu', '0700 100 001', 'Bucharest', 'Bucharest', '030167',
     'Strada Lipscani 24', 'Bl. 3, Sc. A, Ap. 12', TRUE),
    ('ana.popescu@example.com', 'Office', 'Ana Popescu', '0700 100 001', 'Bucharest', 'Bucharest', '010374',
     'Bulevardul Magheru 32', 'Floor 4', FALSE),
    ('mihai.ionescu@example.com', 'Home', 'Mihai Ionescu', '0700 100 002', 'Cluj-Napoca', 'Cluj', '400114',
     'Strada Memorandumului 8', NULL, TRUE),
    ('elena.dumitrescu@example.com', 'Home', 'Elena Dumitrescu', '0700 100 003', 'Iasi', 'Iasi', '700259',
     'Bulevardul Stefan cel Mare 15', 'Ap. 5', TRUE),
    ('andrei.stan@example.com', 'Home', 'Andrei Stan', '0700 100 004', 'Timisoara', 'Timis', '300085',
     'Strada Alba Iulia 2', NULL, TRUE),
    ('ioana.georgescu@example.com', 'Home', 'Ioana Georgescu', '0700 100 005', 'Brasov', 'Brasov', '500030',
     'Strada Republicii 41', 'Ap. 2', TRUE),
    ('ioana.georgescu@example.com', 'Parents', 'Mihaela Georgescu', '0700 100 015', 'Fagaras', 'Brasov', '505200',
     'Strada Tudor Vladimirescu 7', NULL, FALSE),
    ('radu.constantin@example.com', 'Home', 'Radu Constantin', '0700 100 007', 'Constanta', 'Constanta', '900178',
     'Bulevardul Tomis 120', 'Bl. T4, Ap. 31', TRUE),
    ('maria.stoica@example.com', 'Home', 'Maria Stoica', '0700 100 008', 'Sibiu', 'Sibiu', '550163',
     'Strada Nicolae Balcescu 19', NULL, TRUE),
    ('alexandru.marin@example.com', 'Home', 'Alexandru Marin', '0700 100 009', 'Craiova', 'Dolj', '200585',
     'Calea Unirii 54', 'Ap. 9', TRUE),
    ('cristina.rusu@example.com', 'Home', 'Cristina Rusu', '0700 100 010', 'Oradea', 'Bihor', '410067',
     'Strada Republicii 12', NULL, TRUE),
    ('paul.neagu@example.com', 'Home', 'Paul Neagu', '0700 100 011', 'Ploiesti', 'Prahova', '100066',
     'Strada Gheorghe Doja 3', NULL, TRUE),
    ('diana.matei@example.com', 'Home', 'Diana Matei', '0700 100 012', 'Galati', 'Galati', '800025',
     'Strada Domneasca 60', 'Ap. 14', TRUE)
) AS m (email, label, recipient, phone, city, county, postal_code, street, building, is_default)
JOIN app_user u ON u.email = m.email;

-- Orders: older ones were delivered, the last few weeks are shipped or paid, and a few were cancelled
-- because nobody paid. There are no unpaid ones, since the shop cancels those after an hour.
CREATE TEMPORARY TABLE demo_order (
    n        INTEGER PRIMARY KEY,
    email    TEXT NOT NULL,
    status   TEXT NOT NULL,
    days_ago INTEGER NOT NULL,
    order_id BIGINT
) ON COMMIT DROP;

CREATE TEMPORARY TABLE demo_order_item (
    n        INTEGER NOT NULL,
    slug     TEXT NOT NULL,
    format   TEXT NOT NULL,
    quantity INTEGER NOT NULL
) ON COMMIT DROP;

INSERT INTO demo_order (n, email, status, days_ago) VALUES
    (101, 'ana.popescu@example.com', 'DELIVERED', 250),
    (102, 'mihai.ionescu@example.com', 'DELIVERED', 238),
    (103, 'elena.dumitrescu@example.com', 'DELIVERED', 225),
    (104, 'andrei.stan@example.com', 'DELIVERED', 210),
    (105, 'ioana.georgescu@example.com', 'DELIVERED', 196),
    (106, 'vlad.tudor@example.com', 'DELIVERED', 190),
    (107, 'radu.constantin@example.com', 'CANCELLED', 180),
    (108, 'maria.stoica@example.com', 'DELIVERED', 170),
    (109, 'alexandru.marin@example.com', 'DELIVERED', 160),
    (110, 'cristina.rusu@example.com', 'DELIVERED', 150),
    (111, 'ana.popescu@example.com', 'DELIVERED', 140),
    (112, 'mihai.ionescu@example.com', 'DELIVERED', 128),
    (113, 'elena.dumitrescu@example.com', 'DELIVERED', 115),
    (114, 'vlad.tudor@example.com', 'DELIVERED', 100),
    (115, 'andrei.stan@example.com', 'DELIVERED', 92),
    (116, 'ioana.georgescu@example.com', 'DELIVERED', 80),
    (117, 'paul.neagu@example.com', 'DELIVERED', 75),
    (118, 'radu.constantin@example.com', 'DELIVERED', 66),
    (119, 'maria.stoica@example.com', 'DELIVERED', 55),
    (120, 'cristina.rusu@example.com', 'CANCELLED', 44),
    (121, 'alexandru.marin@example.com', 'DELIVERED', 38),
    (122, 'ana.popescu@example.com', 'DELIVERED', 30),
    (123, 'mihai.ionescu@example.com', 'SHIPPED', 12),
    (124, 'elena.dumitrescu@example.com', 'SHIPPED', 8),
    (125, 'andrei.stan@example.com', 'SHIPPED', 6),
    (126, 'ioana.georgescu@example.com', 'PAID', 3),
    (127, 'radu.constantin@example.com', 'PAID', 2),
    (128, 'vlad.tudor@example.com', 'PAID', 1),
    (129, 'cristina.rusu@example.com', 'PAID', 1),
    (130, 'alexandru.marin@example.com', 'CANCELLED', 4);

INSERT INTO demo_order_item (n, slug, format, quantity) VALUES
    (101, 'pride-and-prejudice', 'PAPERBACK', 1), (101, 'emma', 'PAPERBACK', 1),
    (102, 'the-time-machine', 'PAPERBACK', 1), (102, 'the-war-of-the-worlds', 'PAPERBACK', 1), (102, 'frankenstein', 'EPUB', 1),
    (103, 'anna-karenina', 'HARDCOVER', 1),
    (104, 'the-hound-of-the-baskervilles', 'PAPERBACK', 1), (104, 'the-adventures-of-sherlock-holmes', 'PAPERBACK', 1),
    (104, 'strange-case-of-dr-jekyll-and-mr-hyde', 'PAPERBACK', 1),
    (105, 'little-women', 'PAPERBACK', 1), (105, 'wuthering-heights', 'HARDCOVER', 1), (105, 'jane-eyre', 'PAPERBACK', 1),
    (106, 'the-art-of-war', 'PDF', 1), (106, 'meditations', 'EPUB', 1),
    (107, 'war-and-peace', 'HARDCOVER', 1),
    (108, 'the-wonderful-wizard-of-oz', 'HARDCOVER', 2), (108, 'peter-pan', 'PAPERBACK', 1),
    (109, 'crime-and-punishment', 'PAPERBACK', 1), (109, 'thus-spoke-zarathustra', 'PAPERBACK', 1),
    (110, 'dracula', 'PAPERBACK', 1), (110, 'tales-of-mystery-and-imagination', 'PAPERBACK', 1),
    (111, 'great-expectations', 'HARDCOVER', 1), (111, 'persuasion', 'EPUB', 1),
    (112, 'twenty-thousand-leagues-under-the-seas', 'HARDCOVER', 1), (112, 'journey-to-the-centre-of-the-earth', 'PAPERBACK', 1),
    (113, 'madame-bovary', 'PAPERBACK', 1), (113, 'the-picture-of-dorian-gray', 'PDF', 1),
    (114, 'the-time-machine', 'PDF', 1), (114, 'the-great-gatsby', 'EPUB', 1), (114, 'as-a-man-thinketh', 'PDF', 1),
    (115, 'the-count-of-monte-cristo', 'HARDCOVER', 1),
    (116, 'pride-and-prejudice', 'HARDCOVER', 1), (116, 'sense-and-sensibility', 'PAPERBACK', 1),
    (117, 'moby-dick', 'PAPERBACK', 1),
    (118, 'the-republic', 'PAPERBACK', 1), (118, 'walden', 'PAPERBACK', 1),
    (119, 'grimms-fairy-tales', 'HARDCOVER', 1), (119, 'through-the-looking-glass', 'PAPERBACK', 1),
    (120, 'les-miserables', 'HARDCOVER', 1),
    (121, 'the-great-gatsby', 'PAPERBACK', 2),
    (122, 'crime-and-punishment', 'EPUB', 1), (122, 'war-and-peace', 'PDF', 1),
    (123, 'the-invisible-man', 'PAPERBACK', 1), (123, 'alices-adventures-in-wonderland', 'HARDCOVER', 1),
    (124, 'oliver-twist', 'EPUB', 1), (124, 'a-tale-of-two-cities', 'PAPERBACK', 1),
    (125, 'the-art-of-war', 'HARDCOVER', 1), (125, 'as-a-man-thinketh', 'PAPERBACK', 1),
    (126, 'emma', 'PAPERBACK', 1), (126, 'persuasion', 'PAPERBACK', 1), (126, 'sense-and-sensibility', 'EPUB', 1),
    (127, 'frankenstein', 'PAPERBACK', 1), (127, 'dracula', 'HARDCOVER', 1),
    (128, 'les-miserables', 'EPUB', 1), (128, 'the-picture-of-dorian-gray', 'EPUB', 1),
    (129, 'anna-karenina', 'PAPERBACK', 1), (129, 'wuthering-heights', 'PAPERBACK', 1), (129, 'the-great-gatsby', 'HARDCOVER', 1),
    (130, 'the-count-of-monte-cristo', 'PAPERBACK', 1);

-- Same rules as the shop: printed books go to the customer's default address, delivery costs 2.50 EUR unless
-- the books cost more than 40 EUR, e-book-only orders have no delivery, and paid orders have a Stripe payment.
DO $$
DECLARE
    o            RECORD;
    placed       TIMESTAMPTZ;
    customer_id  BIGINT;
    address_id   BIGINT;
    new_order_id BIGINT;
    subtotal     NUMERIC(10, 2);
    shipping     NUMERIC(10, 2);
BEGIN
    FOR o IN SELECT * FROM demo_order ORDER BY n LOOP
        placed := now() - make_interval(days => o.days_ago);
        SELECT id INTO customer_id FROM app_user WHERE email = o.email;
        address_id := NULL;

        IF EXISTS (SELECT 1 FROM demo_order_item WHERE n = o.n AND format IN ('PAPERBACK', 'HARDCOVER')) THEN
            INSERT INTO order_address (recipient_name, phone, country, city, county_region, postal_code, street_address,
                                       building_details, created_at)
            SELECT recipient_name, phone, country, city, county_region, postal_code, street_address, building_details, placed
            FROM address WHERE user_id = customer_id AND is_default
            RETURNING id INTO address_id;
        END IF;

        INSERT INTO customer_order (user_id, order_number, status, shipping_address_id, currency, created_at, updated_at,
                                    paid_at)
        VALUES (customer_id, 'BK-' || to_char(placed, 'YYYY') || '-' || lpad(o.n::TEXT, 6, '0'), o.status, address_id,
                'EUR', placed, placed + make_interval(days => LEAST(o.days_ago, 5)),
                CASE WHEN o.status = 'CANCELLED' THEN NULL ELSE placed + INTERVAL '4 minutes' END)
        RETURNING id INTO new_order_id;

        INSERT INTO order_item (order_id, product_id, product_title, product_format, quantity, unit_price)
        SELECT new_order_id, p.id, b.title, p.format, i.quantity, p.price
        FROM demo_order_item i
                 JOIN book b ON b.slug = i.slug
                 JOIN product p ON p.book_id = b.id AND p.format = i.format
        WHERE i.n = o.n;

        SELECT SUM(quantity * unit_price) INTO subtotal FROM order_item WHERE order_id = new_order_id;
        shipping := CASE WHEN address_id IS NULL OR subtotal > 40 THEN 0 ELSE 2.50 END;
        UPDATE customer_order SET shipping_cost = shipping WHERE id = new_order_id;

        INSERT INTO payment (order_id, provider, provider_session_id, provider_payment_id, amount, currency, status,
                             expires_at, created_at, updated_at, paid_at)
        VALUES (new_order_id, 'STRIPE', 'cs_demo_' || o.n,
                CASE WHEN o.status = 'CANCELLED' THEN NULL ELSE 'pi_demo_' || o.n END,
                subtotal + shipping, 'EUR', CASE WHEN o.status = 'CANCELLED' THEN 'EXPIRED' ELSE 'SUCCEEDED' END,
                placed + INTERVAL '30 minutes', placed, placed,
                CASE WHEN o.status = 'CANCELLED' THEN NULL ELSE placed + INTERVAL '4 minutes' END);

        UPDATE demo_order SET order_id = new_order_id WHERE n = o.n;
    END LOOP;
END $$;

-- Most customers downloaded their e-books soon after paying; some downloaded them again later.
INSERT INTO ebook_download (order_item_id, downloaded_at)
SELECT oi.id, o.paid_at + make_interval(mins => 10 + d.extra)
FROM order_item oi
         JOIN customer_order o ON o.id = oi.order_id
         JOIN demo_order demo ON demo.order_id = o.id
         CROSS JOIN (VALUES (0), (60 * 24 * 7)) AS d (extra)
WHERE oi.product_format IN ('PDF', 'EPUB')
  AND o.status <> 'CANCELLED'
  AND (d.extra = 0 OR demo.n IN (106, 114))
  AND demo.n <> 128;

-- Reviews: most are approved, a few wait for an administrator and two were turned down.
-- The "Verified purchase" mark is not stored; the shop works it out from the orders.
INSERT INTO review (user_id, book_id, rating, title, comment, status, created_at, updated_at)
SELECT u.id, b.id, m.rating, m.title, m.comment, m.status,
       now() - make_interval(days => m.days_ago), now() - make_interval(days => m.days_ago)
FROM (VALUES
    ('ana.popescu@example.com', 'pride-and-prejudice', 5, 'Still the best',
     'I read it every few years and it still makes me laugh out loud. The paperback is light and easy to carry.', 'APPROVED', 230),
    ('ana.popescu@example.com', 'emma', 4, 'Clever and funny',
     'Emma is annoying at first, which is the whole point. Slower than Pride and Prejudice but worth it.', 'APPROVED', 225),
    ('ana.popescu@example.com', 'great-expectations', 5, 'Beautiful hardcover',
     'The story is wonderful and the hardcover looks great on the shelf.', 'APPROVED', 120),
    ('ana.popescu@example.com', 'crime-and-punishment', 4, NULL,
     'Heavy, but I could not stop reading. The e-book worked well on my reader.', 'APPROVED', 20),
    ('mihai.ionescu@example.com', 'the-time-machine', 5, 'Short and brilliant',
     'Read it in one evening. Amazing how modern it still feels.', 'APPROVED', 220),
    ('mihai.ionescu@example.com', 'the-war-of-the-worlds', 4, 'A classic invasion story',
     'The tripods are still scary. The ending surprised me.', 'APPROVED', 215),
    ('mihai.ionescu@example.com', 'frankenstein', 5, 'Not what I expected',
     'Much sadder and deeper than the films. The creature is the most human character in the book.', 'APPROVED', 210),
    ('mihai.ionescu@example.com', 'twenty-thousand-leagues-under-the-seas', 4, NULL,
     'Long lists of fish, but Captain Nemo makes up for them.', 'APPROVED', 100),
    ('mihai.ionescu@example.com', 'the-invisible-man', 3, 'Good idea, slow start',
     'The first chapters drag a little, the second half is great.', 'PENDING', 5),
    ('elena.dumitrescu@example.com', 'anna-karenina', 5, 'A masterpiece',
     'Every character feels real. The hardcover is heavy but beautifully made.', 'APPROVED', 200),
    ('elena.dumitrescu@example.com', 'madame-bovary', 4, NULL,
     'Flaubert writes so precisely. I felt sorry for Emma and angry with her at the same time.', 'APPROVED', 95),
    ('elena.dumitrescu@example.com', 'the-picture-of-dorian-gray', 5, 'Wilde at his wittiest',
     'Every page has a line worth underlining. The PDF reads well on a tablet.', 'APPROVED', 90),
    ('andrei.stan@example.com', 'the-hound-of-the-baskervilles', 5, 'Best Holmes story',
     'Atmospheric, spooky and very well plotted. Perfect for a rainy weekend.', 'APPROVED', 190),
    ('andrei.stan@example.com', 'the-adventures-of-sherlock-holmes', 4, 'Great short cases',
     'Easy to read one case at a time before bed.', 'APPROVED', 185),
    ('andrei.stan@example.com', 'strange-case-of-dr-jekyll-and-mr-hyde', 4, NULL,
     'Much shorter than I thought, and still very creepy.', 'APPROVED', 180),
    ('andrei.stan@example.com', 'the-count-of-monte-cristo', 5, 'Worth every page',
     'Twelve hundred pages and I was sorry when it ended. The best revenge story ever written.', 'APPROVED', 70),
    ('ioana.georgescu@example.com', 'little-women', 5, 'A comfort read',
     'I loved Jo as a child and I love her even more now.', 'APPROVED', 175),
    ('ioana.georgescu@example.com', 'wuthering-heights', 3, 'Dark and stormy',
     'Beautifully written, but I could not like any of the characters.', 'APPROVED', 170),
    ('ioana.georgescu@example.com', 'jane-eyre', 5, NULL,
     'Jane is one of the strongest heroines in English literature.', 'APPROVED', 168),
    ('ioana.georgescu@example.com', 'sense-and-sensibility', 4, 'Two very different sisters',
     'Elinor is my favourite Austen character. Fast delivery too.', 'APPROVED', 60),
    ('vlad.tudor@example.com', 'the-art-of-war', 4, 'Useful beyond war',
     'Short, quotable and surprisingly practical for work. The PDF downloaded straight away.', 'APPROVED', 185),
    ('vlad.tudor@example.com', 'meditations', 5, 'Read it every morning',
     'A page a day keeps me calm. The EPUB works perfectly on my phone.', 'APPROVED', 180),
    ('vlad.tudor@example.com', 'the-great-gatsby', 4, NULL,
     'Short and sad, with some of the most beautiful sentences I have read.', 'APPROVED', 90),
    ('vlad.tudor@example.com', 'as-a-man-thinketh', 3, 'Nice, but very short',
     'Some good ideas, though it repeats itself a lot for such a small book.', 'APPROVED', 85),
    ('radu.constantin@example.com', 'the-republic', 4, 'Hard but rewarding',
     'I needed a few evenings for each part. The cave allegory alone is worth it.', 'APPROVED', 50),
    ('radu.constantin@example.com', 'walden', 4, NULL,
     'Made me want to switch off my phone for a week.', 'APPROVED', 45),
    ('radu.constantin@example.com', 'war-and-peace', 2, 'Order cancelled',
     'I never got to read it because I forgot to pay. My fault, not the shop''s.', 'REJECTED', 175),
    ('maria.stoica@example.com', 'the-wonderful-wizard-of-oz', 5, 'Bought two copies',
     'One for me and one for my niece. The hardcover makes a lovely present.', 'APPROVED', 160),
    ('maria.stoica@example.com', 'peter-pan', 4, NULL,
     'Funnier and stranger than the cartoon. My niece loved Tinker Bell.', 'APPROVED', 155),
    ('maria.stoica@example.com', 'grimms-fairy-tales', 5, 'Real fairy tales',
     'Darker than the versions I grew up with, and much better.', 'APPROVED', 40),
    ('maria.stoica@example.com', 'through-the-looking-glass', 4, NULL,
     'The chess idea is clever. Read it right after Alice.', 'PENDING', 3),
    ('alexandru.marin@example.com', 'crime-and-punishment', 5, 'Unforgettable',
     'Raskolnikov''s fever dreams stayed with me for weeks.', 'APPROVED', 140),
    ('alexandru.marin@example.com', 'thus-spoke-zarathustra', 3, 'Not easy',
     'Poetic and confusing. I will need to read it again with a guide.', 'APPROVED', 135),
    ('alexandru.marin@example.com', 'the-great-gatsby', 5, 'Bought a second copy as a gift',
     'Short enough to read on a train ride and you will think about it for days.', 'APPROVED', 30),
    ('cristina.rusu@example.com', 'dracula', 5, 'Scarier than any film',
     'The letters and diary entries make it feel real. Perfect for October.', 'APPROVED', 130),
    ('cristina.rusu@example.com', 'tales-of-mystery-and-imagination', 4, NULL,
     'The Tell-Tale Heart is still the creepiest thing I have read.', 'APPROVED', 125),
    ('cristina.rusu@example.com', 'les-miserables', 1, 'Spam',
     'Visit my website for cheap books!!!', 'REJECTED', 40),
    ('paul.neagu@example.com', 'moby-dick', 2, 'Too many whales',
     'Interesting in places but the chapters about whaling went on forever.', 'APPROVED', 60),
    ('diana.matei@example.com', 'the-great-gatsby', 5, 'Can''t wait to reread it',
     'Read it at school and want my own copy now. It''s already in my cart.', 'PENDING', 2),
    ('ana.popescu@example.com', 'jane-eyre', 4, NULL,
     'I borrowed it from a friend first. Mr Rochester is still a puzzle to me.', 'APPROVED', 110),
    ('elena.dumitrescu@example.com', 'pride-and-prejudice', 5, 'Perfect',
     'Witty, warm and still relevant. Elizabeth Bennet forever.', 'APPROVED', 150),
    ('andrei.stan@example.com', 'dracula', 4, NULL,
     'Slow in the middle, but the beginning in Transylvania is unbeatable.', 'APPROVED', 140),
    ('mihai.ionescu@example.com', 'journey-to-the-centre-of-the-earth', 4, 'Great adventure',
     'Pure fun from start to finish, even if the science is long out of date.', 'APPROVED', 95),
    ('cristina.rusu@example.com', 'the-hound-of-the-baskervilles', 5, NULL,
     'I guessed wrong until the very end.', 'APPROVED', 100),
    ('ioana.georgescu@example.com', 'emma', 4, 'Lovely',
     'Funny and gentle. The new paperback arrived quickly.', 'PENDING', 1)
) AS m (email, slug, rating, title, comment, status, days_ago)
JOIN app_user u ON u.email = m.email
JOIN book b ON b.slug = m.slug;

INSERT INTO wishlist_item (user_id, book_id, created_at)
SELECT u.id, b.id, now() - make_interval(days => m.days_ago)
FROM (VALUES
    ('ana.popescu@example.com', 'the-count-of-monte-cristo', 20),
    ('ana.popescu@example.com', 'les-miserables', 18),
    ('ana.popescu@example.com', 'persuasion', 15),
    ('mihai.ionescu@example.com', 'peter-pan', 30),
    ('elena.dumitrescu@example.com', 'war-and-peace', 25),
    ('ioana.georgescu@example.com', 'grimms-fairy-tales', 12),
    ('vlad.tudor@example.com', 'the-republic', 40),
    ('vlad.tudor@example.com', 'thus-spoke-zarathustra', 38),
    ('diana.matei@example.com', 'wuthering-heights', 8),
    ('diana.matei@example.com', 'jane-eyre', 8),
    ('diana.matei@example.com', 'little-women', 7)
) AS m (email, slug, days_ago)
JOIN app_user u ON u.email = m.email
JOIN book b ON b.slug = m.slug;

INSERT INTO cart (user_id)
SELECT id FROM app_user WHERE email IN ('ana.popescu@example.com', 'diana.matei@example.com');

INSERT INTO cart_item (cart_id, product_id, quantity)
SELECT c.id, p.id, m.quantity
FROM (VALUES
    ('ana.popescu@example.com', 'the-count-of-monte-cristo', 'PAPERBACK', 1),
    ('diana.matei@example.com', 'the-great-gatsby', 'PAPERBACK', 1),
    ('diana.matei@example.com', 'peter-pan', 'EPUB', 1)
) AS m (email, slug, format, quantity)
JOIN app_user u ON u.email = m.email
JOIN cart c ON c.user_id = u.id
JOIN book b ON b.slug = m.slug
JOIN product p ON p.book_id = b.id AND p.format = m.format;

INSERT INTO contact_message (name, email, message, handled, created_at, updated_at)
SELECT m.name, m.email, m.message, m.handled, now() - make_interval(days => m.days_ago),
       now() - make_interval(days => m.days_ago)
FROM (VALUES
    ('Ana Popescu', 'ana.popescu@example.com',
     'Hello! Will you also sell Northanger Abbey? I have every other Austen novel from you.', TRUE, 90),
    ('Bogdan Ilie', 'bogdan.ilie@example.com',
     'Do you deliver to the Republic of Moldova? I could not find it in the address form.', TRUE, 60),
    ('Maria Stoica', 'maria.stoica@example.com',
     'Could you wrap a book as a present if I write it in the order? Thank you!', TRUE, 50),
    ('Teodora Pavel', 'teodora.pavel@example.com',
     'My EPUB will not open on an old Kindle. Is there a file I can use?', FALSE, 6),
    ('Mihai Ionescu', 'mihai.ionescu@example.com',
     'My order says shipped. Do you have a tracking number for the courier?', FALSE, 3),
    ('George Radu', 'george.radu@example.com',
     'Are there discounts for schools? We would like 25 copies of The Time Machine for a class.', FALSE, 1)
) AS m (name, email, message, handled, days_ago);
