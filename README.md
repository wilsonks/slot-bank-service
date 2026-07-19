# slot-bank-service

`slot-bank-service` is a Spring Boot 3.x microservice that manages wallet balances for an EGM slot-floor platform. It records money movement in a ledger-backed account model for deposits, withdrawals, buy-ins, buy-outs, and slot bet reserve/settle/refund flows.

## Scope

This service handles money movement only:
- Deposit credits into a player wallet
- Withdraw credits from a player wallet
- Buy-in to move value into play
- Buy-out to cash value back from play
- Reserve a bet for a spin
- Settle a reserved bet with or without winnings
- Refund a reserved bet

## Technology

- Java 21
- Spring Boot 3.3.x
- Spring Web, Validation, Security, Actuator
- Spring Data JPA + PostgreSQL
- Flyway migrations
- Micrometer Prometheus metrics
- Testcontainers for PostgreSQL integration tests
- Maven build

## API

All non-actuator endpoints require JWT bearer authentication in runtime environments.

### Accounts

#### Get balance
`GET /api/v1/accounts/{uid}`

Response:
```json
{
  "playerUid": "player-123",
  "balance": 1200,
  "updatedAt": "2026-07-16T12:00:00Z"
}
```

#### Deposit
`POST /api/v1/accounts/{uid}/deposit`

Payload:
```json
{
  "amount": 500,
  "referenceId": "dep-1001",
  "transBy": "cashier-1",
  "egmId": "egm-12"
}
```

#### Withdraw
`POST /api/v1/accounts/{uid}/withdraw`

Payload:
```json
{
  "amount": 200,
  "referenceId": "wd-1001",
  "transBy": "cashier-1",
  "egmId": "egm-12"
}
```

#### Buy-in
`POST /api/v1/accounts/{uid}/buy-in`

Payload:
```json
{
  "amount": 300,
  "referenceId": "buyin-1001",
  "transBy": "kiosk-1",
  "egmId": "egm-12"
}
```

#### Buy-out
`POST /api/v1/accounts/{uid}/buy-out`

Payload:
```json
{
  "amount": 150,
  "referenceId": "buyout-1001",
  "transBy": "kiosk-1",
  "egmId": "egm-12"
}
```

#### Player ledger
`GET /api/v1/accounts/{uid}/ledger?entryType=DEPOSIT&from=2026-07-01T00:00:00Z&to=2026-07-31T23:59:59Z&page=0&size=20`

### Bets

#### Reserve bet
`POST /api/v1/bets/reserve`

Payload:
```json
{
  "spinId": "spin-1001",
  "accountUid": "player-123",
  "betAmount": 100,
  "egmId": "egm-12"
}
```

#### Settle bet
`POST /api/v1/bets/settle`

Payload:
```json
{
  "spinId": "spin-1001",
  "winAmount": 250
}
```

#### Refund bet
`POST /api/v1/bets/{spinId}/refund`

### Ledger admin query
`GET /api/v1/ledger?accountUid=player-123&egmId=egm-12&entryType=BET_RESERVE&page=0&size=20`

## Ledger entry model

Each ledger record stores:
- `accountUid`: player account identifier
- `entryType`: one of `DEPOSIT`, `WITHDRAW`, `BUY_IN`, `BUY_OUT`, `BET_RESERVE`, `BET_SETTLE_WIN`, `BET_SETTLE_NO_WIN`, `BET_REFUND`
- `amount`: signed amount in credits
- `referenceId`: idempotency/reference key
- `egmId`: slot machine identifier when relevant
- `balanceAfter`: balance snapshot after the entry
- `transBy`: originator/operator when relevant
- `createdAt`: entry timestamp

Positive amounts represent credits. Negative amounts represent debits.

## Concurrency control

The `accounts` table includes a JPA `@Version` column for optimistic locking. Update methods run in transactions and are retried with `@Retryable` on optimistic locking failures. This helps prevent lost updates during concurrent balance changes, including simultaneous bet reservations.

## Idempotency strategy

- Deposit, withdraw, buy-in, and buy-out operations use `(referenceId, entryType)` to detect duplicates.
- Bet reservation uses unique `spinId`.
- Bet settlement returns the existing reservation if already settled.
- Bet refund returns the existing reservation if already refunded.

## Environment variables

| Variable | Default | Description |
| --- | --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/slotbank` | PostgreSQL JDBC URL |
| `DB_USER` | `slotbank` | Database username |
| `DB_PASSWORD` | `slotbank` | Database password |
| `SERVER_PORT` | `8084` | HTTP server port |
| `AUTH_SERVICE_JWKS_URL` | `http://localhost:8081/.well-known/jwks.json` | JWKS endpoint for JWT validation |

## Local run

### Prerequisites
- Java 21
- Docker (optional for local PostgreSQL/Testcontainers)

### Start PostgreSQL with Docker Compose
```bash
docker compose up -d postgres
```

### Run the service
```bash
export JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
mvn spring-boot:run
```

## Testing

```bash
export JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
mvn test
```

## Build

```bash
export JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
mvn clean install
```

## Docker

### Build image
```bash
docker build -t slot-bank-service .
```

### Run full stack
```bash
docker compose up --build
```

## Observability

- Actuator health: `/actuator/health`
- Prometheus metrics: `/actuator/prometheus`
- Structured JSON logging outside the `test` profile
