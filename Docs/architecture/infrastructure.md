---
title: Infrastructure
description: Server infrastructure, network, and hosting architecture.
tags:
  - architecture
  - infrastructure
status: reference
phase: ongoing
owner: dev
action: none
---
# Infrastructure (design intent)

High-level infrastructure notes from the design bible: domains, hardware, platform, proxy topology, sync/data, and worlds/seeds. **For live deployment procedures**, see [Operations](../operations/operations-hub.md).


## Hardware and storage

### OS and environment

- **Ubuntu 22.04**; server is run **terminal-only** (no Windows). From chat: *“If u want to help set me up I'll cred ur mc username in the motd. Ubuntu 22.04”*, *“All terminal”*, *“I want pure 64GB 6000MHz DDR5, not no Windows bs”*, *“nah windows just bloat ware for servers.”*

### Storage (current and previous server)

- **Previous server** reached **1.12TB** (overworld + nether + end combined). *“Last server got to 1.12TB”*, *“The 1.12tb was all 3 combined tho.”*
- **Current plan:** **4TB Samsung 990 Evo** (then **990 Pro** mentioned); *“I have a 4TB Samsung 990 Evo in the server, fastest R/W speeds at the time.”*
- **RAID 0** with **backups on external drive**. *“I don't mind, I'll most likely just get another 4tb and doing a raid setup”*, *“Raid 0 with backups on my external drive although the issue is when it gets to the over 1tb. But that took about 2 years to get to.”*
- **New server** might reach **3TB** depending on exploration. *“new server probs gonna hit 3tb.”*
- Later specs mentioned: **RAID 0 8TB**; **18TB HDD backup** (e.g. 4am and 11pm).

### RAM and CPU (per chat)

- **128GB RAM** total; allocation discussed: **60GB per backend** (Survival and Amplified), **6GB BungeeCord**, **2GB Ubuntu**.
- **64GB 6000MHz DDR5** (pure, no Windows).
- **Ryzen 9 9950X3D** for Survival + Amplified; **Ryzen 7 7800X3D** for Lobby + Creative; second **Ryzen 7 9800X3D** for Devoid, Diablo’s Lair, Aquaria, Heaven’s Tower, 6 elemental temples, starport, void/planets.

### Network and power

- **FTTP 1000/400** (fibre); **static IP** for the server so the VPS can connect reliably.
- **UPS** for safe shutdown (e.g. 10 min + surge); broadcast message discussed: *“Power Outage Detected, UPS Engaged…”*

### Domains and cloud

- **lostwilderness.net** (~$27/year); **cloud.lostwilderness.net** for other services.
- **Dedicated DB server** and **10GbE LAN** were considered for future scaling.

### Source

- Direct Messages (Grovyle187): Ubuntu 22.04, 4TB 990 Evo, 1.12TB previous server, RAID 0, external backup, 3TB estimate, 128GB RAM, 60GB per backend, Ryzen CPUs, FTTP, static IP.
- Bible section 2: RAID 0 8TB, 18TB backup, UPS, lostwilderness.net, cloud, 10GbE.


## Proxy and VPS

### BungeeCord (internal)

- **BungeeCord** is used to **link the two backend servers**: normal (Survival) and Amplified. Players switch with **/server amplified** and **/server** (back to normal). From chat: *“At the moment you can change between the normal and amplified with /server amplified n vice versa.”*
- **No /server from Lobby:** The setup is such that **Lobby cannot be bypassed**—i.e. /server is disabled or restricted so players must go through the Lobby (accept resource pack, queue, etc.) before joining Survival or Amplified.
- **Nothing in BungeeCord plugins folder** except what the portal plugins need: *“nah no plugins in bungee folder but portals plugin uses bungee messenger to communicate.”* So Calendar and portal JARs live only on **each backend server’s plugins/** folder; BungeeCord only runs the proxy and the messenger used by the portal plugins.
- **Bind localhost:** For connection issues, the fix mentioned was to set **bind localhost to false** in the BungeeCord config (it was true in an uploaded file). *“also in the bungee config, need to edit the bind local host to false as its on true cuz i uploaded the file before i changed that, apparently it's the fix for the issue but it hasnt changed much.”*
- **BungeeGuard** is planned for secure backend access so only the proxy can connect to the backends.

### Velocity (VPS)

- **Velocity** runs on a **VPS** (e.g. **OVH ~$14/mo**) for **DDoS protection** and the **first anticheat check**. Traffic flow: **play.lostwilderness.net** → **VPS (Velocity)** → **home** (where BungeeCord and the game servers run). So the public connects to the VPS; the VPS then forwards to the home network.
- **TCPShield** is mentioned for DDoS.
- **Static IP** for the home connection so the VPS can reliably reach the backends.
- VPS specs mentioned: **4 core, 4GB RAM, 80GB SSD, 1000/1000** (symmetric).

### Flow summary

1. Player connects to **play.lostwilderness.net** (or equivalent).
2. **Velocity** on VPS handles the connection (DDoS, first AC).
3. Connection is forwarded to **BungeeCord** at home.
4. BungeeCord sends the player to **Lobby** (or, after auth, to Survival/Amplified).
5. **Survival** and **Amplified** are the two backend Paper servers; BungeeCord switches between them when the player uses the custom portal (or /server during testing).

### Source

- Direct Messages (Grovyle187): /server amplified, BungeeCord bind localhost false, no plugins in Bungee folder, Bungee Messenger for portals.
- Bible sections 2 and 6: Velocity VPS, play.lostwilderness.net, BungeeGuard, Lobby cannot be bypassed.


## Worlds and seeds

### Survival (main overworld)

- **Seed:** **"stephen holbery"** (or numeric **1473117994** on 1.21.5). The seed is intended to **stay non-public**; **anti–seed-cracker plugins** are planned so the world cannot be reverse-engineered from client-side info.
- **World type:** Standard overworld; **Amplified** and **Large Biomes** were both considered for other worlds. *“I might have to flip a coin on Amplified or Large Biomes, or maybe I'll do both, depends on performance”* (with a link to “Minecraft 1.18 - amplified is back!”).

### Amplified Nether and End

- **Amplified Nether** and **Amplified End** use a **separate seed** from Survival (e.g. **"stephenholbery"**). **Multiverse-Core** is used to create those worlds with custom seeds (e.g. `/mv create world_nether nether -seed …`).

### Multiverse-Core

- **Multiverse-Core (MV5 beta)** is used for **world creation** (e.g. new Nether/End with random or custom seeds).
- **Multiverse Nether Portals** links **Amplified Nether** to **Amplified overworld** (so nether travel works within Amplified).

### Build height (datapacks, not plugins)

- Build height changes are done with **datapacks**, not a plugin. **End** and **Nether** extended **down to y -64**; **Nether roof** kept at ~127 with building above; **up to 512** build height possible (e.g. Amplified).
- **Amplified:** **-128 to 512** discussed; **360** gen height mentioned to reduce plateaus. *“Amplified -128 to 512 (e.g. 360 gen to reduce plateaus).”*
- **Modrinth BuildHeightLimit** and Spigot/Paper world build limit threads were referenced; implementation is datapack-based (Nether floor -64, roof extended, End -64).

### Paper world separation

- **Paper** keeps **Nether** and **End** as **separate worlds** (separate world folders). So the 1.12TB from the previous server was the sum of overworld, nether, and end world data.

### Centralised test server

- A **centralised test server** over **SSH** was suggested for development and testing.

### Source

- Direct Messages (Grovyle187): seed “stephen holbery” / 1473117994, Amplified vs Large Biomes, Multiverse create command, BungeeCord test server.
- Bible sections 2 and 6: anti–seed-cracker, separate Amplified nether/end seed, MV5, build height datapacks.

---

## Technical notes (from legacy 06-technical-decisions)

### Calendar sync (technical notes)

- **Only one server (leader)** may **write** the current day to MySQL. The other server(s) **read** and **poll every 30–60 seconds**; if the local day doesn’t match **calendar.current_day** in the DB, they update their internal state. This avoids a **race condition** when players sleep on different backends (e.g. Survival vs Amplified).
- **Config file** is used for **single-server** persistence; **MySQL** for **multi-server**.
- **Date format:** “1st January 1MC” style; **Day 0 = Epoch**; **/timeset** must **not** affect day count or reset the calendar (known bug to fix).
- **CalendarPlugin** config: **mysql.enabled**, **mysql.allow_day_updates**, **host**, **port**, **database**, **username**, **password**. Table **calendar** with **id** (e.g. 1), **current_day**.
- Full logic and SQL in [Systems: Calendar](../systems/calendar.md).

### Plugin placement

- **Calendar** and **portal** JARs go in **each backend server’s plugins/ folder only** (Survival and Amplified each have their own SurvivalCalendarPlugin/AmplifiedCalendarPlugin and SurvivalPortalPlugin/AmplifiedPortalPlugin).
- **Nothing** in **BungeeCord plugins/** except what the **portal plugins** need: they use **Bungee Messenger** to communicate between the two backends. So BungeeCord does **not** run the Calendar or Portal plugin JARs.
- From chat: *“Could you put the whole test server onto Dropbox... amp files in amp plugin etc survival in survival”* and *“nah no plugins in bungee folder but portals plugin uses bungee mesenger to communicate.”*

### Portal design (technical notes)

- **1:1 coordinates** between Survival and Amplified: when you build and light a portal at (x, y, z) on one server, the **return portal** is created at **(x, y, z)** on the other server. No vanilla **8:1** nether-style linking.
- **Paper** vanilla portal linking is **disabled** (Paper.yml) so nether portals don’t create or link to new overworld portals; the **custom** Crying Obsidian portals are the only cross-world portals and they are 1:1.
- **Return portal** is created **automatically** at the same coords on the other server when you first use a new pair; no manual linking step.
- **5 pairs per player**; **minimum spacing** between portals; **air pocket** when the frame is encased so the player doesn’t spawn inside blocks. Portals **only in overworld** (Survival and Amplified overworld).
- Full tables and Bungee Messenger in [Systems: Portals](../systems/portals.md).

### Legacy Plugin roadmap pointer

- **Docs/roadmap** on **GitHub:** **lulacoding/LostWilderness**.
- In-repo **ROADMAP_MASTER.md** (Plugin folder) defines phases for the legacy plugin stack. For current V2 planning, see [Roadmap hub](../roadmap/roadmap-hub.md).
