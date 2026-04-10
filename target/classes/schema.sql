-- Run this in MySQL Workbench to set up the database

CREATE DATABASE IF NOT EXISTS inventory_db;
USE inventory_db;

-- Users (admin + clients with login access)
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    username    VARCHAR(50)  UNIQUE NOT NULL,
    email       VARCHAR(100) UNIQUE NOT NULL,
    password    VARCHAR(255) NOT NULL,
    role        ENUM('ADMIN','CLIENT') DEFAULT 'ADMIN',
    phone       VARCHAR(15),
    active      BOOLEAN DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Inventory items master
CREATE TABLE IF NOT EXISTS inventory_items (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    name                VARCHAR(100) NOT NULL,
    description         TEXT,
    category            VARCHAR(50),
    unit                VARCHAR(20)  DEFAULT 'pcs',
    quantity            INT          DEFAULT 0,
    low_stock_threshold INT          DEFAULT 10,
    purchase_price      DECIMAL(10,2),
    selling_price       DECIMAL(10,2),
    sku                 VARCHAR(50)  UNIQUE,
    active              BOOLEAN      DEFAULT TRUE,
    created_at          TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Clients (buyers / deferred payment customers)
CREATE TABLE IF NOT EXISTS clients (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(100),
    phone       VARCHAR(15),
    address     TEXT,
    upi_id      VARCHAR(100),
    active      BOOLEAN   DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Purchase records (items bought / restocked)
CREATE TABLE IF NOT EXISTS purchases (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    item_id         BIGINT          NOT NULL,
    quantity        INT             NOT NULL,
    unit_price      DECIMAL(10,2)   NOT NULL,
    total_amount    DECIMAL(10,2)   GENERATED ALWAYS AS (quantity * unit_price) STORED,
    supplier        VARCHAR(100),
    purchase_date   TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    notes           TEXT,
    created_by      BIGINT,
    FOREIGN KEY (item_id)    REFERENCES inventory_items(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- Sales header
CREATE TABLE IF NOT EXISTS sales (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    sale_number     VARCHAR(50)  UNIQUE,
    client_id       BIGINT,
    total_amount    DECIMAL(10,2) DEFAULT 0,
    discount        DECIMAL(10,2) DEFAULT 0,
    net_amount      DECIMAL(10,2) DEFAULT 0,
    payment_status  ENUM('PAID','PENDING','PARTIAL','OVERDUE') DEFAULT 'PENDING',
    sale_date       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes           TEXT,
    created_by      BIGINT,
    FOREIGN KEY (client_id)  REFERENCES clients(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- Sale line items
CREATE TABLE IF NOT EXISTS sale_items (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    sale_id     BIGINT          NOT NULL,
    item_id     BIGINT          NOT NULL,
    quantity    INT             NOT NULL,
    unit_price  DECIMAL(10,2)   NOT NULL,
    total_price DECIMAL(10,2)   GENERATED ALWAYS AS (quantity * unit_price) STORED,
    FOREIGN KEY (sale_id) REFERENCES sales(id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES inventory_items(id)
);

-- Payment records
CREATE TABLE IF NOT EXISTS payment_records (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    sale_id             BIGINT          NOT NULL,
    client_id           BIGINT,
    amount              DECIMAL(10,2)   NOT NULL,
    payment_method      ENUM('UPI','CASH','BANK_TRANSFER','DEFERRED') NOT NULL,
    payment_status      ENUM('PAID','PENDING','OVERDUE')   DEFAULT 'PENDING',
    due_date            DATE,
    paid_at             TIMESTAMP,
    upi_transaction_id  VARCHAR(100),
    razorpay_order_id   VARCHAR(100),
    alert_sent          BOOLEAN   DEFAULT FALSE,
    notes               TEXT,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sale_id)   REFERENCES sales(id),
    FOREIGN KEY (client_id) REFERENCES clients(id)
);

-- Full audit / transaction log
CREATE TABLE IF NOT EXISTS transaction_log (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_type    ENUM('PURCHASE','SALE','STOCK_ADJUSTMENT') NOT NULL,
    item_id             BIGINT,
    item_name           VARCHAR(100),
    quantity_change     INT,
    quantity_before     INT,
    quantity_after      INT,
    unit_price          DECIMAL(10,2),
    reference_id        BIGINT,
    reference_type      VARCHAR(50),
    performed_by        BIGINT,
    transaction_date    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes               TEXT,
    FOREIGN KEY (item_id)       REFERENCES inventory_items(id),
    FOREIGN KEY (performed_by)  REFERENCES users(id)
);

-- Default admin user  (password: Admin@123 — bcrypt)
INSERT IGNORE INTO users (username, email, password, role, phone)
VALUES ('admin', 'admin@inventory.com',
        '$2a$12$9u.jGYeHqBJT9U8bNq9Mf.VqJmEkVMWXTuS3IqShK9mCIJtF0p1Gu',
        'ADMIN', '9999999999');
