## Future: Paper Performance & Capacity (100–400 Players)

This page describes how to **tune each Paper backend** (Lobby, Survival, Amplified, and future worlds) so that the network can scale towards hundreds of concurrent players without collapsing TPS.

It’s split into **per‑world tuning**, **global JVM/OS tuning**, and a **repeatable test loop**.

---

### 1. Per‑world / per‑server tuning goals

Each backend has a different role and budget:

- **Lobby** — lightweight hub, minimal entities, tiny simulation budget; must always feel instant.
- **Survival** — heavy, real gameplay, most players; can afford some load but must stay near 20 TPS.
- **Amplified** — similar to Survival but potentially more expensive terrain; might have lower safe capacity.

The configuration knobs are mainly in:

- `server.properties`
- `bukkit.yml`
- `spigot.yml`
- `paper-global.yml` / `paper-world-defaults.yml`

This file should eventually contain **actual snippets** once you’ve converged on stable values.

---

### 2. Lobby server profile

Target characteristics:

- **Max players per lobby:** high (e.g. 200+) but with very low CPU usage.
- Entities: mostly armor stands, a few NPCs, almost no mobs.
- World: static terrain, no farms, no redstone machines.

Recommended baseline:

- `server.properties`:
  - `view-distance=6` (or even `5`)
  - `simulation-distance=4`
  - `difficulty=peaceful`
  - `spawn-protection` big enough to cover your lobby region.
- `paper-world-defaults.yml`:
  - Disable **mob spawning** and **villager breeding** in lobby.
  - Aggressively despawn ambient entities.
- Region plugin (WorldGuard or similar):
  - Deny block breaking/placing for non‑staff.
  - Disable explosions, fire spread, etc.

The lobby should be functionally “cheap” so it never becomes the bottleneck.

---

### 3. Survival / Amplified profile

Target characteristics:

- Most CPU time spent here.
- Mix of exploration, farms, redstone, mobs, and events.
- Expected player load: 100–150 per Survival instance, somewhat fewer on Amplified when terrain is more expensive.

Key levers:

- **Chunk loading and view distance**
  - Start with `view-distance=8`, `simulation-distance=6` and adjust based on tests.
  - Lower these values if CPU becomes the bottleneck before memory.
- **Entity limits**
  - Use Paper’s per‑world mob cap tweaks to keep hostile and passive mobs under control.
  - Consider lowering max cramming, spawn caps per chunk, and hard caps on item entities.
- **Redstone and tick optimisations**
  - Enable Paper’s optimisations for hopper checks, redstone, and piston behaviour as needed.
  - Decide early what kinds of **lag machines** are simply not allowed and write that into rules + enforcement.

Document final chosen values for Survival and Amplified once you’ve load‑tested.

---

### 4. JVM and OS tuning

For each physical/virtual machine:

- **Dedicated JVM flags for Paper** (per server), e.g.:
  - A modern G1GC configuration aimed at low pause times.
  - Fixed heap size per server (e.g. 4–8 GB per big world, less for lobby) rather than “give it everything”.
- **OS‑level settings:**
  - Ensure server is running on SSD/NVMe storage.
  - If Linux: tune `vm.swappiness`, file descriptor limits, and scheduler priority for Java processes.
  - Pin high‑traffic servers to specific cores if needed.

Capture the exact JVM flags and system tunables that work well for you in this file so future machines can copy them.

---

### 5. Testing loop (how to know it works)

To reach “Hypixel‑like” confidence, you need a **repeatable test loop**:

1. **Baseline test**
   - Spin up Survival with your current configs.
   - Bring in 20–30 real players (or scripted bots) doing normal gameplay.
   - Record:
     - TPS, tick time distribution.
     - CPU %, RAM, GC pause frequency.
     - DB query latency.

2. **Raise load**
   - Increase simulated or real players to 60, 80, 100+.
   - Note when TPS starts to fall below 19 consistently or when GC pauses become disruptive.

3. **Adjust and repeat**
   - Lower view/simulation distance, tweak entity limits, tune DB queries if needed.
   - Repeat tests until you’re confident about **safe capacity per server**.

4. **Document safe limits**
   - For each backend, record in this file:
     - “We target up to **X players** with these settings; beyond that, we add another instance.”

This becomes the basis for automated autoscaling later, even if you start with manual scaling at first.

---

### 6. Implementation (ServerUPDATE) — config snippets

**Lobby (lobby-1) — applied in `ServerUPDATE/backends/lobby-1/server.properties`:**

- `view-distance=6`
- `simulation-distance=4`
- `difficulty=peaceful`
- `spawn-protection=16` (or larger to cover lobby region)
- Optional: in `config/paper-world-defaults.yml` disable mob spawning and villager breeding for the lobby world.

**Survival (survival-1) / Amplified (amplified-1) — baseline (tune after load test):**

- `view-distance=8`
- `simulation-distance=6`
- `difficulty=easy` (or `hard` per design)
- Entity/mob caps and redstone optimisations: use Paper world defaults; document “lag machine” policy in server rules.

**JVM flags (example for start.bat / start.sh):**

- Lobby: `java -Xms1G -Xmx2G -XX:+UseG1GC -XX:+ParallelRefProcEnabled -jar paper-*.jar`
- Survival/Amplified: `java -Xms4G -Xmx4G -XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 -jar paper-*.jar`

Use a fixed heap (e.g. 4–8 GB for game backends, 1–2 GB for lobby) rather than unbounded. Adjust after load testing.

**Safe player counts (record after load test):**

| Backend     | Target max players (baseline) | Notes |
|------------|--------------------------------|-------|
| lobby-1    | 200+ (lightweight)             | Tune view/simulation distance if needed. |
| survival-1 | *(record after test)* e.g. 100–120 | Run baseline test at 20–30, then 60, 80, 100; document when TPS stays ≥ 19. |
| amplified-1| *(record after test)* e.g. 80–100  | May be lower than Survival due to terrain cost. |

Update this table after each load test. Beyond the safe limit, add another instance (e.g. survival-2) per multi-instance plan.

