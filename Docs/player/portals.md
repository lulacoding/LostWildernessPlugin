# Portals

## Overview

**Portals** let you build linked gates between the **Survival** and **Amplified** servers. You build a frame out of **Crying Obsidian**, light it, then walk through to travel. Each player has a limited number of portal pairs (e.g. 5); the exact limit is set in the server config.

---

## How to Create a Portal

1. **Build the frame**
   - Use **Crying Obsidian** only for the frame.
   - Frame size: 4 blocks wide × 5 blocks tall (interior 2×3). The frame can be built along the X axis or the Z axis.
   - The interior must be empty (air) when you light it.

2. **Light the portal**
   - Use the correct item (e.g. flint and steel or the configured item) on the **inside** of the frame (one of the empty spaces).
   - If you're under the portal limit, the frame turns into an **End Gateway** and the portal is saved.

3. **Link and travel**
   - The first time you **walk through** that portal, you're sent to the other server (Survival ↔ Amplified).
   - That portal is then **linked**: using it again (or using a paired portal on the other server) sends you back and forth.

---

## Commands

| Command | Description |
| --- | --- |
| **/portals** | Lists your linked portals: name, location, and direction. |
| **/deleteportals &lt;username&gt;** | Admin only. Deletes all portals for that player. |

---

## Limits and Rules

- You can only have a **limited number of portal pairs** (e.g. 5). The server message will say "You can only create X portal pairs" if you're at the limit.
- Portals are stored **per player**; each of your portals links Survival to Amplified (or vice versa depending on which server you're on).
- The server must be behind a **BungeeCord** (or compatible) proxy. Connect to the **proxy** (e.g. port 25565), not directly to Survival or Amplified, or portal travel may not work.

---

## Entity transfer (vehicles and mounts)

- When you cross a portal **on a horse, boat, or other allowed vehicle**, the plugin can transfer that entity to the other server so you arrive still mounted. Supported types include horses, donkeys, boats, striders, pigs, and minecarts. The entity is saved when you step into the portal and restored when you appear on the other server.

---

## Tips

- If the portal doesn't light, check that the frame is **only** Crying Obsidian and the inside is empty (no water, blocks, etc.).
- Use **/portals** to see where your portals are and avoid building too many.
- When you join after creating a portal on one server, the proxy may process the link and send you to the other server when you walk through.
- To test entity transfer: saddle a horse (or use a boat), ride it into the portal, and confirm you appear on the other server still mounted.
