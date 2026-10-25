-- ================================================================
-- UniSwap database schema
-- ================================================================
-- NOTE: You do NOT need to run this file manually for the app to
-- work. spring.jpa.hibernate.ddl-auto=update (see application.properties)
-- creates and updates these tables automatically the first time you
-- run the app, and createDatabaseIfNotExist=true creates the database
-- itself too.
--
-- This script is provided as a reference so you can see the exact
-- schema being created, and as a manual fallback / for documentation
-- purposes (e.g. showing table relationships in your project report).
--
-- IMPORTANT: `condition` and `year` are reserved/troublesome words in
-- MySQL, so they are intentionally named `item_condition` and
-- `study_year` below - matching the @Column(name = "...") mappings
-- in the Java entities exactly.
-- ================================================================

CREATE DATABASE IF NOT EXISTS uniswap_db;
USE uniswap_db;

-- ----------------------------------------------------------------
-- users  (Module 1: Authentication & Profile)
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name       VARCHAR(255) NOT NULL,
    email           VARCHAR(255) NOT NULL,
    username        VARCHAR(255) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,   -- BCrypt hash, never plain text
    course          VARCHAR(255),
    study_year      VARCHAR(255),
    campus          VARCHAR(255),
    bio             VARCHAR(500),
    created_at      DATETIME NOT NULL,
    UNIQUE KEY uk_users_email (email),
    UNIQUE KEY uk_users_username (username)
);

-- ----------------------------------------------------------------
-- categories  (used by Module 2 listings)
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS categories (
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    name    VARCHAR(255) NOT NULL,
    UNIQUE KEY uk_categories_name (name)
);

-- ----------------------------------------------------------------
-- listings  (Module 2: Marketplace / Listings)
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS listings (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                VARCHAR(255) NOT NULL,
    description         VARCHAR(1000),
    category_id         BIGINT NOT NULL,
    price               DOUBLE,
    item_condition      VARCHAR(20) NOT NULL,   -- NEW / LIKE_NEW / GOOD / FAIR
    transaction_type    VARCHAR(20) NOT NULL,   -- SELL / EXCHANGE / BORROW / DONATE
    status              VARCHAR(20) NOT NULL,   -- ACTIVE / SOLD / EXCHANGED / BORROWED / DONATED / INACTIVE
    owner_id            BIGINT NOT NULL,
    location            VARCHAR(255),
    image_url           VARCHAR(255),           -- e.g. /uploads/listings/<uuid>.jpg (NULL = no photo)
    created_at          DATETIME NOT NULL,
    CONSTRAINT fk_listings_category FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT fk_listings_owner FOREIGN KEY (owner_id) REFERENCES users(id),
    INDEX idx_listings_status (status),
    INDEX idx_listings_category (category_id)
);

-- ----------------------------------------------------------------
-- favourites  (Module 4: Favourites & Wishlist)
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS favourites (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    listing_id  BIGINT NOT NULL,
    created_at  DATETIME NOT NULL,
    CONSTRAINT fk_favourites_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_favourites_listing FOREIGN KEY (listing_id) REFERENCES listings(id),
    UNIQUE KEY uk_favourites_user_listing (user_id, listing_id)   -- prevents duplicate favourites
);

-- ----------------------------------------------------------------
-- requests  (Module 3: Buy, Exchange & Borrow Requests)
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS requests (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    listing_id      BIGINT NOT NULL,
    requester_id    BIGINT NOT NULL,
    type            VARCHAR(20) NOT NULL,   -- SELL(=buy) / EXCHANGE / BORROW / DONATE
    message         VARCHAR(1000),
    extra_detail    VARCHAR(255),           -- exchange offer, or borrow duration
    status          VARCHAR(20) NOT NULL,   -- PENDING / ACCEPTED / REJECTED / CANCELLED / COMPLETED
    created_at      DATETIME NOT NULL,
    updated_at      DATETIME,
    CONSTRAINT fk_requests_listing FOREIGN KEY (listing_id) REFERENCES listings(id),
    CONSTRAINT fk_requests_requester FOREIGN KEY (requester_id) REFERENCES users(id),
    INDEX idx_requests_status (status)
);

-- ----------------------------------------------------------------
-- transactions  (Module 5: My Listings & Transactions)
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS transactions (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    listing_id      BIGINT NOT NULL,
    requester_id    BIGINT NOT NULL,
    owner_id        BIGINT NOT NULL,
    type            VARCHAR(20) NOT NULL,
    amount          DOUBLE,
    status          VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    completed_at    DATETIME NOT NULL,
    CONSTRAINT fk_transactions_listing FOREIGN KEY (listing_id) REFERENCES listings(id),
    CONSTRAINT fk_transactions_requester FOREIGN KEY (requester_id) REFERENCES users(id),
    CONSTRAINT fk_transactions_owner FOREIGN KEY (owner_id) REFERENCES users(id)
);

-- ----------------------------------------------------------------
-- Default categories (also auto-seeded by DataSeeder.java on first run)
-- ----------------------------------------------------------------
INSERT IGNORE INTO categories (name) VALUES
    ('Books'), ('Electronics'), ('Stationery'),
    ('College Supplies'), ('Lab Equipment'), ('Other');

-- Upgrading an existing database? (Hibernate's ddl-auto=update does this automatically.)
-- ALTER TABLE listings ADD COLUMN image_url VARCHAR(255) NULL;
