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
- `risk-service`: Simple risk-rule endpoint for limits/blacklist checks.
- `reconciliation-service`: Batch job to generate reconciliation files and detect breaks.
- `notification-service`: Sends outbound notifications (SMS/email/webhook placeholder).
- `frontend`: React + Vite + Ant Design workbench that surfaces account, payment, and cash pool workflows.

## Quick start
Each module is an independent Spring Boot 2.7 application using Java 1.8, MySQL 8.x, and MyBatis for persistence.

1. Start MySQL 8.x locally (recommended: Docker) and load the schema/seed data:
   ```bash
   # start MySQL with sample schema automatically loaded
   docker compose up -d mysql

   # or load the schema manually into an existing instance
   mysql -u bankcore -p < sql/mysql-schema.sql
   ```
   The script creates the `bankcore` schema plus sample accounts, payments, and cash pools.

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
   Default ports: account 8081, customer 8082, payment 8083, treasury 8084.

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
- Payment: submit transfer order, review status, trigger retry；在提交时会根据付款账户所属客户的 KYC 状态（NORMAL/RISKY/BLOCKED）自动阻断或进入风控复核。
- Treasury: define cash pool, register member accounts, run manual sweep to header account.
- Settlement Batch: launch a job that consumes payment events and emits a reconciliation summary.

### Front-end pages
- **Dashboard**：汇总账户余额、风控/清算队列、现金池策略与批次监控，方便演示端到端流量。
- **账户管理**：创建结算账户、入账/出账交易，实时读取 MyBatis+MySQL 持久化数据。
- **支付指令**：录入单笔或批量支付，触发多线程风控+清算，支持批次/渠道/优先级字段展示与人工放行/记账。
- **现金池**：配置 Pool 与成员账户，设置目标余额与策略，手工触发 sweep 场景。

The services now use MyBatis + MySQL for persistence with mapper XMLs under each module's `resources/mapper` folder. Datasource defaults point to `jdbc:mysql://localhost:3306/bankcore` with user/password `bankcore`, and you can override them per environment in `application.yml`.

## 强化的实业务逻辑与多线程示例
- **支付风控+清算并发流水线**：`payment-service` 使用自定义线程池（`paymentTaskExecutor`）驱动 `processAsync`，先并发计算风险评分（金额/币种/优先级等维度叠加），分流到“拒绝”“通过+清算”“大额跨境等待”三类状态，再调度清算适配器模拟落地网联/跨境通道，批量处理时会并行消费多个指令并产出 `PaymentBatchResult` 汇总。
- **更丰富的支付字段**：指令表新增 `channel`、`batch_id`、`priority`、`risk_score`，可区分银企直联批量代发、API 代收等常见渠道，并记录风控评分结果。
- **批量处理端点**：`POST /payments/batch/process` 接收指令号列表，利用线程池并行风控 + 清算，按成功、风控拒绝、失败维度统计，模拟银行真实批量任务的吞吐与落地结果反馈。

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
