# Portals (Survival ↔ Amplified)

**Implementation:** See [implementation-status.md](../../implementation-status.md). Implemented: Crying Obsidian frame, fire, 1:1 coords, /portals, /deleteportals, Bungee Messenger, MySQL, **cross-server entity transfer** (horses, boats, etc. serialized and respawned on the other server). Optional: Happy Ghasts / more mob types via PortalEntitySerializer.

## Concept

Custom portals use **Crying Obsidian** as the frame and **fire** as the ignition method (like a nether portal). They connect **Survival** (normal overworld) to **Amplified** (separate world). When you build and light a portal on one server, the plugin **creates a return portal at the same coordinates** on the other server — **1:1 coords**, no vanilla 8:1 nether-style linking. Each player can have **5 portal pairs** (to/from); there is **minimum spacing** between portals and an **air pocket** so that if the frame is encased in blocks, the player still has space to stand.

## Custom Portal API

- **Custom Portal API** (originally Forge; Spigot/Paper port): **https://github.com/Waterpicker/customportalapi** — *“Developer Api for creating custom portals to any dimension.”* CurseForge link was also sent: *“This allows to make the custom portal frame (Crying Obsidian) and method of ignition, which will just be fire like a nether portal.”*
- The plugin is **open source**; the plan was to **“steal the code and make it only for crying obsidian”** and **update to 1.21.4** (and 1.21.5). *“already is opened source too just need to update to 1.21.4.”*

## BungeeCord and Bungee Messenger

- **BungeeCord** connects the two servers so the portal plugin can send players to the other backend. *“https://www.spigotmc.org/wiki/bungeecord/ To connect the 2 servers and will let the portal plugin work.”*
- **No portal plugins in the BungeeCord plugins folder**; the portal plugins run on **each backend**. They use **Bungee Messenger** to communicate between the two servers (e.g. “player X lit portal at coords Y, create return portal on the other server and send player X there”). *“nah no plugins in bungee folder but portals plugin uses bungee mesenger to communicate.”*

## Separate JARs per backend

- **SurvivalPortalPlugin** — on the Survival server; handles portals in the Survival world.
- **AmplifiedPortalPlugin** — on the Amplified server; handles portals in the Amplified world.
- **Amplified** is the world you reach when you go **through** the portal from Survival. *“so did you want amplified to be the world when you go through portal?”* — *“Yeah.”*
- JARs sent in chat: **survivalportalplugin-1.0.jar**, **AmplifiedPortalPlugin-1.0.jar**. Same MySQL config (e.g. “tunnelsomething” password) for both.

## MySQL tables (from chat)

**Database:** **minecraft**

**pending_portals:**

```sql
CREATE TABLE IF NOT EXISTS pending_portals (
  id INT AUTO_INCREMENT PRIMARY KEY,
  player_uuid VARCHAR(36) NOT NULL,
  portal_name VARCHAR(64) NOT NULL,
  direction VARCHAR(1) NOT NULL,
  amplified_world VARCHAR(255),
  amplified_x DOUBLE,
  amplified_y DOUBLE,
  amplified_z DOUBLE,
  survival_world VARCHAR(255),
  survival_x DOUBLE,
  survival_y DOUBLE,
  survival_z DOUBLE,
  status VARCHAR(20) DEFAULT 'pending',
  FOREIGN KEY (player_uuid) REFERENCES players(uuid)
);
```

**portals:**

```sql
CREATE TABLE IF NOT EXISTS portals (
  portal_id INT AUTO_INCREMENT PRIMARY KEY,
  player_uuid VARCHAR(36) NOT NULL,
  portal_name VARCHAR(64) NOT NULL,
  direction VARCHAR(1) NOT NULL,
  survival_world VARCHAR(255) NOT NULL,
  survival_x DOUBLE NOT NULL,
  survival_y DOUBLE NOT NULL,
  survival_z DOUBLE NOT NULL,
  amplified_world VARCHAR(255) NOT NULL,
  amplified_x DOUBLE NOT NULL,
  amplified_y DOUBLE NOT NULL,
  amplified_z DOUBLE NOT NULL,
  status VARCHAR(20) DEFAULT 'pending',
  UNIQUE (player_uuid, portal_name)
);
```

(If **calendar** table was recreated, the chat said: *“then do DROP TABLE IF EXISTS calendar; then CREATE TABLE calendar ( id INT PRIMARY KEY DEFAULT 1, current_day INT NOT NULL ); then INSERT INTO calendar (id, current_day) VALUES (1, 1);”* and *“amp files in amp plugin etc survival in survival.”*)

## 1:1 coords and return portal

- When you build and light a portal on **Survival** at (x, y, z), the plugin **creates a return portal at the same (x, y, z)** on **Amplified**. If you then run 300 blocks south and make a **new** frame and light it, the return portal is created **300 blocks south** on the other server. *“when you create a portle on whatever server it then logs that portals coords then creates a return portal at the same coords just on the other server.”* *“so like i run 300 blocks south and make a new portal, a return portal is created 300 blocks south.”*
- So **no 8:1 linking**; it’s **1:1** and **linking is done automatically** per pair.

## Air pocket and encased frames

- **Grovyle187:** *“Could u make it have a little air pocket incase the portal is incased by blocks. Like no adjacent blocks. I wonder if that then could be used to glitch outside the world border.”* **lula.pog:** *“yea i could easily do that.”* So the plugin creates an **air pocket** so the player doesn’t spawn inside blocks if the frame is surrounded.

## 5 pairs per person and minimum spacing

- Initially **3 pairs** per person were implemented; **Grovyle187** asked for **5** for longevity. *“ive also added the 3 per person as in 3 pairs of portal to and from and im gonna make it aswell like minimum space between portals”* — *“Could we make that 5? I'm just thinking longevity”* — *“yea no problem.”*
- **Minimum space between portals** is enforced so portals can’t overlap or be too close.

## Portals only in overworld

- Portals are **only creatable in the overworld** (Survival overworld and Amplified overworld), not in Nether/End.

## Commands

- **/portaldelete** — Delete a portal (admin/own).

## Entity passthrough **[Implemented]**

- **Cross-server entity transfer:** When a player crosses a portal, their vehicle (horse, boat, strider, pig, minecart, etc.) is serialized and stored in `portal_transfer_entities`. On the destination server the entity is spawned and the player mounted. See PortalEntitySerializer, PortalEntitySpawner, and [test-guide.md](../../development/test-guide.md). **Grovyle187** wanted to **bring Happy Ghasts** into Amplified for pre-elytra travel: *“Can we have entities also, and make bigger portals? Cuz I want to bring happy ghasts into the Amplified for pre elytra journeys.”*
- **Happy Ghasts** and other types can be added in PortalEntitySerializer.isTransferable() if desired.

## Vanilla portal linking (reminder)

- **Paper** vanilla portal linking (8:1) is **disabled** so that nether portals don’t create new overworld portals; the **custom** portals are 1:1 and don’t rely on that. *“Ours will be fine. Bc they're 1:1.”* *“It's the 8:1 linking radius.”*

## Source

- Direct Messages (Grovyle187): Custom Portal API link, BungeeCord link, Crying Obsidian + fire, 1:1 coords, air pocket, 5 pairs, minimum spacing, Bungee Messenger, no plugins in Bungee folder, SurvivalPortalPlugin / AmplifiedPortalPlugin JARs, MySQL CREATE TABLE for pending_portals and portals, /portaldelete, Happy Ghasts and entity passthrough.
- Bible sections 3 and 6: portal design, plugin placement.
