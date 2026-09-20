-- ============================================================
-- E-Bookstore Platform — PostgreSQL Schema
-- IBM Capstone Project
-- Run with: psql -U ecomuser -d ecomdb -f schema.sql
-- ============================================================

-- Users
CREATE TABLE IF NOT EXISTS users (
    id             BIGSERIAL    PRIMARY KEY,
    username       VARCHAR(50)  NOT NULL UNIQUE,
    email          VARCHAR(100) NOT NULL UNIQUE,
    password_hash  TEXT         NOT NULL,
    full_name      VARCHAR(100),
    phone          VARCHAR(20),
    role           VARCHAR(20)  NOT NULL DEFAULT 'CUSTOMER',
    gift_points    INTEGER      NOT NULL DEFAULT 0,
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Categories
CREATE TABLE IF NOT EXISTS categories (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(80)  NOT NULL UNIQUE,
    description VARCHAR(255),
    image_url   TEXT,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Products (Books)
CREATE TABLE IF NOT EXISTS products (
    id                     BIGSERIAL        PRIMARY KEY,
    title                  VARCHAR(200)     NOT NULL,
    author                 VARCHAR(150),
    isbn                   VARCHAR(20)      UNIQUE,
    description            TEXT,
    price                  NUMERIC(10,2)    NOT NULL,
    original_price         NUMERIC(10,2),
    stock_quantity         INTEGER          NOT NULL DEFAULT 0,
    image_url              TEXT,
    publisher              VARCHAR(100),
    publication_year       INTEGER,
    tentative_delivery_days INTEGER         DEFAULT 5,
    average_rating         NUMERIC(3,2)     DEFAULT 0.00,
    review_count           INTEGER          DEFAULT 0,
    status                 VARCHAR(20)      NOT NULL DEFAULT 'ACTIVE',
    category_id            BIGINT           REFERENCES categories(id) ON DELETE SET NULL,
    created_at             TIMESTAMP        NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMP        NOT NULL DEFAULT NOW()
);

-- Addresses
CREATE TABLE IF NOT EXISTS addresses (
    id            BIGSERIAL    PRIMARY KEY,
    user_id       BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    full_name     VARCHAR(100) NOT NULL,
    address_line1 TEXT         NOT NULL,
    address_line2 TEXT,
    city          VARCHAR(100) NOT NULL,
    state         VARCHAR(100) NOT NULL,
    postal_code   VARCHAR(20)  NOT NULL,
    country       VARCHAR(60)  NOT NULL,
    phone         VARCHAR(20),
    is_default    BOOLEAN      DEFAULT FALSE,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Orders
CREATE TABLE IF NOT EXISTS orders (
    id                   BIGSERIAL        PRIMARY KEY,
    order_number         VARCHAR(30)      NOT NULL UNIQUE,
    user_id              BIGINT           NOT NULL REFERENCES users(id),
    shipping_address_id  BIGINT           REFERENCES addresses(id) ON DELETE SET NULL,
    subtotal             NUMERIC(12,2)    NOT NULL,
    discount_amount      NUMERIC(12,2)    DEFAULT 0.00,
    gift_points_redeemed INTEGER          DEFAULT 0,
    total_amount         NUMERIC(12,2)    NOT NULL,
    status               VARCHAR(20)      NOT NULL DEFAULT 'PENDING',
    notes                TEXT,
    created_at           TIMESTAMP        NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP        NOT NULL DEFAULT NOW()
);

-- Order Items
CREATE TABLE IF NOT EXISTS order_items (
    id          BIGSERIAL     PRIMARY KEY,
    order_id    BIGINT        NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id  BIGINT        NOT NULL REFERENCES products(id),
    quantity    INTEGER       NOT NULL,
    unit_price  NUMERIC(10,2) NOT NULL,
    total_price NUMERIC(12,2) NOT NULL
);

-- Carts (one per user)
CREATE TABLE IF NOT EXISTS carts (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT    NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Cart Items
CREATE TABLE IF NOT EXISTS cart_items (
    id          BIGSERIAL PRIMARY KEY,
    cart_id     BIGINT    NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    product_id  BIGINT    NOT NULL REFERENCES products(id),
    quantity    INTEGER   NOT NULL,
    UNIQUE (cart_id, product_id)
);

-- Payments
CREATE TABLE IF NOT EXISTS payments (
    id               BIGSERIAL     PRIMARY KEY,
    order_id         BIGINT        NOT NULL UNIQUE REFERENCES orders(id) ON DELETE CASCADE,
    amount           NUMERIC(12,2) NOT NULL,
    method           VARCHAR(30)   NOT NULL,
    status           VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    -- Only masked reference / gateway ID stored — never full card numbers
    transaction_ref  VARCHAR(80),
    gateway_response TEXT,
    paid_at          TIMESTAMP,
    created_at       TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_products_category    ON products(category_id);
CREATE INDEX IF NOT EXISTS idx_products_status      ON products(status);
CREATE INDEX IF NOT EXISTS idx_orders_user          ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_status        ON orders(status);
CREATE INDEX IF NOT EXISTS idx_order_items_order    ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_cart_items_cart      ON cart_items(cart_id);
CREATE INDEX IF NOT EXISTS idx_addresses_user       ON addresses(user_id);

-- Seed categories
INSERT INTO categories (name, description) VALUES
    ('Fiction',       'Novels, short stories, and literary fiction'),
    ('Non-Fiction',   'Biographies, history, and reference books'),
    ('Science',       'Physics, biology, chemistry, and scientific journals'),
    ('Technology',    'Programming, AI, cloud computing, and IT'),
    ('Self-Help',     'Personal development and motivational books'),
    ('Children',      'Picture books, early readers, and young adult fiction'),
    ('Business',      'Entrepreneurship, finance, and management')
ON CONFLICT (name) DO NOTHING;
