# Config reference

List of config files used by PluginV2 and what each is for. Details filled as modules are implemented.

| File | Purpose |
|------|--------|
| **core.yml** | Server role, enabled modules list, environment (dev/stage/prod). |
| **db.yml** | Datasources: jdbcUrl, username, password, pool size per logical DB (player, clans, etc.). |
| **player.yml** | Player module: cache TTL, save interval, timeouts. |
| **progression.yml** | Progression: optional list of achievement keys, display names. |
| **skills.yml** | Skills: skill IDs, XP curve formulas, rewards per level. |
| **economy.yml** | Currencies, default balances, caps. |
| **calendar.yml** | Real-time ratio, season lengths, eclipse cycle, leader node. |
| **events.yml** | Event definitions, weights, cadence, cascade rules. |
| **clans.yml** | Clan limits, war rules, head drops. |
| **portals.yml** | Frame rules, cooldowns, caps, destination mapping. |
| **quests.yml** | Quest definitions or path to definition store. |
| **party.yml** | Party max size, invite timeout, cross-server. |

---

## core.yml (example)

```yaml
environment: dev
server-role: survival
enabled-modules:
  - player
  - progression
  # - skills
  # - economy
  # ...
```

---

## db.yml (example)

```yaml
datasources:
  player:
    jdbcUrl: jdbc:mysql://localhost:3306/lw_player
    username: lw
    password: CHANGE_ME
    maximumPoolSize: 20
```

(Add more datasources as modules need them.)
