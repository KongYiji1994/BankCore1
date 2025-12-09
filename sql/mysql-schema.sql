-- Schema for BankCore demo using MySQL and MyBatis
CREATE DATABASE IF NOT EXISTS bankcore DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bankcore;

CREATE TABLE IF NOT EXISTS accounts (
    account_id VARCHAR(64) PRIMARY KEY,
    customer_id VARCHAR(64) NOT NULL,
    currency VARCHAR(8) NOT NULL,
    balance DECIMAL(18,2) NOT NULL,
    available_balance DECIMAL(18,2) NOT NULL,
    opened_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS payments (
    instruction_id VARCHAR(64) PRIMARY KEY,
    payer_account VARCHAR(64) NOT NULL,
    payee_account VARCHAR(64) NOT NULL,
    currency VARCHAR(8) NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    purpose VARCHAR(128) NOT NULL,
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

INSERT INTO payments(instruction_id, payer_account, payee_account, currency, amount, purpose, status, created_at) VALUES
    ('PMT-INIT-1', 'ACCT-1001', 'ACCT-1002', 'CNY', 10000.00, 'Payroll batch', 'INITIATED', NOW()),
    ('PMT-INIT-2', 'ACCT-1002', 'ACCT-1003', 'USD', 2500.00, 'Vendor settlement', 'RISK_REVIEWED', NOW())
ON DUPLICATE KEY UPDATE status=VALUES(status);

INSERT INTO cash_pools(pool_id, header_account, member_accounts, target_balance, strategy) VALUES
    ('POOL-001', 'ACCT-1001', 'ACCT-1002,ACCT-1003', 800000.00, 'TARGET_BALANCE'),
    ('POOL-002', 'ACCT-1002', 'ACCT-1001', 150000.00, 'ZERO_BALANCE')
ON DUPLICATE KEY UPDATE target_balance=VALUES(target_balance);
