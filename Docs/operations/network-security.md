---
title: Network & Security
description: Network topology and security hardening.
tags:
  - operations
  - network
  - security
status: reference
phase: ongoing
owner: ops
action: none
---
## Future: Network & Security Architecture

Goal: move from a “works on my machine” setup to a **clean, proxy‑centric network** that can safely host **hundreds of players**, withstand basic abuse, and be easy to reason about.

This page assumes:
- One or more **public entry points** (proxies: BungeeCord/Velocity).
- Multiple **backend** Paper servers (`lobby`, `survival`, `amplified`, later more).


### 2. Proxy ↔ backend security

Minecraft proxying is simple but easy to misconfigure. The core guarantees you want:

- Backends **never** trust the remote IP/UUID/username they see from the socket directly; they only trust data forwarded by the proxy.
- If someone bypasses the proxy and connects directly to a backend, they should be **kicked or blocked by firewall**.

**Backend config (each Paper server):**

- `server.properties`:
  - `online-mode=false`
- `spigot.yml`:
  - `bungeecord: true`
- If using Velocity modern forwarding, configure the corresponding Paper option (velocity forwarding) instead of `bungeecord: true`.

**Proxy config:**

- Bungee/Waterfall:
  - `online_mode: true`
  - `ip_forward: true`
  - Define `servers:` (`lobby`, `survival`, etc.) with **internal addresses** only.

**Optional but recommended:**

- **BungeeGuard / Velocity‑forwarding key:**
  - Generate a secret key; configure it on the proxy and on each backend (plugin or built‑in).
  - Backends reject any forwarded player connection that does not present this key.

Result: even if someone finds a backend IP:port, they cannot impersonate arbitrary players or bypass Mojang auth.


### 4. IPs, DNS, and domains

To feel “Hypixel‑level”, the network should have a **stable, simple domain story**:

- Primary domain: e.g. `lostwilderness.net`.
- Records:
  - `mc.lostwilderness.net` → proxy IP.
  - `status.lostwilderness.net` or similar for any external status page / HTTP endpoints.

Recommended:

- Use a **long TTL** on the game SRV/A records once stable.
- Keep a dedicated section in this file listing current DNS records and their purpose, so ops and docs stay aligned.


### 6. Implementation (ServerUPDATE)

This section records the **actual** layout and config for the Lost Wilderness deploy under `ServerUPDATE/`.

**Bind addresses and ports:**

| Backend     | Port  | Bind address (recommended) | Config location |
|------------|-------|----------------------------|-----------------|
| Proxy      | 25565 | 0.0.0.0 (public) or bind to public IP | `ServerUPDATE/proxy/config.yml` |
| lobby-1    | 25568 | 127.0.0.1 or 10.x (private) | `ServerUPDATE/backends/lobby-1/server.properties` (`server-port=25568`) |
| survival-1 | 25566 | 127.0.0.1 or 10.x (private) | `ServerUPDATE/backends/survival-1/server.properties` (`server-port=25566`) |
| amplified-1 | 25567 | 127.0.0.1 or 10.x (private) | `ServerUPDATE/backends/amplified-1/server.properties` (`server-port=25567`) |

To bind backends to localhost only, set in each `server.properties`: `server-ip=127.0.0.1` (or leave empty and rely on firewall).

**Shared secret (recommended): BungeeGuard**

This prevents forged proxy forwarding and stops direct-backend connections from impersonating players.

- Proxy-side example: `Server/proxy/plugins/BungeeGuard/config.yml.example`
- Backend-side examples:
  - `Server/backends/lobby-1/plugins/BungeeGuard/config.yml.example`
  - `Server/backends/survival-1/plugins/BungeeGuard/config.yml.example`
  - `Server/backends/amplified-1/plugins/BungeeGuard/config.yml.example`

Implementation notes:

- Install the BungeeGuard jar on the **proxy** and on **every backend**.
- Copy each `.example` file to `config.yml`.
- Generate a long random token and set it in the proxy `allowed-tokens`, and the same value as the backend `token`.

**Proxy config (BungeeCord):**

- `ServerUPDATE/proxy/config.yml`: `online_mode: true`, `ip_forward: true`, `host: 0.0.0.0:25565`.
- `servers:` use **internal** addresses only: `lobby-1` → `127.0.0.1:25568`, `survival-1` → `127.0.0.1:25566`, `amplified-1` → `127.0.0.1:25567`.
- `priorities:` lobby-1, survival-1, amplified-1 (lobby-1 first for new connections).

**Backend config (each Paper server in ServerUPDATE/backends/):**

- `server.properties`: `online-mode=false`, `server-port` as in table above.
- `spigot.yml`: `settings.bungeecord: true`.

**Firewall rules (summary):**

- **Allow:** Internet → proxy port 25565 only.
- **Allow:** Proxy host → 127.0.0.1:25568, 127.0.0.1:25566, 127.0.0.1:25567 (backend ports).
- **Allow:** Backends + Discord bot host → MySQL (e.g. 3306).
- **Deny:** Direct internet access to backend ports (25566, 25567, 25568) and to MySQL.

Example (Linux UFW): allow 25565/tcp for proxy; allow from proxy IP to 25566,25567,25568; allow 3306 from backend/bot IPs only; default deny incoming.

Runbooks:

- Windows (PowerShell): `Docs/ops/firewall-windows.md`
- Linux (UFW): `Docs/ops/firewall-linux-ufw.md`

**DNS (document when set):**

- Primary game: e.g. `mc.lostwilderness.net` → proxy IP. TTL and status page can be added here when in use.

**Checklist status (ServerUPDATE):**

- [x] Backends use private ports; proxy is sole public entry (config in place; firewall is ops responsibility).
- [x] All backends have `online-mode=false`; proxy has `online_mode: true` and `ip_forward: true`.
- [ ] BungeeGuard or Velocity forwarding secret configured (example configs committed; enable by installing jars + setting token).
- [ ] Firewall rules applied on host (document above; apply per environment).
- [x] Connection throttling on proxy (`connection_throttle`, `connection_throttle_limit` in config).
- [ ] DNS entries documented (fill when production domain is set). 

