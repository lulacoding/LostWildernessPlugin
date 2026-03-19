# Custom discs

- **Get Lost** — Easter egg for **full LW + Vanilla** advancements. **Implementation:** DiscRewardService supports `get_lost`; config `discs.get-lost` (material); `/give-disc get_lost [player]` (lw.admin.disc).
- **Grand Theme / Main Theme** — opening cutscene disc. **Implementation:** DiscRewardService supports `main_theme`; config `discs.main-theme`; `/give-disc main_theme [player]`; when 200% advancement exists, call `giveDisc(player, REWARD_MAIN_THEME)`.
- **Amplified overworld** — 2 discs from Amplified eclipses (zombie horses; Strays/Bogged/Husks in Blizzards/Bog/Heatwaves); custom tracks from super_kuromi.
- **Lobby** — rare **zombie horse** drop; **void if sat on** (easter egg).
- **2 per dimension**; **Amplified Nether** = Bastion or Hell Rage/Blue Hell; **Amplified End** = End ship + Ender Quasar jukebox; **advancements** per disc.
- **7 map discs** + **2** for **100%** and **200%** achievements (200% = Main Theme disc). See [test-guide.md](../../development/test-guide.md) for testing disc rewards.

## Source

- Bible section 7; 01-overview goals-and-completion.
