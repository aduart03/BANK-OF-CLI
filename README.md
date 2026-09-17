# Bank of CLI

Terminal banking app — Java 17, Maven, PostgreSQL, JDBC.

## Setup

1. Start Postgres:
```bash
docker run --name bankofcli-db -e POSTGRES_PASSWORD=mypassword \
  -e POSTGRES_DB=bankofcli -p 5434:5432 -d postgres
```

2. Create `bank-of-cli/src/main/resources/db.properties`:
```
DB_URL=jdbc:postgresql://localhost:5434/bankofcli
DB_USER=postgres
DB_PASSWORD=mypassword
```

3. Run:
```bash
cd bank-of-cli
mvn compile exec:java -Dexec.mainClass="com.bankofcli.api.Main"
```

Tables are created automatically on startup. Logs are written to `bank.log` in the working directory.

## Commands

login, signup, logout, balance, deposit, withdraw, transfer, fetchTransactions, update, delete, help, exit

## Architecture

- `api` — terminal I/O
- `service` — business rules and validation
- `persistence` — JDBC and SQL
- `domain` — Account, Transaction