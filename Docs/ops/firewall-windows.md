---
title: Windows Firewall Setup
description: Windows firewall rules for development and hosting.
tags:
  - operations
  - firewall
  - windows
status: reference
phase: ongoing
owner: ops
action: none
---
## Ops: Windows Firewall (single-host proxy + backends)

This runbook assumes **proxy + all Paper backends are on the same Windows host**.

### Goal

- **Public internet** can reach **proxy only**: TCP `25565`.
- **Backends** (`lobby-1`, `survival-1`, `amplified-1`) are **not reachable** from the internet:
  - TCP `25568`, `25566`, `25567` should only be reachable locally (or from a private subnet if you later split hosts).
- **MySQL** (`3306`) is reachable only from explicitly allowed hosts (backends + Discord bot host).

### Notes

- If you set each backend `server-ip=127.0.0.1` (recommended), those backend ports are **already not reachable** off-host. Firewall rules then become an extra safety net.
- Run PowerShell **as Administrator**.


### 2) Block backend inbound ports (defense in depth)

```powershell
New-NetFirewallRule -DisplayName "LW Backends Block (Inbound TCP 25566-25568)" `
  -Direction Inbound -Action Block -Protocol TCP -LocalPort 25566,25567,25568
```


### 4) Quick verification commands

```powershell
Get-NetFirewallRule -DisplayName "LW *" | Get-NetFirewallPortFilter
```

From another machine on the internet/private LAN, confirm:

- `proxyHost:25565` is reachable
- `proxyHost:25566/25567/25568` is **not** reachable
- `dbHost:3306` is reachable **only** from allowed hosts

