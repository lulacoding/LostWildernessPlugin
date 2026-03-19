## Future: Auth, Anticheat & Trust Boundaries

To operate at Hypixel‑level scale, you must be very clear about:

- **Who you trust** (and for what information).
- How you detect and handle **cheats** and compromised accounts.

This page ties together proxy auth, backend settings, and anticheat plans.

---

### 1. Authentication chain

The desired chain is:

`Mojang / Microsoft login → Proxy → Backend`

Where:

- Proxy validates **online‑mode** sessions (unless the network ever runs in offline mode behind heavy auth, which is not recommended).
- Proxy forwards:
  - UUID
  - Username
  - IP address
- Backend **never** trusts the socket’s remote IP/name; it only trusts what the proxy forwards.

Key points:

- Backends must have:
  - `online-mode=false`
  - `bungeecord: true` or Velocity modern forwarding configured.
- A shared secret (BungeeGuard / Velocity key) prevents forged proxy packets.

Document:

- Which auth mode you run in.
- Any exceptions (e.g. testing setups).

---

### 2. Bedrock cross‑play (future)

If/when you add **Geyser/Floodgate**:

- New trust chain:
  - Bedrock client → Geyser proxy → Java proxy → backend.
- Backends still see a **Java identity** (Floodgate does name/UUID mapping).

Considerations:

- Name formats for Bedrock players (prefixes or suffixes).
- Verification and whitelist flows for Bedrock vs Java.
- Anticheat: some checks may need looser thresholds for Bedrock input jitter.

Capture Geyser/Floodgate config decisions here when you implement them.

---

### 3. Anticheat layers

Hypixel‑tier networks treat anticheat as **layers**, not a single plugin:

- **Network‑level**:
  - Connection rate limiting, suspicious IP blocking, basic geo/ASN filters.
- **Server‑level** (anticheat plugin like Vulcan/Negativity):
  - Movement checks (speed, fly, no‑fall).
  - Combat checks (aim, reach).
  - Interaction checks (fast break/place, scaffold).
- **Business logic level**:
  - Limit suspicious behaviours (e.g. insane portal creation, exploit‑like movement).

For Lost Wilderness:

- Decide on a primary anticheat plugin (e.g. Vulcan) and which worlds it runs on (Survival/Amplified vs Lobby).
- Document:
  - Baseline config.
  - Known false‑positive patterns to ignore or tune around.

---

### 4. Permissions & trust boundaries

Clearly define:

- Which commands are **operator‑only** (e.g. `/resetcalendar`, deep debug).
- Which roles in Discord map to in‑game perms (via verification status / clan roles).
- Where cross‑trust happens:
  - Discord roles → in‑game clans or perks.
  - Clans → Discord channels/roles.

Goal: you can draw a diagram showing where **privilege escalation is possible**, and ensure each edge has logging and safeguards.

---

### 5. Incident handling (security/cheating)

You’ll eventually face:

- Compromised staff accounts.
- Cheaters abusing dupe or movement exploits.

Prepare now:

- Write policies for:
  - **Temporary mitigations** (e.g. disable portals if a portal dupe is discovered).
  - **Account actions** (mutes, temp/perm bans, rollback of specific chunks or inventories).
- Ensure:
  - Anticheat and security‑related events are logged with enough context (player, time, world, location) to investigate.
  - Serious security events can generate alerts (e.g. unusually high volume of anticheat flags).

This page should evolve into a security/anticheat **playbook** as you implement tools and gain experience.

