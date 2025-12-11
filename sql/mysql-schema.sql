-- Schema for BankCore demo using MySQL and MyBatis
CREATE DATABASE IF NOT EXISTS bankcore DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bankcore;

CREATE TABLE IF NOT EXISTS accounts (
    account_id VARCHAR(64) PRIMARY KEY,
    customer_id VARCHAR(64) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    total_balance DECIMAL(18,2) NOT NULL,
    available_balance DECIMAL(18,2) NOT NULL,
    frozen_balance DECIMAL(18,2) DEFAULT 0,
    status VARCHAR(16) DEFAULT 'ACTIVE',
    opened_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS customers (
    customer_id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    credit_code VARCHAR(32) NOT NULL,
    contact_name VARCHAR(64) NOT NULL,
    contact_phone VARCHAR(32) NOT NULL,
    onboard_date DATE NOT NULL,
    risk_level VARCHAR(32) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'NORMAL',
    segment VARCHAR(64) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS payments (
    request_id VARCHAR(64) NOT NULL,
    instruction_id VARCHAR(64) PRIMARY KEY,
    payer_account VARCHAR(64) NOT NULL,
    payee_account VARCHAR(64) NOT NULL,
    payer_customer_id VARCHAR(64) NOT NULL,
    payer_customer_status VARCHAR(16) NOT NULL,
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

CREATE TABLE IF NOT EXISTS payment_requests (
    request_id VARCHAR(64) PRIMARY KEY,
    payment_instruction_id VARCHAR(64),
    status VARCHAR(32) NOT NULL,
    message VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS cash_pools (
    pool_id VARCHAR(64) PRIMARY KEY,
    header_account VARCHAR(64) NOT NULL,
    member_accounts TEXT,
    target_balance DECIMAL(18,2) NOT NULL,
    strategy VARCHAR(32) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS risk_rules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL,
    type VARCHAR(64) NOT NULL,
    expression VARCHAR(255) DEFAULT NULL,
    threshold DECIMAL(18,2) DEFAULT NULL,
    enabled TINYINT(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS risk_decision_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id VARCHAR(64) NOT NULL,
    payer_account VARCHAR(64) NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    result VARCHAR(16) NOT NULL,
    rule_type VARCHAR(64),
    rule_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_risk_log_customer_date (customer_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed demo data
INSERT INTO accounts(account_id, customer_id, currency, total_balance, available_balance, frozen_balance, opened_at) VALUES
    ('ACCT-1001', 'CUST-001', 'CNY', 500000.00, 500000.00, 0, NOW()),
    ('ACCT-1002', 'CUST-002', 'CNY', 300000.00, 300000.00, 0, NOW()),
    ('ACCT-1003', 'CUST-003', 'USD', 120000.00, 120000.00, 0, NOW())
ON DUPLICATE KEY UPDATE total_balance=VALUES(total_balance), available_balance=VALUES(available_balance), frozen_balance=VALUES(frozen_balance);

INSERT INTO customers(customer_id, name, credit_code, contact_name, contact_phone, onboard_date, risk_level, status, segment) VALUES
    ('CUST-001', '华夏科创有限公司', '91310000MA1K123X1', '张经理', '13800001111', CURDATE(), 'LOW', 'NORMAL', 'CORP'),
    ('CUST-002', '联鑫国际贸易集团', '91310000MA1K234X2', '李总', '13800002222', CURDATE(), 'MEDIUM', 'RISKY', 'CORP'),
    ('CUST-003', '远航供应链科技', '91310000MA1K345X3', '王财务', '13800003333', CURDATE(), 'HIGH', 'BLOCKED', 'SME')
ON DUPLICATE KEY UPDATE credit_code=VALUES(credit_code), status=VALUES(status), risk_level=VALUES(risk_level);

INSERT INTO payments(request_id, instruction_id, payer_account, payee_account, payer_customer_id, payer_customer_status, currency, amount, purpose, channel, batch_id, priority, risk_score, status, created_at) VALUES
    ('REQ-PMT-INIT-1', 'PMT-INIT-1', 'ACCT-1001', 'ACCT-1002', 'CUST-001', 'NORMAL', 'CNY', 10000.00, 'Payroll batch', 'H2H', 'BATCH-202401', 4, 15.00, 'PENDING', NOW()),
    ('REQ-PMT-INIT-2', 'PMT-INIT-2', 'ACCT-1002', 'ACCT-1003', 'CUST-002', 'RISKY', 'USD', 25000.00, 'Vendor settlement FX', 'API', 'BATCH-202402', 2, 38.00, 'IN_RISK_REVIEW', NOW())
ON DUPLICATE KEY UPDATE status=VALUES(status), risk_score=VALUES(risk_score), payer_customer_status=VALUES(payer_customer_status);

INSERT INTO payment_requests(request_id, payment_instruction_id, status, message)
VALUES
    ('REQ-PMT-INIT-1', 'PMT-INIT-1', 'PENDING', 'seed request'),
    ('REQ-PMT-INIT-2', 'PMT-INIT-2', 'PENDING', 'seed request')
ON DUPLICATE KEY UPDATE status=VALUES(status), payment_instruction_id=VALUES(payment_instruction_id);

INSERT INTO cash_pools(pool_id, header_account, member_accounts, target_balance, strategy) VALUES
    ('POOL-001', 'ACCT-1001', 'ACCT-1002,ACCT-1003', 800000.00, 'TARGET_BALANCE'),
    ('POOL-002', 'ACCT-1002', 'ACCT-1001', 150000.00, 'ZERO_BALANCE')
ON DUPLICATE KEY UPDATE target_balance=VALUES(target_balance);

INSERT INTO risk_rules(name, type, expression, threshold, enabled) VALUES
    ('Per txn limit 1M', 'LIMIT_PER_TXN', NULL, 1000000.00, 1),
    ('Daily total 3M', 'LIMIT_DAILY', NULL, 3000000.00, 1),
    ('Blacklist demo', 'BLACKLIST', 'CUST-003,ACCT-9999', NULL, 1)
ON DUPLICATE KEY UPDATE threshold=VALUES(threshold), enabled=VALUES(enabled);
