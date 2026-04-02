---
title: Third-Party Plugins
description: Reference list of all third-party plugins and their roles.
tags:
  - plugins
  - reference
status: reference
phase: ongoing
owner: admin
action: none
---
# Other plugins

## Multichat

- **Server-wide chat** and **PMs** across BungeeCord; **RGB colour** support. Link: https://www.spigotmc.org/resources/multichat-full-rgb-colour-support.26204/
- Needed so chat and PMs work when players are on different backends (Survival vs Amplified). *“This gonna be needed for the chat to be server wide, and pms to work between servers. I'll probably need to test this a little bit, including Bedrock players.”*

## Multiverse

- **Multiverse-Core (MV5 beta)** for world creation (e.g. Nether/End with custom seeds).
- **Multiverse Nether Portals** to link **Amplified Nether** to **Amplified overworld**.

## Clearlagg

- **TNT/redstone** clearing; **per-item despawn**; **SaveDeathItem**; **/lagg removechunk**.
- **Separate configs** for **Survival**, **Amplified**, **Creative**.
- To be **verified on 1.21.4+**.

## Harbor (removed)

- **Harbor** (sleep plugin) was **removed**; sleep behaviour is now **vanilla gamerule**. *“Yeah it'll be like the old sleep plugin, which is now a vanilla gamerule feature so I can delete Harbor.”*

## Datapacks

- **Build height** (End/Nether -64, roof, Amplified 512); **custom Lost Wilderness advancement tab** (crying obsidian block); **datapack ver 77** (1.21.6): terms, cutscene, custom menus; **new nether/end** exploration.
- **Achievements plugin** triggers **datapack advancements** (e.g. first Amplified visit); many LW advancements gated behind visiting Amplified.

## Difficulty

- Server **difficulty** is **locked to Hard**.

## Geyser + Floodgate

- **Geyser** and **Floodgate** for **Bedrock**; from a gameplay perspective *“there’s no such thing as Java or Bedrock on Lost Wilderness”* — same features for both clients where possible.

## LuckyPerms

- **LuckyPerms** (or similar) for permissions (referenced in infrastructure/plugins context).

## Source

- Direct Messages (Grovyle187): Multichat link, Harbor removal, BungeeCord.
- Bible sections 2 and 3: Clearlagg, Multiverse, datapacks, difficulty, Geyser/Floodgate.
