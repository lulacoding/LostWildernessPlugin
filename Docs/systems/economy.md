---
title: Economy
description: Planned multi-currency wallet and transaction system (not yet started).
tags:
  - system
  - economy
status: planned
phase: phase-2
owner: dev
action: needs-dev
---
# Plugin: Economy

Wallets, multi-currency, transactions. No main-thread DB.

## Purpose

- Multi-currency wallets per player.
- All balance changes: read from cache, update, async persist.
- Optional transaction log for auditing.

## Main classes

| Class | Responsibility |
|-------|----------------|
| `Currency` | Currency id, symbol, display name. |
| `EconomyService` / `WalletService` | getBalance, deposit, withdraw; async persistence. |
| `WalletRepository` | Async load/save wallet rows. |
| `TransactionLogRepository` | Optional: log each transaction for auditing. |

## Config

- `config/economy.yml` – Currencies, default balances, caps (if any).

## DB

- `wallets` – e.g. `player_uuid`, `currency_id`, `balance`, `updated_at`.
- `transaction_log` (optional) – player, currency, amount, reason, timestamp.

## Integration

- Shops, quest rewards, and other modules call EconomyService; never access DB from listeners.
