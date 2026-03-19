# Platform and Versions

Paper versioning, portal linking, and launch dependencies.

---

## Paper

- **Production:** **Paper 1.21.4** was used for initial testing (e.g. Calendar plugin). From the chats: *“I'll give her a run tonight, Paper 1.21.4”* and *“Ight bet, I'll give her a run tonight, Paper 1.21.4.”*
- **Test / experimental:** **Paper 1.21.5** (including **experimental builds**, e.g. **Build #23**) was used for the test server. *“Test server is on experimental paper 1.21.5”*, *“Server will boot on 1.21.6. Doubt there will be much issue.”*
- **Launch target:** **1.21.6** when stable, then moving with **1.21.11** or newest. *“Eventually we will update to 1.21.5 when it's ready and keep dev and testing up until 1.21.6 is ready to go.”*

---

## Vanilla Portal Linking (8:1) Disabled

**Paper** is configured so that **vanilla portal linking** (the 8:1 nether overworld↔nether coordinate mapping) is **disabled**. Reason given in chat: *“Can we absolutely fucking remove portal linking lmao. There's a Paper.yml thing for it but trust me, 3 years tells me it doesn't do the job. Even for nether portals. I want you to enter and exit out of a portal, not have portals that are meant to be linked creating new portals.”* So players always enter and exit through the same portal (or custom portal pairs), and the game does not create new nether portals automatically. The custom Survival↔Amplified portals use **1:1 coords** (see [Portal design](06-technical-decisions/portal-design.md)), so disabling 8:1 avoids confusion and grief (e.g. raiders using nether travel to pop out at unexpected overworld coords).

---

## Force Resource Packs

- **force-resource-packs** is used so the server can push the **Lost Wilderness** pack (and lobby/eclipse packs). **Optional kick** if the player declines is discussed.
- For **Bedrock (Geyser/Floodgate)**: **force-resource-packs** in **server.properties** is set to **false** for Bedrock compatibility so Bedrock clients can connect even if pack loading is janky. A **Floodgate bypass** is used if the Bedrock pack fails.

---

## Paper Built-in Features

- **Anti-xray** is handled by **Paper’s built-in** anti-xray; no separate plugin is required for that.
- **Paper separates Nether and End** (world folders); the previous server’s **1.12TB** was “all 3 combined” (overworld + nether + end).

---

## Dependencies for Launch

From the chats, the stack depends on **Paper**, **BungeeCord**, **Geyser**, **Floodgate**, and (optionally) **anti-cheat**. *“If the whole essentials can be ported over that'll be easy bc then we only need to wait on Bungeecord, Paper, Geyser and Floodgate to update”* and *“this won't be live til the next drop, and when paper and Geyser is ready so most likely like June/July.”* So the **launch window** was tied to Paper and Geyser being ready (e.g. June/July from that conversation). Geyser was noted as “shitting itself in the logs on the test bc it's for 1.21.5 not 1.21.4.”

---

## Source

- Direct Messages (Grovyle187): Paper 1.21.4, 1.21.5 experimental Build #23, 1.21.6 target, portal linking removal, June/July launch, Geyser/Paper/Floodgate wait.
- Bible sections 2 and 6: force-resource-packs, Bedrock false, Paper anti-xray, world separation.
