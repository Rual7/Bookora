# Bookora

An online bookstore for **printed books** and **e-books**, built as a single-page application: a **Java Spring Boot** REST backend, a **PostgreSQL** database and a **React** frontend.

> **Status:** in development. The architecture and database are designed; the application is being implemented.

## Contents

1. [About](#about)
2. [Features](#features)
3. [Technology stack](#technology-stack)
4. [Architecture](#architecture)
5. [Database](#database)
6. [Main business rules](#main-business-rules)
7. [Security](#security)
8. [Quality and delivery](#quality-and-delivery)
9. [Project structure](#project-structure)
10. [Running locally](#running-locally)

## About

Bookora sells two kinds of products:

- **Printed books** (paperback, hardcover) with managed stock and delivery to an address.
- **E-books** (PDF, EPUB) with no stock, downloadable only by the customer who paid for them.

A **book** is one catalog entry with its title, description, authors, categories, cover and reviews. Each book can be offered in several **formats**, and every format has its own price, stock and ISBN. Customers pay online with **Stripe Checkout**, and the payment is confirmed on the backend through a signed webhook.

## Features

**Customer**

- Register, sign in and sign out
- Browse the catalog with search, filters (category, author, format) and pagination
- Book page with formats, authors, categories and reviews
- Shopping cart and wishlist
- Saved delivery addresses
- Checkout with online payment and order history
- Download purchased e-books
- Rate and review books (moderated)

**Administrator**

- Manage books, formats, categories and authors
- Upload the e-book file of a digital format
- Moderate reviews
- View orders and update their status

**Interface**

- Image carousel for featured books on the home page
- CSS animations
- Responsive layout for desktop, tablet and phone

## Technology stack

| Layer      | Technology                                                                    |
| ---------- | ----------------------------------------------------------------------------- |
| Backend    | Java 25 (LTS), Spring Boot 4 (Web MVC, Data JPA, Security, Validation), Maven |
| Database   | PostgreSQL 18, Flyway migrations                                              |
| Frontend   | React 19, Vite, React Router, plain CSS (separate stylesheets)                |
| Payments   | Stripe Checkout and webhooks                                                  |
| Security   | Spring Security, Bucket4j (rate limiting), Cloudflare Turnstile (CAPTCHA)     |
| Testing    | JUnit 5, Mockito                                                              |
| Logging    | SLF4J and Logback, written to a file                                          |
| Web server | Apache HTTP Server (static files, reverse proxy, `.htaccess`)                 |
| DevOps     | Docker, Docker Compose, Git and GitHub, GitHub Actions                        |

## Architecture

```
 Browser (React SPA)
        |  HTTPS
        v
 Apache HTTP Server (.htaccess)
   |                     |
   | static files        | /api/**
   v                     v
 React build         Spring Boot (REST)
                     Controller -> Service -> Repository -> PostgreSQL
                          |
                          +-- Stripe (Checkout + webhook)
```

The backend is layered: **controller** (HTTP), **service** (business logic, defined by interfaces), **repository** (data access), **model** (JPA entities), **dto**, **exception** and **security**. Apache serves the frontend and proxies `/api` to Spring Boot, so both live on the same domain and no CORS configuration is needed.

## Database

PostgreSQL, **19 tables**, in **third normal form (3NF)**. Details, diagram and rules are in [`docs/database`](docs/database/README.md); the schema itself is [`V1__init.sql`](docs/database/V1__init.sql).

| Area                | Tables                                                                                |
| ------------------- | ------------------------------------------------------------------------------------- |
| Users               | `app_user`, `address`                                                                 |
| Catalog             | `book`, `product`, `author`, `book_author`, `category`, `book_category`, `ebook_file` |
| Customer            | `review`, `wishlist_item`, `cart`, `cart_item`                                        |
| Orders and payments | `customer_order`, `order_address`, `order_item`, `payment`, `ebook_download`          |
| Infrastructure      | `processed_webhook_event`                                                             |

Main design choices:

- **Book and product are separate.** Shared data (authors, categories, description, reviews) lives on the book; price, stock and ISBN live on each format.
- **Totals are not stored.** Order totals and average ratings are computed through views.
- **Orders keep a snapshot** of titles, prices and the delivery address, so history stays correct when the catalog changes.
- **Constraints protect the data:** valid ratings, non-negative prices and stock, no stock for e-books, one review per user and book, one successful payment per order.

## Main business rules

- Prices are always recalculated on the server; the browser is never trusted.
- Stock for printed books is reserved when checkout starts and released if the payment session expires.
- An order is marked as paid only after Stripe's signed webhook arrives, and each webhook event is processed once.
- Orders move through `PENDING_PAYMENT`, `PAID`, `SHIPPED` and `DELIVERED` (or `CANCELLED`). Orders with only e-books need no delivery address.
- An e-book can be downloaded only by the user who owns a paid order for it, and only after the file passed its security check. Files are stored outside the public web root.
- New reviews are visible only after an administrator approves them.
- Books and formats are deactivated, never deleted, so past orders remain intact.

## Security

| Measure          | Solution                                                                                              |
| ---------------- | ----------------------------------------------------------------------------------------------------- |
| Authentication   | Session in an `HttpOnly` cookie, passwords hashed with BCrypt                                         |
| Authorization    | `CUSTOMER` and `ADMIN` roles                                                                          |
| CSRF             | Spring Security token protection                                                                      |
| XSS              | React output escaping and a Content-Security-Policy                                                   |
| SQL injection    | Parameterized queries through Spring Data JPA                                                         |
| Uploads          | 12 MB limit, extension allow-list, real file type verification                                        |
| Steganography    | Uploaded images are re-encoded on the server; other files get a scan status before they are published |
| Rate limiting    | More than 100 requests per minute from one address are blocked temporarily (`429`)                    |
| CAPTCHA          | Cloudflare Turnstile on registration and sign-in                                                      |
| Security headers | Set in Apache, target grade A                                                                         |
| `.htaccess`      | Blocks sensitive files and directory listing                                                          |
| Payments         | Stripe webhook signature verification                                                                 |

## Quality and delivery

- **Unit tests** with JUnit 5 and Mockito for the service layer
- **Git** with a stable `main` branch and feature branches
- **GitHub Actions** builds and tests the project on every push and pull request
- **Logging** of user actions and security events to a rotating log file
- **SEO**: page titles and descriptions, `robots.txt`, image alt text, Lighthouse SEO score of 90 or more
- **Hosting**: online deployment on a provider that supports Docker, PostgreSQL and HTTPS (to be chosen)

## Project structure

```
bookora/
├── backend/        Spring Boot application (Maven)
├── frontend/       React application (Vite)
├── docker/         Apache configuration and Dockerfiles
├── docs/           Database schema and documentation
├── .github/        GitHub Actions workflows
├── docker-compose.yml
├── .env.example
└── README.md
```

## Running locally

**Requirements:** JDK 25, Node.js (current LTS), Docker with Docker Compose, a Stripe account in test mode.

```bash
cp .env.example .env          # fill in the local values
docker compose up -d db       # PostgreSQL
cd backend && ./mvnw spring-boot:run     # Flyway migrations run on startup (API on :8080)
cd frontend && npm install && npm run dev  # app on :5173, /api is proxied to :8080
```

Notes:

- PostgreSQL is published on host port **5433** (`DB_PORT`) so it does not clash with a local PostgreSQL on 5432. `DB_URL` in `.env` must use the same port.
- Maven needs `JAVA_HOME` to point at JDK 25.
- The full stack (Apache + backend + database) can be built with `docker compose --profile full up -d --build`; the site is then on http://localhost:8081.

Configuration is read from environment variables: database connection, Stripe keys, Turnstile secret, e-book storage path and public base URL.
