# BankCore1

This repository contains a lightweight implementation of Solution A (企业现金管理与资金池平台) with Spring Boot microservice skeletons. It focuses on common corporate banking scenarios such as settlement accounts, in-house transfers, scheduled sweeps, and reconciliation jobs.

## Modules
- `common`: Shared DTOs and value objects used across services.
- `auth-service`: Issues JWT tokens for downstream services; placeholder security gateway.
- `api-gateway`: Optional entry point to route external traffic to internal services.
- `customer-service`: Maintains customer KYC and segment data.
- `account-service`: Manages corporate accounts (multi-currency, three-way balances, lifecycle status) and basic ledger entries.
- `payment-service`: Accepts payment instructions, applies risk checks/idempotency/limits, and posts to ledger abstraction.
- `treasury-service`: Manages cash pool structures, interest, and executes sweeping/target-balance strategies.
- `risk-service`: Risk rule engine (单笔/单日限额、黑名单) with MyBatis-backed rules table and decision log.
- `reconciliation-service`: Handles external reconciliation file upload, compares against internal payments, stores daily summary/break tables, and exposes CSV export APIs.
- `notification-service`: Asynchronous notification gateway; consumes RabbitMQ events (支付成功/失败、对账异常) or REST calls and
  fans out to email (mock SMTP on localhost:1025) or webhook callbacks for企业 ERP 集成。
- `frontend`: React + Vite + Ant Design workbench that surfaces account, payment, and cash pool workflows.
- `cross-cutting`: Unified error codes, MDC trace IDs, and Actuator endpoints for health/metrics.

## Quick start
Each module is an independent Spring Boot 2.7 application using Java 1.8, MySQL 8.x, and MyBatis for persistence.

## 日志、错误码与监控
- 统一异常：`@RestControllerAdvice` 通过自定义 `BusinessException` 与 `ErrorCode`（如 `NOT_FOUND`、`BUSINESS_RULE_VIOLATION`、`RISK_REJECTED`、`PROCESSING` 等）封装 API 错误响应，调用方可基于错误码做重试/降级或友好提示。
- 链路追踪：全局 `TraceIdFilter` 会读取/生成 `X-Trace-Id` 写入 MDC 并回写响应头，所有日志自动带 traceId，关键日志额外打印 paymentId/accountId/customerId，方便排查跨服务与 MQ 场景。
- 可观测性：所有 Spring Boot 服务引入 Actuator，默认暴露 `/actuator/health`、`/actuator/info`、`/actuator/metrics`，健康检查展示依赖状态，可进一步接入 Prometheus/Grafana 或 ELK/SkyWalking。

1. Start MySQL 8.x and RabbitMQ locally (recommended: Docker) and load the schema/seed data:
   ```bash
   # start MySQL + RabbitMQ with sample schema automatically loaded
   docker compose up -d mysql rabbitmq

   # or load the schema manually into an existing instance
   mysql -u bankcore -p < sql/mysql-schema.sql
   ```
   The script creates the `bankcore` schema plus sample accounts, payments, cash pools, and default risk rules/blacklist entries.

2. Launch services (update `application.yml` datasource credentials if needed):
   ```bash
   mvn -pl auth-service spring-boot:run
   mvn -pl customer-service spring-boot:run
   mvn -pl account-service spring-boot:run
   mvn -pl payment-service spring-boot:run
   mvn -pl treasury-service spring-boot:run
   mvn -pl risk-service spring-boot:run
   mvn -pl reconciliation-service spring-boot:run
   mvn -pl notification-service spring-boot:run
   ```
   Default ports: account 8081, customer 8082, payment 8083, treasury 8084, reconciliation 8087.

3. Launch the front-end workbench (requires Node.js 18+):
   ```bash
   cd frontend
   cp .env.example .env # adjust backend base URLs if not running locally
   npm install
   npm run dev
   ```
   The dev server listens on http://localhost:5173 by default and calls the account (8081), payment (8083), and treasury (8084)
   services; adjust `.env` if you run services on different ports.

## Build notes
- The root `bankcore-parent` now inherits from `spring-boot-starter-parent 2.7.18`, which provides managed plugin/dependency versions so modules build cleanly with Java 8.
- To build all Java services locally (skipping tests), run `mvn clean package -DskipTests` from the repository root after ensuring Maven can reach the public repositories.
- If your environment uses an internal Maven proxy, configure it in `~/.m2/settings.xml` to resolve Spring Boot/MyBatis/MySQL artifacts.

## Sample APIs
- Customer/KYC: onboard enterprise customers with credit code +联系人信息，支持查询客户风控状态，并可查询名下账户列表。
- Account: create account, query balances, freeze/unfreeze funds, settle outgoing payments, and close zero-balance accounts.
- Payment: submit transfer order with request-level idempotency, review status, trigger retry；在提交时会根据付款账户所属客户的 KYC 状态（NORMAL/RISKY/BLOCKED）自动阻断或进入风控复核。
- Treasury: define cash pool (PHYSICAL/NOTIONAL) with three-way balances, set daily interest rate, trigger manual sweep or accrual, and rely on nightly scheduled sweeps plus 23:30 interest posting.
- Reconciliation: `POST /recon/file/upload` to ingest external CSV对账文件（instruction_id/external_ref/payer_account/payee_account/currency/amount），自动匹配 `payments` 表生成 `MATCHED/INTERNAL_ONLY/EXTERNAL_ONLY/AMOUNT_MISMATCH`，并可通过 `GET /recon/summary/{id}/export` 导出差异 CSV 给运营复核。

Sample CSV for upload (header required):
```
instruction_id,external_ref,payer_account,payee_account,currency,amount
PMT-INIT-1,EXT-202401-001,ACCT-1001,ACCT-1002,CNY,10000.00
PMT-INIT-3,EXT-202401-002,ACCT-1003,ACCT-1002,USD,5000.00
```
- Reconciliation: upload external statement CSV (`instruction_id,external_ref,payer_account,payee_account,currency,amount`), compare against the `payments` table, and export mismatch CSVs. Daily batch metadata is persisted to `recon_summary`/`recon_break`.

### Front-end pages
- **Dashboard**：汇总账户余额、风控/清算队列、现金池策略与批次监控，方便演示端到端流量。
- **账户管理**：创建结算账户、入账/出账交易，实时读取 MyBatis+MySQL 持久化数据。
- **支付指令**：录入单笔或批量支付，触发 MQ 异步风控+清算，支持批次/渠道/优先级字段展示与人工放行/记账。
- **现金池**：配置 Pool 与成员账户，设置目标余额/池类型/日利率，手工触发 sweep 或利息计提，定时任务会在 23:00 自动 sweep、23:30 生成利息凭证。

The services now use MyBatis + MySQL for persistence with mapper XMLs under each module's `resources/mapper` folder. Datasource defaults point to `jdbc:mysql://localhost:3306/bankcore` with user/password `bankcore`, and you can override them per environment in `application.yml`.

## 强化的实业务逻辑与异步支付示例
- **幂等性**：`payment-service` 新增 `payment_requests` 表，POST `/payments` 必须携带 `request_id`，若请求重复且已有成功结果则直接返回原指令，处理中则返回处理中状态，失败允许重试并复用同一 `request_id`。
- **异步+MQ 清算**：提交支付仅做基础校验和持久化，随后将事件写入 RabbitMQ（`payment.events.exchange`/`payment.events.queue`）；消费者串联风控（调用 `risk-service`）+账户冻结/结算（调用 `account-service`），失败自动落入 DLQ，便于面试讲解重试/死信设计。
- **批量入队**：`POST /payments/batch/process` 将指令号列表入队，快速模拟批量代付/分账调度；可通过 RabbitMQ 控制台观察积压与消费。
- **通知解耦**：`notification-service` 监听 `notification.events.exchange`，也支持 `POST /notifications/events` 直接投递事件，按 EMAIL/WEBHOOK 异步推送，便于模拟“支付成功回执”、“对账差异报警”场景。

## 同步代码到 GitHub
如果需要将仓库推送到远端（例如 `https://github.com/KongYiji1994/BankCore1`），可按以下步骤操作：

1. 配置远端：
   ```bash
   git remote add origin https://github.com/KongYiji1994/BankCore1.git
   ```
2. 确认远端：
   ```bash
   git remote -v
   ```
3. 推送分支（假设当前在 `work` 分支）：
   ```bash
   git push -u origin work
   ```

如需推送其他分支或标签，替换命令中的分支名即可。若使用 SSH，需要先配置好 SSH key，并将远端地址改为 `git@github.com:KongYiji1994/BankCore1.git`。

### 一键同步脚本
如果希望在命令行一键同步到 GitHub，可运行仓库自带的脚本（默认推送 `work` 分支，如需其它分支将分支名作为第二个参数传入）：

```bash
./scripts/sync-to-github.sh https://github.com/KongYiji1994/BankCore1.git work
```

脚本会自动添加 `origin` 远端、尝试拉取最新引用并推送当前分支，遇到需要认证时会提示输入凭据（或使用已配置的 SSH/令牌）。
