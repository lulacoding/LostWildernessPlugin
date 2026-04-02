---
title: Anti-Cheat
description: Planned anti-cheat plugin setup and configuration.
tags:
  - plugins
  - anticheat
status: planned
phase: phase-2
owner: ops
action: needs-ops
---
# Anticheat

## Vulcan

- **Vulcan Anti-Cheat** — **paid** (~20 USD); used **server-side**; syncs across the two backends (bans/flags follow the player). **Offline inventory checking** mentioned.
- **PacketEvents 2.80+** (Modrinth) for 1.21.5 compatibility.
- **Webhook alerts** to admin Discord.

## Negativity

- **Negativity v2** — **8 Euro**; runs on **BungeeCord/Velocity** proxy for a first pass; **Geyser/Floodgate friendly**; **Bedrock** and **light** checks so Bedrock movement isn’t falsely flagged.
- **Bedrock** players are **exempt** from certain movement checks (movement flags) because of client differences.

## Other options considered

- **Themis AntiCheat** — **open source**; mentioned as more compatible with **Geyser and Floodgate**. Link: https://discord.gg/5DT5eaPe.
- **Wither AntiCheat** — code was suggested to look at.
- Building a **custom anticheat** was discussed but deemed **“hella effort”** (detection logic, not just rules); **Java is easy to decompile** so custom AC would need ongoing work. *“that would take hella effort tho like cause its not just defing can and cant do shit its creating like ai to detect things.”*

## Whitelist and client policy

- **Vanilla and OptiFine only** (whitelist); **skip names beginning with \*** (Bedrock prefix) in name-based checks.
- **Paper** has **built-in anti-xray**; no separate xray plugin required.

## BungeeGuard

- **BungeeGuard** is **planned** so only the proxy can connect to the backend servers (secure backend access).

## Source

- Direct Messages (Grovyle187): Vulcan purchase, Themis/Wither, Bedrock *, Vanilla/Optifine, custom AC discussion.
- Bible sections 2 and 3: Negativity, PacketEvents, webhook, BungeeGuard.
