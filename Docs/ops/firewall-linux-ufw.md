---
title: Linux UFW Firewall Setup
description: UFW firewall rules for Linux backend servers.
tags:
  - operations
  - firewall
  - linux
status: reference
phase: ongoing
owner: ops
action: none
---
## Ops: Linux UFW firewall (proxy + backends)

This is a **starting point** for a typical setup:

- Public internet → Proxy (TCP `25565`)
- Proxy → Backends (TCP `25566`, `25567`, `25568`) on a private LAN
- Backends + Discord bot host → DB (TCP `3306`)

Adjust ports, interfaces, and IPs to match your actual deployment.


### Example: backend host (separate machine)

Replace `PROXY_IP` with your proxy’s private IP (or CIDR).

```bash
sudo ufw default deny incoming
sudo ufw default allow outgoing

# Allow only the proxy to connect to backend ports
sudo ufw allow from PROXY_IP to any port 25566 proto tcp
sudo ufw allow from PROXY_IP to any port 25567 proto tcp
sudo ufw allow from PROXY_IP to any port 25568 proto tcp

# (Optional) SSH admin access from your admin IP
sudo ufw allow from ADMIN_IP to any port 22 proto tcp

sudo ufw enable
sudo ufw status numbered
```

---

### Example: DB host (separate machine)

Replace `BACKEND_CIDR` / `BOT_IP` with your real addresses.

```bash
sudo ufw default deny incoming
sudo ufw default allow outgoing

sudo ufw allow from BACKEND_CIDR to any port 3306 proto tcp
sudo ufw allow from BOT_IP to any port 3306 proto tcp

sudo ufw enable
sudo ufw status numbered
```

