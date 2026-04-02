---
title: Audio & Visual
description: Music, sound design, and visual identity planning.
tags:
  - design
  - audio-visual
status: planned
phase: phase-2
owner: design
action: needs-design
---
# Audio visual

Merged design notes (audio, branding, discs).


## custom-discs

- **Get Lost** â€” Easter egg for **full LW + Vanilla** advancements. **Implementation:** DiscRewardService supports `get_lost`; config `discs.get-lost` (material); `/give-disc get_lost [player]` (lw.admin.disc).
- **Grand Theme / Main Theme** â€” opening cutscene disc. **Implementation:** DiscRewardService supports `main_theme`; config `discs.main-theme`; `/give-disc main_theme [player]`; when 200% advancement exists, call `giveDisc(player, REWARD_MAIN_THEME)`.
- **Amplified overworld** â€” 2 discs from Amplified eclipses (zombie horses; Strays/Bogged/Husks in Blizzards/Bog/Heatwaves); custom tracks from super_kuromi.
- **Lobby** â€” rare **zombie horse** drop; **void if sat on** (easter egg).
- **2 per dimension**; **Amplified Nether** = Bastion or Hell Rage/Blue Hell; **Amplified End** = End ship + Ender Quasar jukebox; **advancements** per disc.
- **7 map discs** + **2** for **100%** and **200%** achievements (200% = Main Theme disc). See [test guide](../testing/test-guide.md) for testing disc rewards.

## Source

- Bible section 7; 01-overview goals-and-completion.



## lobby-and-warzone-music

## Lobby

- **Main theme** for **opening cutscene** (PMD “Grand Tale” style); **four seasonal remixes** (winter/spring/summer/autumn); **xylophone remix** for **queue/credits**; **medieval ~2 min** loop; **season or wet/dry**; **winter wind + fire crackles**.
- **First join** = grand theme; then **seasonal by last location**; **credits** = Shinx Ending Theme style.
- **Hemisphere or server season** can drive which lobby track plays.

## Warzone

- When **warring players** are in **100×100** radius: **on foot** = metal, no lyrics; **riding entity** = military rock; **elytra** = phonk/spacey. **100×100** hand2hand/entity radius; **only declared wars** (not raids). **Custom music priority** over vanilla/noteblocks.
- **Interplanetary war theme** for **Galactic Journey** (e.g. Nailgun, Carnage, Combat Zone, Dive Into The Void, Lockdown). Also at **spawn/bed/respawn anchor** when in war.

## Source

- Bible section 7; agent-extracted audio design.


