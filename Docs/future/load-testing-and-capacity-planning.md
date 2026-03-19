## Future: Load Testing & Capacity Planning

To grow safely, you need **evidence‑based capacity numbers**, not guesswork. This page describes how to test Lost Wilderness under load and plan when to add more servers.

---

### 1. Objectives

- Determine:
  - How many players a single Survival/Amplified instance can handle comfortably.
  - How many players a single Lobby instance can handle.
  - Where the first real bottlenecks are (CPU, DB, disk, network).
- Use that data to:
  - Decide when to add `survival-2`, `lobby-2`, etc.
  - Tune configs (`view-distance`, entity caps, DB settings).

---

### 2. Types of load tests

Recommended test types:

1. **Synthetic bot tests**
   - Use scripted clients or load‑testing tools that log in, move around, and perform simple actions.
   - Good for sweeping “how many connections can we handle” tests.
2. **Real‑player stress tests**
   - Organised events with real players on a staging or preview server.
   - Reveal social behaviours and edge cases bots miss (e.g. redstone machines, farms, PvP).

Both are useful; document tools and scripts you use here.

---

### 3. Metrics to capture during tests

During each test, record at least:

- **Per server:**
  - Player count over time.
  - TPS and max tick time.
  - CPU % and per‑core usage.
  - Memory usage (heap and system).
  - Number of loaded chunks.
  - Entity counts (total and by category if available).
- **Database:**
  - Queries per second.
  - Average and p95 query latency.
  - Connection pool utilisation (per server, per bot).

Plot these on a timeline alongside the test script (e.g. “ramp from 50 → 100 players over 15 minutes”).

---

### 4. Test scenarios

Define specific scenarios like:

- **Join burst into lobby**
  - 100 clients connect to proxy in 60 seconds; measure lobby TPS and connection error rates.
- **Survival exploration**
  - 50–100 players spread out in Survival, exploring and loading new chunks.
- **Farm/redstone heavy**
  - Smaller number of players (20–40) but with large farms and redstone contraptions running.
- **War/event scenario**
  - 50+ players concentrated in one region (e.g. PvP arena or boss fight).

For each scenario, write:

- Expected behaviour.
- Actual results and any issues discovered.

---

### 5. Determining safe capacities

Using the collected data:

- Choose thresholds, for example:
  - Target TPS ≥ 19.5 during peak activity.
  - CPU for a given JVM < 80% sustained.
  - DB latency at p95 < 50–100 ms for gameplay‑critical queries.
- For each backend type, record:
  - “At **N players**, we remain within thresholds.”
  - “At **N + Δ players**, we see degradation (TPS drops, lag spikes, DB slowdown).”

Define these as your **safe capacity numbers**:

- e.g. “Survival: 120 safe, lobby: 200 safe.”

---

### 6. Capacity planning rules

Once safe capacities are known:

- Set rules such as:
  - When average concurrent players on Survival exceed **80% of safe capacity** during peak hours, plan to:
    - Add `survival-2`.
    - Update routing to split players.
  - When the lobby regularly exceeds **70–80% of safe capacity**, add an additional lobby instance.

Keep a small table here with:

- Current instance counts.
- Measured safe capacities.
- “Add more when X is true” conditions.

