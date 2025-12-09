# BankCore1

This repository contains a lightweight implementation of Solution A (企业现金管理与资金池平台) with Spring Boot microservice skeletons. It focuses on common corporate banking scenarios such as settlement accounts, in-house transfers, scheduled sweeps, and reconciliation jobs.

## Modules
- `common`: Shared DTOs and value objects used across services.
- `account-service`: Manages corporate accounts and basic ledger entries.
- `payment-service`: Accepts payment instructions, applies risk checks, and posts to ledger abstraction.
- `treasury-service`: Manages cash pool structures and executes sweeping/target-balance strategies.
- `settlement-batch`: Spring Batch job to generate reconciliation files and detect breaks.

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
   mvn -pl account-service spring-boot:run
   mvn -pl payment-service spring-boot:run
   mvn -pl treasury-service spring-boot:run
   mvn -pl settlement-batch spring-boot:run
   ```

## Sample APIs
- Account: create account, query balance, post debit/credit.
- Payment: submit transfer order, review status, trigger retry.
- Treasury: define cash pool, register member accounts, run manual sweep to header account.
- Settlement Batch: launch a job that consumes payment events and emits a reconciliation summary.

The services now use MyBatis + MySQL for persistence with mapper XMLs under each module's `resources/mapper` folder. Datasource defaults point to `jdbc:mysql://localhost:3306/bankcore` with user/password `bankcore`, and you can override them per environment in `application.yml`.

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
