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

---

### 1) Allow proxy inbound (public entry)

```powershell
New-NetFirewallRule -DisplayName "LW Proxy 25565 (Inbound TCP)" `
  -Direction Inbound -Action Allow -Protocol TCP -LocalPort 25565
```

If you also expose query (you currently have `query_port: 25577` but `query_enabled: false`), either leave it closed or add a rule explicitly.

---

### 2) Block backend inbound ports (defense in depth)

```powershell
New-NetFirewallRule -DisplayName "LW Backends Block (Inbound TCP 25566-25568)" `
  -Direction Inbound -Action Block -Protocol TCP -LocalPort 25566,25567,25568
```

---

### 3) Restrict MySQL inbound to an allowlist

Replace the IPs with the **actual** backend/bot host IPs if MySQL is on a different machine.

If MySQL is on the **same host** and only local services use it, prefer binding MySQL to `127.0.0.1` in `my.cnf` and/or blocking inbound `3306` entirely.

Example allowlist rule:

```powershell
# Example: allow only from a private subnet (edit to your real ranges)
New-NetFirewallRule -DisplayName "LW MySQL 3306 allow private subnet" `
  -Direction Inbound -Action Allow -Protocol TCP -LocalPort 3306 `
  -RemoteAddress 10.0.0.0/8,192.168.0.0/16,172.16.0.0/12
```

And an explicit block (placed after allow rules works fine):

```powershell
New-NetFirewallRule -DisplayName "LW MySQL 3306 block all other inbound" `
  -Direction Inbound -Action Block -Protocol TCP -LocalPort 3306
```

---

### 4) Quick verification commands

```powershell
Get-NetFirewallRule -DisplayName "LW *" | Get-NetFirewallPortFilter
```

From another machine on the internet/private LAN, confirm:

- `proxyHost:25565` is reachable
- `proxyHost:25566/25567/25568` is **not** reachable
- `dbHost:3306` is reachable **only** from allowed hosts

