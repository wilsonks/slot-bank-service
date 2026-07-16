# slot-bank-service

Bank/Wallet service for the `slot-central` microservices platform — owns money movement ONLY (deposit, withdraw, buy-in, buy-out, bet reserve/settle), implemented as a double-entry ledger backed by Postgres.

This service does NOT own player identity/profile (nickname, PIN, on-hold status) — that's `slot-auth-service`'s responsibility. This service does NOT compute spin math — that's `slot-game-engine-service`. It is called by `slot-game-controller-service` to reserve bets and settle winnings, using an idempotency key (spinId) to prevent double-debit/double-credit on retries.

This repository is part of a re-architecture of the `slot-central-server-express-rmq` Node.js EGM slot-floor backend into Spring Boot microservices.

Scaffolding in progress.
