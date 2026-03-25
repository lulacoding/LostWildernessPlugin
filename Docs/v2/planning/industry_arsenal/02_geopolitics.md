# Lost Wilderness: Geopolitics & Government Architecture

## 1. The Political Hierarchy
The server uses a nested, bottom-up political structure. Every level provides expanding mechanics.

```
Player
  └── Clan (The Nation)
        └── Alliance
              └── IGO (The Mega-Alliance)
                    ├── Solar Empire (Solar System IGOs only)
                    └── Zeratari Empire (Zeratari/Andromeda IGOs only)
                          └── Galactic Empire (requires BOTH Solar Empire + Zeratari Empire)
```

- **Player:** The individual citizen.
- **Clan (The Nation):** The foundational sovereign political unit.
- **Alliance:** A coalition of multiple Clans bound by shared mutual interest.
- **IGO (The Mega-Alliance):** Two or more Alliances merged into a superpower. Physically divisible via secession.
- **Solar Empire:** Multiple IGOs whose member clans are based exclusively on Solar System planets. Restricted to Solar System territory only.
- **Zeratari Empire:** Multiple IGOs whose member clans are based exclusively in the Zeratari/Andromeda system. Restricted to Zeratari territory only. Parallel tier to Solar Empire — neither is above the other.
- **Galactic Empire:** Can ONLY be formed when one Solar Empire AND one Zeratari Empire formally merge. The first cross-galaxy political entity and the pinnacle of player-driven civilization. No single galaxy can form one alone.

## 2. Government Types (`/gov type`)
A Clan (Nation) can define its internal political compass:
- **Anarchy:** No laws. Raiding is legal. (Default for new clans).
- **Chiefdom:** Single leader with absolute power.
- **Monarchy:** Hereditary succession. Heir is promoted automatically if the monarch is offline for 30+ days.
- **Oligarchy/Syndicate:** A board of the wealthiest citizens votes on policy based on resource contribution.
- **Republic:** Elected representatives vote on laws.
- **Democracy:** Universal suffrage. Every citizen votes on laws, taxes, and war.
- **Theocracy:** Ruled by Biblical alignment (Celestial or Corrupted). Laws are dictated by religious mandate.
- **Communist Collective:** All resources go to a state vault. Automated, equal redistribution.
- **Military Junta:** Ruled by the highest-ranking Soldier/Ballistics Expert.

## 3. The Command Suite (Full Political Hierarchy)
All tiers follow the same command pattern as `/clan` and `/alliance`. Each tier adds deeper mechanics.

### `/clan` (The Nation)
`/clan create`, `/clan invite`, `/clan kick`, `/clan rank`, `/clan vault`, `/clan claim`, `/clan map`, `/clan base set`, `/clan flag`, `/clan constitution`, `/clan dissolve`, `/clan audit`, etc.

### `/alliance` (The IGO — Tier 1 Mega-Alliance)
`/alliance create`, `/alliance invite`, `/alliance merge`, `/alliance split`, `/alliance vault`, `/alliance radar`, `/alliance charter`, `/alliance vote`, `/alliance expel`, `/alliance sanctions`, `/alliance mobilize`, `/alliance dissolve`, etc.

### `/igo` (Tier 2 Mega-Alliance — 2+ Alliances)
`/igo create [name]`, `/igo invite [alliance]`, `/igo leave`, `/igo vault`, `/igo charter`, `/igo vote`, `/igo expel [alliance]`, `/igo sanctions [entity]`, `/igo constitution`, `/igo dissolve`

### `/solar-empire` (Multiple IGOs, same Solar System)
`/solar-empire create [name]`, `/solar-empire invite [igo]`, `/solar-empire leave`, `/solar-empire vault`, `/solar-empire charter`, `/solar-empire vote`, `/solar-empire expel [igo]`, `/solar-empire constitution`, `/solar-empire dissolve`

### `/zeratarian-empire` (Solar + Zeratari IGOs combined)
`/zeratarian-empire create [name]`, `/zeratarian-empire invite [solar-empire]`, `/zeratarian-empire leave`, `/zeratarian-empire vault`, `/zeratarian-empire charter`, `/zeratarian-empire vote`, `/zeratarian-empire expel`, `/zeratarian-empire constitution`, `/zeratarian-empire dissolve`

### `/galactic-empire` (Multiple Solar Empires)
`/galactic-empire create [name]`, `/galactic-empire invite [solar-empire]`, `/galactic-empire leave`, `/galactic-empire vault`, `/galactic-empire charter`, `/galactic-empire vote`, `/galactic-empire expel`, `/galactic-empire constitution`, `/galactic-empire dissolve`

> **Note — No Universal Council:** There is no formal `/universal-council` command tier. At the Galactic Empire level, diplomacy, global law, and geopolitical negotiation are handled entirely through Discord channels and in-game chat. The players ARE the council. The Discord Chronicle is the record.

## 4. The Holy War (Sin-O-Meter Trigger)
- **The Auto-Declaration:** When the global Sin-O-Meter reaches a **90%+ disparity** between Good (Celestial/Redeemed) and Evil (Corrupted/Destroyers), the server automatically declares a **Holy War** between the two aligned factions.
- **No Command Required:** Unlike standard wars, neither side initiates this. The server itself declares it based purely on the collective moral state of the playerbase.
- **The Mechanics:**
  - All players with positive Honor are flagged as the **Army of Heaven**. All players with negative Honor are flagged as the **Army of the Abyss**.
  - PvP between the two alignments is enabled server-wide — including in normally protected zones.
  - Celestial Templar and Wildland Ranger classes receive massive combat buffs. Corrupted Cultists and Destroyer Berserkers receive their own dark buffs.
  - Archangel bosses spawn naturally in Holy territory. Demon hordes spawn in Corrupted territory.
- **The Resolution:** The Holy War ends when the Sin-O-Meter drops below 90% disparity (one side converts enough players) or when a server-wide Biblical event triggers the End of Days.
- **The Constitution (`/constitution`):** The immutable foundation of a Nation or IGO. A Constitution outlines the unalienable rights of its citizens, voting thresholds (e.g., requiring a 75% supermajority to alter the Constitution itself), and term limits for elected officials. No standard `/law` can be passed if it violates the Constitution.
- **Drafting Laws (`/law`):** Governments use `/law create` to establish physical rules (e.g., Trade embargoes, Environmental protections).
- **The Senate/Council:** Democracies and Republics use `/senate` to hold automated, tamper-proof elections. Councils (War, Economic, Science) handle specialized approvals.
- **Police & Bounties:** The "Enforcer" role can arrest players with Wanted Levels. Cuffed players are sent to physical, player-built Jails to serve real-time sentences.
- **The Court:** Serious crimes trigger a `/trial`, where a Judicial Council votes on punishments (fines, exile, execution).

## 4. Revolutions & Coups
- **The Coup:** Internal overthrow of a Dictator/Monarch if they are inactive or approval drops.
- **The Rebellion:** An oppressed group within a Nation can declare independence via `/rebellion`, triggering a 72-hour defense war for sovereignty.

## 8. The Live Map System (Multiverse Cartography)
- **Live-Updating Maps:** Building on the V1 map system, every Minecraft Map item automatically live-updates whenever the chunks it covers are loaded or modified, across all 34 dimensions and 17 planets.
- **The Custom Map Frame:** A special custom item frame (and Glow variant) that only accepts Map items. Unlike a standard item frame, this block is recognized by `PluginV2` as a "Map Display." It hooks into the live-update engine and refreshes the rendered map in real time as chunks load and change.
- **Multiversal:** The Map Frame tracks which dimension and planet it was created on. A map made on Mars shows Mars terrain. A map made in Amplified Earth shows the Amplified terrain. All live, all accurate.
- **Held in Hand:** A player holding a live map sees it updating in real time as they move and as the world around them changes — enemy bases being built, craters from Nukes, territory flags being planted, forests being burned.
- **Intelligence Use:** Because the map updates live when chunks are loaded, a strategically placed Map Frame in a hidden outpost near enemy territory becomes a passive intelligence tool — the moment the enemy loads their base chunks, the map renders their layout in real time.
- **Map Detection Blind Spots:** Maps only render within Minecraft's vanilla build height (Y=0 to Y=320). This creates deliberate strategic counter-play:
  - *Sky Bases (Y > 320):* Building or flying above the vanilla build height makes a structure completely invisible to map detection. However, Radar Arrays and Radio Frequency scanners sweep the sky and will detect movement at any altitude.
  - *Deep Underground (below Y=0 / Bedrock layer):* Bases built below Bedrock (via alien tech or endgame excavation) are immune to both map detection AND sonar/frequency scanning, as signals cannot penetrate the Bedrock layer. This makes sub-Bedrock construction the most strategically secure building location in the entire server.
  - *Void Bases (The Most Hidden):* The ultimate stealth installation. Players excavate all the way down to Y=-128 (the absolute bottom of the world) and build directly above the void. Completely undetectable by maps, sonar, and frequency scanning. However, the construction risk is extreme — one misplaced block, a fall, or an explosion during construction sends everything into the void permanently with zero recovery. Building at Y=-128 requires Prestige-level engineering skill, specialized alien gravity boots, and nerves of steel. The payoff is a base that is effectively invisible to every passive detection system in the game.
- **The Loom as a Flag Designer:** Every political entity at every tier of the hierarchy can design an official flag using the vanilla Loom system. The plugin serializes the banner's NBT pattern data and stores it permanently in the database as that entity's official flag.
- **Flags Per Political Tier:**
  - *Personal Flag:* A player's own banner design (shown on their personal camp or nomad claim).
  - *Clan Flag:* The Nation's sovereign banner. Planted on all claimed chunks. Physically knocked down or replaced when territory is captured in a siege.
  - *Alliance Flag:* A shared banner agreed upon by member clans. Displayed at Alliance embassies and joint bases.
  - *IGO Flag:* The Mega-Alliance's official banner. Flies above Capitol buildings and Space Hubs.
  - *Solar Empire Flag:* A grand composite banner representing all member IGOs of a Solar system.
  - *Galactic Empire Flag:* The ultimate physical flag, flying above the most powerful structures in the known universe.
- **Territory Entry Titles:** When any player walks into a claimed chunk, `PluginV2` fires a title/subtitle packet:
  - *Title:* `[Clan Name] Territory`
  - *Subtitle:* Dynamically built from applicable tiers only: `Base Name | Alliance Name | IGO Name | Solar Empire | Galactic Empire`
  - If the clan is solo with no Alliance, only the Base Name shows. Each tier only appears if it exists.
  - This is purely informational — it does not protect the territory in any way.
  - Commands: `/clan base set [name]` to name a base at your current coordinates. `/clan base list` to view all named bases.
- **Physical Placement:** When a player claims a chunk, their Clan flag is automatically placed as a Banner block at the chunk's border. When a territory is captured during a `/siege`, the plugin physically replaces the defending Clan's banner with the attacker's banner in real time. Players watching the siege see the flag change.
- **The War Trophy:** When a war ends, the winning nation can claim the losing nation's flag as a physical banner item dropped into their vault — a permanent war trophy.
- **The Shield Integration:** A player's Clan flag pattern is automatically applied to their equipped Shield, making every player a living standard-bearer of their Nation on the battlefield.