-- Schema for BankCore demo using MySQL and MyBatis
CREATE DATABASE IF NOT EXISTS bankcore DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bankcore;

CREATE TABLE IF NOT EXISTS accounts (
    account_id VARCHAR(64) PRIMARY KEY,
    customer_id VARCHAR(64) NOT NULL,
    currency VARCHAR(8) NOT NULL,
    balance DECIMAL(18,2) NOT NULL,
    available_balance DECIMAL(18,2) NOT NULL,
    frozen_amount DECIMAL(18,2) DEFAULT 0,
    status VARCHAR(32) DEFAULT 'ACTIVE',
    opened_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS customers (
    customer_id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    national_id VARCHAR(32) NOT NULL,
    onboard_date DATE NOT NULL,
    risk_level VARCHAR(32) NOT NULL,
    segment VARCHAR(64) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS payments (
    instruction_id VARCHAR(64) PRIMARY KEY,
    payer_account VARCHAR(64) NOT NULL,
    payee_account VARCHAR(64) NOT NULL,
    currency VARCHAR(8) NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    purpose VARCHAR(128) NOT NULL,
    channel VARCHAR(32) DEFAULT NULL,
    batch_id VARCHAR(64) DEFAULT NULL,
    priority INT DEFAULT 5,
    risk_score DECIMAL(5,2) DEFAULT 0,
    status VARCHAR(32) NOT NULL,
    created_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS cash_pools (
    pool_id VARCHAR(64) PRIMARY KEY,
    header_account VARCHAR(64) NOT NULL,
    member_accounts TEXT,
    target_balance DECIMAL(18,2) NOT NULL,
    strategy VARCHAR(32) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed demo data
INSERT INTO accounts(account_id, customer_id, currency, balance, available_balance, opened_at) VALUES
    ('ACCT-1001', 'CUST-001', 'CNY', 500000.00, 500000.00, NOW()),
    ('ACCT-1002', 'CUST-002', 'CNY', 300000.00, 300000.00, NOW()),
    ('ACCT-1003', 'CUST-003', 'USD', 120000.00, 120000.00, NOW())
ON DUPLICATE KEY UPDATE balance=VALUES(balance), available_balance=VALUES(available_balance);

INSERT INTO payments(instruction_id, payer_account, payee_account, currency, amount, purpose, channel, batch_id, priority, risk_score, status, created_at) VALUES
    ('PMT-INIT-1', 'ACCT-1001', 'ACCT-1002', 'CNY', 10000.00, 'Payroll batch', 'H2H', 'BATCH-202401', 4, 15.00, 'INITIATED', NOW()),
    ('PMT-INIT-2', 'ACCT-1002', 'ACCT-1003', 'USD', 25000.00, 'Vendor settlement FX', 'API', 'BATCH-202402', 2, 38.00, 'IN_RISK_REVIEW', NOW())
ON DUPLICATE KEY UPDATE status=VALUES(status), risk_score=VALUES(risk_score);

INSERT INTO cash_pools(pool_id, header_account, member_accounts, target_balance, strategy) VALUES
    ('POOL-001', 'ACCT-1001', 'ACCT-1002,ACCT-1003', 800000.00, 'TARGET_BALANCE'),
    ('POOL-002', 'ACCT-1002', 'ACCT-1001', 150000.00, 'ZERO_BALANCE')
ON DUPLICATE KEY UPDATE target_balance=VALUES(target_balance);
