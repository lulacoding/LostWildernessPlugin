# Portal design

- **1:1 coordinates** between Survival and Amplified: when you build and light a portal at (x, y, z) on one server, the **return portal** is created at **(x, y, z)** on the other server. No vanilla **8:1** nether-style linking.
- **Paper** vanilla portal linking is **disabled** (Paper.yml) so nether portals don’t create or link to new overworld portals; the **custom** Crying Obsidian portals are the only cross-world portals and they are 1:1.
- **Return portal** is created **automatically** at the same coords on the other server when you first use a new pair; no manual linking step.
- **5 pairs per player**; **minimum spacing** between portals; **air pocket** when the frame is encased so the player doesn’t spawn inside blocks. Portals **only in overworld** (Survival and Amplified overworld).
- Full tables and Bungee Messenger in [03-plugins/portals](03-plugins/portals.md).

## Source

- Direct Messages (Grovyle187) 1:1, no 8:1; Bible section 6; 03-plugins portals.
