---
title: Resource Packs
description: Resource pack hosting, serving, and pack update process.
tags:
  - plugins
  - resource-packs
status: partial
phase: phase-2
owner: dev
action: needs-dev
---
# Resource packs

Merged from design bible resource-pack notes.


## logo and assets

## Logo

- **256×256** for **Discord** logo (placeholder then final).
- **64×64** **server icon** (PNG): **barrel + emeralds + “LW”** in gold; centred; **text-less** and **barrel-only** variants for different uses.
- **512×512** for **Discord** (higher detail); **gold blocks** text; **emeralds + barrel + pickaxe** (centred). From graphical-general: *“smooth out the pickaxe, I'd use this as Server-icon.png and the older photo at Discord Icon in higher detail”*; *“version without text, also put the barrel, emeralds and put L W in similar gold writing, in a small 64x64 png icon.”*
- **Scalable** for Discord and server list.

## Advertising

- **Planet Minecraft** advertisement **banner** (GIF).
- **Sites:** Planet Minecraft, minecraftservers.org, Minecraft-MP.com, ServerPact.com, MC-Servers.com, MinecraftBuzz.com.
- **TikTok/Reels/Shorts:** AI text-to-speech over Eclipse/Minecraft footage; **trailer ad** ~1 week before launch + link on launch; **portals sometimes hidden** in ads for discovery.

## Source

- Lost Wilderness - Graphical Design - graphical-general, logo, banner-gif, advertising; Bible section 7.



## serving packs

## Geyser config

1. Upload the **converted Bedrock pack** (ZIP) to a **public URL** (Dropbox, CDN, etc.).
2. In **Geyser config.yml** under **bedrock.resource-packs**:
   - **pack-id:** UUID (e.g. from uuidgenerator.net)
   - **pack-name:** e.g. "CustomBirds" / "Lost Wilderness"
   - **pack-version:** e.g. "1.0.0"
   - **link:** URL to the .zip

## Floodgate

- **Floodgate** lets Bedrock players join **without** a custom client; the pack is **downloaded automatically** when they connect.
- **force-resource-packs** in **Paper server.properties** must be **false** for Bedrock compatibility so decliners aren’t disconnected when the pack fails.
- **DefaultPackListener** (and PackServer **/packs/*.zip**); **Floodgate bypass** if Bedrock pack fails so they can still play.

## Lost Wilderness pack policy

- **Forced** Lost Wilderness pack for Java (kick if declined); **lobby pack** first, then **LW pack** on accept; **xray packs** get **kicked**.

## Source

- lw-resource-pack channel; Bible section 4; Plugin DefaultPackListener.


---
