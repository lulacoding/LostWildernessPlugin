---
title: Industry Arsenal Overview
description: Overview of the Industry Arsenal long-range feature cluster.
tags:
  - ideas
  - endgame
  - speculative
status: speculative
phase: post-story
owner: design
action: needs-design
---
# Lost Wilderness: Master Overview
# Phase 4 & 5 Architecture Blueprint

## 1. The Planetary Engine (17 Worlds x2)
- **Scale:** 17 distinct planets distributed across the Solar System and Andromeda (Zeratari).
- **The Amplified Mirror:** Every single planet has a parallel Amplified version accessible via portals or space travel from Amplified Earth.
- **Structure:** Each planet consists of corresponding layers:
  - **Surface (Normal & Amplified):** Z-axis latitudinal biome generation.
  - **Core:** Deep underground dimension for endgame mining/bosses.
  - **Space Hub:** Shared orbital platforms or transit stations.

## 1b. Exploration Equipment
- **Alien Jetpack (Tier 4 Tech):** A custom chestplate slot item crafted from Zeratari Alien Tech. Provides controlled, fuel-consuming flight in any environment including the Void (Y=-128), deep space (Asteroid Belt), and the End dimension where standard Elytras and rockets are unreliable.
- **Fuel:** Runs on Antimatter Cells (the rarest fuel in the game). A single cell provides ~10 minutes of flight. Refuelling mid-void requires carrying spare cells.
- **Tiered Variants:**
  - *Tier 1 (Diesel Thruster Pack):* Basic vertical boost only. Works above ground. Falls like a rock in zero-G.
  - *Tier 2 (Ion Pack):* Full directional flight. Works in space and low atmosphere.
  - *Tier 3 (Plasma Jetpack):* High-speed, maneuverable. Works anywhere including the void.
  - *Tier 4 (Alien Void Thruster):* The ultimate. Near-infinite directional control. Required for safe Void Base construction at Y=-128. Immune to void damage while equipped.
- **Bedrock Compatibility:** Rendered as a custom armor model via the Server Resource Pack. GeyserMC maps it to a Bedrock geometry model so mobile/console players see the full jetpack visual.

## 2. The Industrial Supply Chain (Factorio/Rust Meta)
- **Resource Processing:** Oil extraction -> Refining into Rubber & Plastics -> Manufacturing Tires & Components. Assembly lines for Jeeps, Boats, Ships, Subs, Planes, and Helicopters.
- **Munitions:** Lead, Brass, Cordite assembly lines for bullet and artillery production.
- **Power Grid:** Coal -> Oil/Diesel -> Nuclear (Uranium -> Plutonium) -> Alien/Antimatter (Zeratari only).

## 3. Weapons of Mass Destruction (WMDs) & Defense
- **The Arsenal:** 40+ types of missiles, torpedoes, and specialized firearms.
- **Nuclear Progression:** Small Uranium tactical devices escalating to the Pu-enriched Quasar Core (2500x2500 blast). Leaves a 150x150 permanent irradiated death zone, poisons oceans, triggers a 1-year extreme weather cycle.
- **Automated Defense:** Iron Dome (C-RAM) and SAM sites connected via the Fiber Optic redstone network to intercept threats via calculated vectors.

## 4. Epochian Cybernetics & Alien Tech
- **64-Channel Fiber Optics:** Single cable carries 64 independent binary channels via 64-bit `long` bitwise math. Every component (Repeaters, Comparators, Power Sources) configurable per-channel via GUI.
- **Cross-Server Wireless (Tachyon Relays):** 64-channel data packets transmitted across the proxy via Redis Pub/Sub. A lever on Earth can execute a command sequence on Mars.
- **The Void Terminal (Alien GUI):** A hyper-advanced interface block. Translates the raw binary from a player's hand-built redstone computer into a clean, readable Chest GUI or custom-font screen, acting as a visual Operating System.
- **Dialog-Based UI & Video Cutscenes:** The server bypasses standard text-based chat commands by utilizing massive, pixel-perfect **Dialog Menus (Tile UIs)**. These custom GUIs open on the player's screen to display everything from fully functional, clickable OS keyboards to fluid, animated video cutscenes (like DOOM or Bad Apple rendered entirely via UI tiles).
- **Alien Teleportation:** Endgame transit gates that instantly beam players or items across planets, bypassing the need for physical spacecraft.

## 5. Information Age & Cyberwarfare
- **Cellular Networks & Smartphones:** Build Cell Towers for coverage. Smartphones open custom app GUIs for teleportation, banking, and clan comms within range.
- **Cloud Matter Storage:** Items digitized into SQL/Redis. Clans access global inventory from any Cloud Terminal across all 17 planets.
- **Hacking & Encryption:** Frequencies can be encrypted. Rival clans build Decryption Arrays to brute-force keys, steal cloud items, or disable enemy Iron Domes.

## 6. The Arcane Wilderness & Dimensional Events
- **Overworld Fantasy:** Haunted Swamps, Bioluminescent Mushroom Forests, procedural Medieval Castles.
- **Supernatural AI:** Fairies heal allied reputations and blind enemies. Ghosts ignore armor, phase through walls, drain Mana/XP.
- **Nether Expansion:** Soul Canyons, Magma Tides. Calendar events: Magma Surges, Piglin Crusades.
- **End Expansion:** Void Archipelagos, Cosmic Storms, Ender Brood multi-dragon swarm events.
- **Amplified-Exclusive Events:** Avalanches, Harpy Swarms at Y=200+, Titan Awakenings inside hollow mountains.
- **The Milestone Engine:** 1,000+ unique advancements tracking everything from first Coal Engine to shooting down a Nuke.

## 7. System Synergy (The Interlocking Web)

> *"This is not a server with features. This is a civilization with physics."*

Lost Wilderness is not a collection of plugins bolted together. It is a single, continuous, causally-linked ecosystem where every system is a live wire touching every other system. Nothing is isolated. Nothing is decorative. Every mechanic is a gear in an enormous machine, and that machine never stops.

What follows is the definitive map of how this world works as one whole — illustrated through cinematic, ground-level examples that trace a single event through the entire system like a shockwave through glass.


### 7.2 The 17-Planet Multiverse × The Factory/Fuel Economy

#### The Cascade That Begins in Space and Ends in Bankruptcy

The 17 planets are not scenery. They are the raw material supply chain of the entire server economy, and the Factory/Fuel system is the engine that converts those raw materials into power, vehicles, and weapons.

**The Dependency Graph:**

- **Earth** holds Oil, Coal, Iron, and Lead — the baseline industrial inputs for Tier 1 production.
- **Mars** holds Red Ore (used in advanced alloys) and Perchlorite deposits used in rocket propellant.
- **Titan** holds vast natural gas seas — the cleanest and most efficient fuel source for mid-game power grids.
- **Europa** holds cryo-ice aquifers — required for Plutonium cooling circuits in nuclear reactors.
- **Zeratari (Andromeda)** holds Antimatter crystals and Alien isotopes — the exclusive endgame fuel source for Antimatter engines and Tier 3+ exotic weapons.

Each of these planets sits at a different Z-axis latitude relative to Earth's orbital plane. The CalendarService tracks planetary alignments in real time. When a planet is in **opposition** (directly opposite the Sun from Earth), Tachyon Relay signal strength to that planet drops by 40%, making cross-planet remote commands unreliable. When a planet is in **conjunction** (passing behind the Sun), direct space travel routes require 3× the fuel to compensate for gravitational drag calculations.

**The Cinematic Example — The Titan Gas Crisis:**

*It's Week 14 of the server. The dominant Alliance, Ironveil, controls the only functioning Oil Refinery on Earth. They've leveraged this monopoly to power every vehicle factory and every Iron Dome on the continent. Three other Alliances depend on Ironveil's refined Diesel to keep their Jeep fleets running.*

*Then Ironveil's scouts discover the Titan methane seas. They commission a Cargo Ship run — a 3-day real-time journey requiring continuous fuel and a Pilot-classed player at the helm. They establish a Titan Fuel Platform (a physical player-built structure drilled into Titan's gas sea floor) and begin pumping Titan LNG back to Earth.*

*The moment Titan LNG enters the market, Diesel's barter value drops 35% in a single server day. The `/stock-market` registers the crash. The three dependent Alliances scramble to renegotiate their supply contracts.*

*But here's the twist: Titan sits at the outer edge of the Solar System, near the polar cold-cap of its orbital Z-axis latitude. The CalendarService rules that in 6 real-world days, Titan will enter its Winter Solstice — a 3-week period where its surface temperature drops to -180°C and the methane seas partially freeze. The Fuel Platform's extraction pumps have a functional temperature floor. If no Mechanic-classed player travels to Titan to install Cryo-Insulation Kits (manufactured on Earth using Rubber from the Oil refinery chain), the entire Titan operation freezes offline.*

*Ironveil's Fuel Platform, their economic dominance, and the political stability of three Alliances all hang on a 3-week window to manufacture and ship cryo parts across the Solar System before winter hits.*

*The Amplified version of Titan — accessible via portal from Amplified Earth — has the same methane seas, but at 10× vertical scale. The Amplified Titan platform, if built, would produce 10× the LNG yield. However, Amplified Titan's polar storms reach Y=320, making construction above the methane layer suicidal without Aerospace Engineer-tier equipment only available at Prestige 3+.*

*One planet's seasonal cycle just restructured the entire server's energy market, sparked a diplomatic crisis, and opened a new frontier for endgame-class players.*


### 7.4 The Biblical Mythos & Zodiac Astronomy × Nuclear Arms Race × WMD Fallout

#### God Is Watching the Arms Race

The divine layer of Lost Wilderness is not a separate story mode. It is a **live monitor** watching what players do to the world, and it responds in kind. The Biblical Mythos and the Zodiac Astronomy system are the universe's immune system — and nuclear weapons are the infection they were designed to fight.

**The Mechanisms:**

- **The Sin-O-Meter** is a global counter, visible to all players, representing the aggregate spiritual weight of every action on the server. Every Quasar Core fired adds a massive Sin delta. Every Iron Dome that saves a civilian settlement subtracts from it. Every natural biome destroyed by strip-mining or nuclear fallout adds passive Sin per server tick.
- **The Zodiac Calendar** determines which of the 13 ruling constellations is active. Each Zodiac carries a different spiritual valence — Scorpio amplifies Sin-related events. Pisces dampens them. Ophiuchus, the 13th sign, is the wildcard: during its month, all Sin-triggered events have double severity.
- **Biblical Event Thresholds:** As the Sin-O-Meter crosses defined thresholds, server-wide Biblical events trigger automatically, with no admin intervention:
  - **Threshold 1 (Wrath):** The Nether's Magma Tides begin surging through all active Nether portals. Lava floods into the Overworld near every active portal for 3 in-game days.
  - **Threshold 2 (Pestilence):** A Plague event spreads across Earth's equatorial biomes. All players in the equatorial zone begin taking passive disease damage unless treated by a Doctor-classed player.
  - **Threshold 3 (War):** Archangel entities (endgame boss-tier AI mobs armed with void-energy weapons) spawn at every IGO capital and begin hunting the highest-Sin-rated player on the server.
  - **Threshold 4 (Apocalypse — The End of Days):** A server-wide 7-day countdown begins. The Epochian Prophecy activates. The sky turns black at all hours. All calendar weather systems are overridden by a permanent Void Storm. Players must complete the Chapter 7 Biblical storyline together or the server enters a permanent "Fallen World" state — all biomes begin converting to Void-corrupted terrain until purged.

**The Cinematic Example — The Arms Race That Woke God:**

*Three IGOs are locked in a nuclear standoff. The Sin-O-Meter sits at Threshold 2 — Pestilence is already active in the equatorial zone. It's the month of Ophiuchus on the Zodiac Calendar. Every Sin event has double severity.*

*The Devoider Cult — players who completed Chapter 4 of the Biblical storyline and sided with the Fallen — have been secretly enriching Plutonium for a Quasar Core. Their Nuclear Physicist-classed players have already Prestiged twice, unlocking the Plutonium-Enriched Quasar Core (2500×2500 blast radius).*

*The moment the Quasar Core fires, the system processes the Sin delta. Under Ophiuchus amplification, the Sin spike is doubled. The Sin-O-Meter crosses Threshold 3.*

*Within minutes, Archangels spawn at every IGO capital simultaneously. These aren't regular boss mobs — they are void-energy wielding entities that ignore armor class, phase through walls like Ghosts, and specifically target the highest-Sin player on the server (which, right now, is the Nuclear Physicist who just fired the nuke). The Archangels begin teleporting across the world, hunting.*

*The 2500×2500 blast zone has eliminated an entire continent's worth of farmland. The ecological collapse from the destroyed biomes adds passive Sin per tick — the Sin-O-Meter is now climbing even without anyone doing anything. Threshold 4 is approaching.*

*Meanwhile, the Zodiac calendar shows that the Blood Eclipse is due in 3 in-game days. During a Blood Eclipse under Ophiuchus — a combination that occurs once every 13 months — the ruling constellation's buff applies to ALL players simultaneously, but during the Fallen World state it inverts: instead of buffs, all players take the debuffs. It becomes a ticking clock.*

*Players who chose the Templar class — the only profession capable of purging radiation and performing Sin-cleansing rituals — are now the most valuable people on the server. They can walk into the 150×150 irradiated death zone without dying. They can perform the Ritual of Atonement at the site of the blast, which subtracts a significant chunk from the Sin-O-Meter — but the ritual requires 3 Templars standing in formation at the center of the irradiated zone for 10 uninterrupted in-game minutes while Archangels try to kill them.*

*Nuclear physics, divine prophecy, the Zodiac calendar, class specialization, and ecological collapse have converged into a single, unforgettable server event.*


### 7.6 The World War / Calendar Engine: The Heartbeat of the Living World

#### The Clock That Runs Everything

If the Fiber Optic network is the nervous system and the Sin-O-Meter is the soul, then the **World War / Calendar Engine** is the heart. It is the single system that every other system references, and it never stops beating.

**What the Calendar Controls:**

Every server tick, the CalendarService is computing and broadcasting state changes to every other system:

| Calendar State | Systems It Directly Affects |
|---|---|
| Current Season (per hemisphere) | Crop yields, mob spawn rates, biome temperatures, vehicle fuel efficiency |
| Current Zodiac Month | Sin-O-Meter event severity multipliers, player/pet buff distributions, Blood Eclipse timing |
| Planetary Alignment (17 planets) | Space travel fuel costs, Tachyon Relay signal strength, alien event probabilities |
| Active Weather Event (per region) | Movement speed, visibility, weapon accuracy, farming, water treatment plant efficiency |
| Active War Declaration | PvP flag changes, Iron Dome arming states, Stock Market volatility index, IGO Senate emergency sessions |
| Nuclear Fallout Zones | Soil fertility, water contamination, radiation debuff areas, ecological Sin generation |
| Day/Night Cycle | Ghost mob activity, Fairy healing rates, Sanity meter drain rate for sleep-deprived players |

**The World War Engine:**

A World War is not just a PvP flag change. It is a system-wide state transition that modifies the behavior of every connected plugin simultaneously:

- **Economic:** The `/stock-market` enters "War Economy" mode. Demand for Ammunition, Fuel, and Medical Supplies spikes. Food and Construction goods see reduced demand. Inflation tracking accelerates.
- **Industrial:** All Factory production lines that produce weapons auto-unlock bonus output rates. Vehicle factories get a 20% production speed increase. Civilian construction halves.
- **Diplomatic:** All active Non-Aggression Pacts between the warring IGOs are automatically suspended. Neutral parties receive notifications via Discord. The `/census` global map updates in real time to show front lines based on active territorial conflicts.
- **Biblical:** The Sin-O-Meter accelerates its passive climb rate during an active World War. War itself is categorized as a Sin-generating state. Every combat death adds a fractional Sin delta.
- **Environmental:** The Calendar weather engine shifts to increase storm frequency in actively contested regions. Heavy weather is partially emergent from the chaos of war — explosions, fires, and nuclear events all feed into the climate model.

**The Cinematic Example — The Day Everything Changed:**

*It's the 6th month of the server. The Zodiac is in Scorpio — the amplification month for conflict and death-related Sin events. A World War between the UPC and the Obsidian Syndicate has been formally declared via the `/gov Senate` war vote. The Calendar marks it as Day 1 of the War.*

*In real time, across all 17 planets:*

*On **Earth**, the UPC's Iron Dome network goes to full alert. Every 64-channel fiber line connecting their SAM batteries to their Void Terminal command center is lit up. Operators are watching for incoming signals. The CalendarService confirms Scorpio is active — all Sin-related events will hit twice as hard.*

*On **Mars**, the Obsidian Syndicate's Tachyon Relay sends an encrypted command to a pre-positioned ICBM silo. The Mars orbital distance is currently in opposition — signal strength is at 60%, meaning the command arrives with a 40% packet loss risk. The Syndicate's Cyber-Tech player has to resend the command twice. In those 8 extra seconds, the UPC's Decryption Array detects the retransmission attempt and begins analyzing the signal pattern.*

*On **Titan**, the fuel platform is automated — it doesn't care about the war. But the war's industrial production bonus means LNG consumption on Earth has jumped 40% as Factories run double shifts. The fuel pipeline from Titan to Earth is running at maximum capacity. One well-placed torpedo hitting the Cargo Ship in transit would cut Earth's fuel supply by 40% overnight.*

*The UPC Diplomat-classed player opens a `/market` trade listing offering emergency food aid to two neutral City States at below-market prices — a political move to lock in neutral support before the Syndicate can approach them. The `/stock-market` shows the food market spiking as war-demand kicks in.*

*A Templar-classed player on a neutral continent watches the Sin-O-Meter. It's at 78% of Threshold 2. With Scorpio active, every death in this war is pushing them toward Pestilence. They draft a message to both IGOs via the Smartphone diplomatic channel, requesting a 48-hour ceasefire to perform a Atonement Ritual.*

*Neither IGO responds. The war is too profitable.*

*On Night 3, a Quasar Core fires. The blast levels 600×600 blocks of the UPC's northern supply depot. The Calendar registers the thermal event. The regional weather locks into a 1-year storm cycle. The Sin-O-Meter crosses Threshold 2. Pestilence spreads across the equatorial zone.*

*The Advancement engine broadcasts three server-wide alerts in rapid succession:*

- *"[Syndicate_Player] has fired the Quasar Core for the first time in this War. Achievement Unlocked: [The First Horseman]."*
- *"The Sin-O-Meter has reached Threshold 2. Pestilence has begun."*
- *"Warning: Scorpio Amplification is active. All Sin-event thresholds have doubled severity. The Blood Eclipse arrives in 4 in-game days."*

*Every player on every planet — the fuel workers on Titan, the arms dealer hiding on Europa, the Templar watching from a mountain, the Astronomer charting the night sky from the Southern Hemisphere — receives these alerts simultaneously. The world has changed for all of them, right now, because of decisions made by a handful of players on a planet they may have never visited.*

*The Calendar advances one tick. The Heart beats. The web holds.*


### 7.8 The Core Truth

There is no background. There is no foreground. There is only the web.

The player who builds a Water Treatment Plant on a neutral continent is part of the same system as the Quasar Core engineer who is about to make every river on that continent toxic. The Zodiac Astronomer watching the Blood Eclipse is reading the same calendar clock as the Nuclear Physicist calculating their next enrichment window. The Arms Dealer who just unlocked Prestige 4 is quietly pulling the Sin-O-Meter toward Threshold 3 with every transaction.

Every player, on every planet, at every Prestige tier, in every profession, is a node in the web. And the web is always, always tightening.

**Lost Wilderness does not have players who "engage with systems." It has citizens who live inside one.**

## 8. The Geographical Engine (Universal Planetary Latitudes)
- **Z-Axis Biome Logic (All Planets):** World generation across all 34 dimensions is bound to the Z-axis.
  - **Equator (Z = 0):** Scorching deserts, jungles, volcanic archipelagos.
  - **Mid Latitudes:** Temperate forests, grasslands, wetlands.
  - **Polar Ice Caps (World Borders):** Absolute zero Ice Spikes and Frozen Peaks at extreme Z limits on every planet.
- **Amplified Compatibility:** Same latitudinal biome layout fed through amplified noise; Polar ice spikes stretch to Y=320.
- **The Outback & Fiordlands (AU/NZ Biomes):** 15 custom biomes in the Deep South of Earth (Outback, Eucalyptus Forests, Great Barrier Reef, Fiordlands).
- **Endemic Custom Mobs:** Kangaroos, Emus, Drop Bears, Crocodiles, Giant Huntsman Spiders, Kiwis, Platypuses.
- **Hemisphere-Dependent Seasons:** CalendarService checks Z-coordinate. Summer North = Winter South.

## 9. Astronomy & The 13 Zodiacs
- **Dynamic Night Sky:** Packet-based Display Entities render custom glowing stars at Y=1000+, completely lag-free.
- **Hemispheric Constellations:** Southern Cross visible in the Deep South; Northern constellations in the Deep North. Physically different skies per hemisphere.
- **The 13 Zodiacs (Including Ophiuchus):** Ruling constellation rotates monthly with the Calendar.
- **The Blood Eclipse:** Moon turns red. Ruling Zodiac constellation stars transition from white to Crimson Red.
- **Zodiac Gameplay Hooks:** Players and pets aligned with the ruling Zodiac receive massive passive buffs during their month and Blood Eclipse.

## 10. The Hardware Foundation (8TB Gen 5 Data Center)
- **World Borders:** 30,000,000 x 30,000,000 vanilla limits preserved for true sandbox scale.
- **Data Protection via Fuel Economy:** All vehicles require physical fuel. Even Nuclear-powered vehicles need Uranium/Plutonium rod refueling (but travel 100x further). Players must build FOBs and pipelines to push deep into the 30M block frontier.
- **Storage Strategy:** 3x 8TB Samsung 9100 Pro Gen 5 NVMes (24TB total) across the 3-node cluster.

## 11. The Evolution of Finance & Global Markets

The economy of Lost Wilderness does not start with money — it *evolves into it*. Finance is not a system handed to players; it is a technology that civilizations invent, develop, and eventually weaponize. Each phase represents a genuine leap in economic sophistication, unlocked through the progression of the server's overall industrial and political maturity.


### Phase 2 — Banks & Fiat Currency (Mid Game)

As clans grow and trade volumes become too large to manage by physically lugging stacks of Uranium or barrels of Oil, the need for a *representation of value* emerges organically. Players solve this problem by building Banks.

- **Physical Bank Construction:** Any Clan or City State can construct a **Bank** — a player-built structure that must meet minimum infrastructure requirements (a secure vault room, a Teller Counter block, and a registered `/bank` anchor sign). The Bank is a physical location in the world that can be raided, bombed, or blockaded.
- **Depositing Resources:** Players physically deposit resources (ores, fuel, refined goods) into the Bank's vault. The Bank's internal ledger (stored in the plugin's database) records the deposit against the player's account.
- **Serialized Bank Cards & Cheques (Custom Items):**
  - Upon deposit, the Bank Teller (an NPC or a player with the `BANKER` role) issues the depositor a serialized **Bank Card** or a **Cheque** — a custom Minecraft item with NBT-encoded metadata representing the debt owed by the bank.
  - **Bank Cards** are persistent and reloadable. They function like a debit card tied to a specific bank's vault. Using the card at any registered terminal of that bank deducts from the vault balance.
  - **Cheques** are single-use physical items inscribed with a specific item type and quantity (e.g., *"Pay Bearer: 64x Refined Steel"*). They can be traded, gifted, or used as collateral without the holder ever touching the underlying resource. When redeemed at the issuing bank, the vault pays out the inscribed goods.
- **Inter-Bank Trust & Exchange Rates:** A Cheque issued by a small, untrustworthy clan bank may be traded at a discount versus one issued by a powerful Alliance-backed bank. Players negotiate exchange rates between different bank currencies, creating a natural foreign exchange market entirely driven by reputation and perceived vault solvency.
- **Bank Runs & Raids:** If a Bank's physical vault is raided and emptied, its issued Cards and Cheques become worthless IOUs. This creates genuine systemic financial risk — holding too much of one clan's paper currency is a strategic liability. Smart players diversify, just like in real finance.
- **No Server-Enforced Fiat:** The server does not create or back any currency. Every "coin" is a player-issued debt instrument backed by real physical goods in a real physical vault. Inflation and deflation are entirely organic consequences of how much players deposit, lend, and spend.


### The `/stock-market` Command

As the `/market` accumulates thousands of trades across the server's history, raw barter data transforms into macroeconomic signal. The `/stock-market` command is the interface that surfaces this intelligence.

- **Real-Time Market Dashboard (GUI):** Opening `/stock-market` presents a custom GUI that aggregates all player-inputted trade data from `/market` listings — both active and historical — into a live economic overview.
- **Moving Averages:** For every commodity that has been traded more than a configurable threshold number of times, the system calculates and displays a moving average of its "exchange rate" (how many units of Commodity B it took to acquire one unit of Commodity A). This reveals whether Iron is becoming more or less valuable relative to Refined Steel over time.
- **Inflation Tracking:** By tracking the volume and relative exchange rates of key baseline commodities (Coal, Iron, Stone) over server time, the `/stock-market` derives a rudimentary inflation index. If it suddenly takes 3x as much Coal to buy a stack of Iron as it did last month, the dashboard flags it as inflationary pressure — likely caused by a shortage, a new industrial demand, or a Bank issuing too many Cheques.
- **Most Heavily Traded Commodities:** A ranked leaderboard of the top traded items by volume and by unique trade count across the entire server, updated in real time as `/market` transactions clear escrow.
- **Per-Planet Breakdowns:** Trade data can be filtered by planet, allowing players to identify arbitrage opportunities — commodities that are cheap on Mars but scarce on Earth — and profit from the logistical gap.
- **Clan Economic Rankings:** A secondary tab ranks Clans and Alliances by their estimated physical GDP (vault sizes, active market listings, and token market caps), giving a real-time geopolitical wealth map of the server.
- **Discord Integration:** Major market events (a commodity's price spiking >50% in 24 hours, a new token launching, a bank-run collapse) are automatically posted to `#stock-market-alerts` on Discord, keeping the broader community informed of economic shifts even when offline.

## 12. Geopolitics, Alliances & Economic Control
- **The Political Hierarchy:** Players -> Clans -> Alliances -> IGOs (Intergovernmental Organizations).
- **Local Economies & Exchange Rates:** `/economy` commands available at Clan, Alliance, and IGO levels to set physical Exchange Rates, Trade Minimums/Maximums.
- **Trade Regulations & Embargoes:** Any governing body can impose trade blocks on rivals or Factions.
- **Physical Taxation:** Trading hubs siphon a physical % of goods into the governing body's Cloud Vault.

## 13a. The Alignment Trigger System (Good vs Evil vs Neutral)

### The Core Philosophy
Every action in the Honor system is **context-aware**. The same action (e.g., launching a Nuke) generates different Honor outcomes depending on the player's current alignment, the target's alignment, and the circumstances.

### Good Players (Positive Honor)
Good players are **not locked out of WMDs or war** — they just need proper justification:
- **The Nuclear Exception:** A Good-aligned player may deploy a Nuke without Honor penalty if:
  - They were already nuked first (retaliation doctrine), OR
  - They have been in an active Hot War for 30+ in-game Calendar years with no ceasefire offered.
- **Justified Nuking of Evil:** Nuking a Corrupted/Destroyer faction base during a declared Holy War or justified conflict grants **Celestial Honor** instead of deducting it.
- **The Infiltrator Bonus (Good Agent):** A Good-aligned player who infiltrates an Evil clan, builds trust, and successfully triggers a `/revolution` that flips the clan to neutral or good alignment earns a **massive Honor jackpot** — one of the largest single Honor gains in the game.
- **Exposing War Crimes:** A Good player who gathers espionage evidence of enemy War Crimes and presents it to the Global Assembly earns Celestial/Redeemed honor.

### Evil Players (Negative Honor)
Evil players mirror the Good system with dark equivalents:
- **Justified Nuking of Good:** Nuking a Celestial/Redeemed faction base during a declared war gives **Corrupted/Destroyers Honor** instead of Sin penalties.
- **The Subversion Bonus (Evil Agent):** An Evil-aligned player who infiltrates a Good clan, poisons its reputation through `/war civil`, fake war crimes, or propaganda, causing the clan's Honor to collapse, earns a **massive Evil Honor jackpot**.
- **Committing War Crimes inside a Good Clan:** Joining a Good military and deliberately triggering civilian casualties or killing allies generates massive Corrupted rep (but will also get them hunted by the clan's own Enforcers).
- **Dark Nuclear Doctrine:** Evil players who have been Holy-War declared upon may retaliate with WMDs without Sin penalty.

### Neutral Players (The Fence Sitters)
Neutral players are not punished but they are deliberately not rewarded for passivity:
- **No Alignment Bonuses:** They do not receive the +20% class synergy bonuses, the Infiltrator jackpots, or the Justified Nuclear exception.
- **No Holy War Drafting:** They are not forcibly drafted when a Holy War triggers.
- **Retaliation Only:** If a Neutral player is attacked first, they may retaliate with equal force without Honor penalty. Escalating beyond equal force tips them toward an alignment.
- **Full Access to Economy:** Neutral players have complete access to `/market`, trading, crafting, pets, and exploration. The server is fully enjoyable without picking a side.
- **The Choice:** Fence-sitting is safe and viable but deliberately leaves massive Honor jackpots, alignment-specific spells, and WMD justifications on the table. The world subtly encourages choosing a path.

## 13a. The Alignment Trigger System (Good vs Evil vs Neutral)

### The Core Philosophy
Every action in the Honor system is **context-aware**. The same action (e.g., launching a Nuke) generates different Honor outcomes depending on the player's current alignment, the target's alignment, and the circumstances.

### The Expanded Honor Scale (-50,000 to +50,000)
The scale is expanded across the full 8-chapter arc. The highest and lowest tiers are locked behind story milestones.

**Good Tiers:**
| Score | Title | Unlocked |
|---|---|---|
| 50,000 | **God** | After Reality Checkerboard |
| 25,000 | **Demigod** | After Edge of Sol |
| 5,000 | **Archangel** | Post-story completionist |
| 4,000 | **Angel** | Chapter 4 requirement |
| 2,500 | **Hero** | |
| 1,500 | **Noble** | |
| 800 | **Honorable** | |

**Neutral Band:**
| Score | Title |
|---|---|
| ±200 | **Neutral** |

**Evil Tiers (Reversible):**
| Score | Title |
|---|---|
| -800 | **Villain** | |
| -1,500 | **Wicked** | |
| -2,500 | **Condemned** *(custom permanent effect)* | |

**Evil Tiers (Permanent):**
| Score | Title | Unlocked |
|---|---|---|
| -4,000 | **Damned** | |
| -5,000 | **Demon** | Chapter 3 Evil (Void Altar) |
| -25,000 | **Abyss Incarnate** | After Edge of Sol |
| -50,000 | **The Devil** | After Reality Checkerboard |


### The Damned State (Irreversible)
When a player drops below **-4000 Honor**, they become **Damned**:
- *Permanent:* The Damned state cannot be reversed through Forgiveness, quests, or sacrifices.
- *The Lord is Closed:* The Lord NPC permanently refuses to speak with a Damned player. Space travel via The Lord's Starport blessing is permanently locked.
- *The Visual:* A permanent, intense void-black aura with red particle cracks. Instantly recognizable server-wide.
- *The Only Redemption Path:* Deep in the Evil storyline, a Damned player reaches a critical story fork where they can **turn on El Diablo** — betraying him before he corrupts new worlds and spreads his reach into the Zeratari galaxy. This act is so significant it bypasses standard forgiveness and is recognized by the Heavens as a divine sacrifice. It does not fully restore Good alignment but lifts the Damned status and sets them to **Condemned (-3000)** — giving them a chance to climb back.
- *The Space Lock:* Damned players cannot access the Starport or The Lord's planetary blessing system. They must rely on player-built rockets, Void Terminals, and their own engineering to reach other planets — fitting the lore that the Heavens have abandoned them.


### The Mirror System (Summary)
Every major Good action has a direct Evil mirror and vice versa. No alignment is locked out of the server's full feature set — they just access it through their own moral lens.

### Good Player Alignment Locks
*   Access to Starport / The Lord's planetary blessings
*   Celestial healing spells and Aegis defensive magic
*   Archangel boss allies during Holy Wars
*   Fairies heal and assist Good players in forests

### Evil Player Alignment Locks
*   Access to Dark Altars and Abyssal magic (Hellfire, Necromancy, Void Tear)
*   Ghosts ignore and assist Damned/Condemned players in Haunted Swamps
*   Wither Rose allegiance rituals
*   The Evil storyline fork (turning on El Diablo) only accessible to Damned players

### Neutral Player Reality
Full access to economy, exploration, pets, crafting, and all 34 dimensions. Miss out on: class alignment bonuses, Infiltrator jackpots, WMD justification exceptions, Holy War buffs. The world is fully enjoyable without choosing a side — but choosing one opens the deepest layers of the game.

## 13. The Biblical Mythos & The Epochian Prophecy
- **The Divine Backdrop:** 7-chapter biblical storyline involving the gods, the Heavens, and the Abyss.
- **Holy Wars & Fallen Angels:** Epochians who side with the Devoider become Fallen Angels wielding Void Tech as demonic artifacts.
- **Technology vs. Divinity:** ICBMs vs. Archangels. Nuclear reactors powering Iron Domes against biblical Leviathans.
- **Player-Driven Apocalypse:** Players themselves trigger the "End of Days" through industrialization, strip-mining, and Quasar Cores.

## 14. Planetary Tech Trees & Job Mastery
- **Professions (Expanding to ~300 total traits):** Starting small (5-10 choices) but expanding massively through Prestige levels and off-world exploration.
  - *Nuclear Physicist, Mechanic/Aerospace Engineer, Doctor/Bio-Chemist, Botanist/Xenobiologist, Soldier/Ballistics Expert, Cyber-Tech/Hacker, Mage/Theologian, Zoologist, Geologist, Pilot, Naval Commander, Alchemist, Architect, Quartermaster, Spy/Assassin, Diplomat...*
- **Planetary Gating:** Profession mastery requires physical off-world exploration and rare alien tech fragments.

## 15. The Party System & Storyline Gating
- **Linear Biblical Progression:** Every player must complete the 7-chapter storyline linearly. No skipping.
- **Party Phasing:** Players cannot enter story instances above their current chapter. Content is phased per player.
- **The Mentor Mechanic:** Completed players can scale down to help others through chapters they are stuck on.

## 16. The Prestige System (10 Tiers)
- **Story Completion Gate:** All players must Prestige at least once to unlock post-story endgame content.
- **Prestige Rewards (Per Profession):**
  - *Prestige 1:* Story completion. Unlocks Quasar Core crafting for Nuclear Physicists. Unlocks endgame magic tiers for Mages.
  - *Prestige 2:* Unlocks Plutonium Enriched Quasar Core. Unlocks Tier 5 Alien spells. Botanists unlock full Xenobiology tree.
  - *Prestige 3:* Unlocks Unstable Elements (massive power gains with catastrophic failure risks). Mechanics can build Antimatter engines.
  - *Prestige 4-10:* Progressively deeper specialization in all ~300 traits. Prestige 10 grants a server-wide legendary title and permanent cosmetic aura.
- **The Risk of Unstable Elements (Prestige 3+):** Crafting with unstable Alien isotopes has a server-calculated probability of catastrophic failure. A failed synthesis could trigger a localized explosion, permanent stat damage, or uncontrolled Void Tear events.

## 17. Hyper-Realistic Survival Mechanics (The Struggle)
- **Thirst & Hydration:** A secondary HUD bar. Players must drink purified water (processed through a campfire or a massive clan Water Treatment Plant). Drinking from swamps or toxic oceans causes severe illness requiring a Doctor.
- **Sleep Deprivation & Sanity:** Skipping sleep doesn't just spawn Phantoms. It drains a hidden Sanity meter, causing players to hear fake creeper hisses, see shadow-mobs that aren't there, and eventually suffer localized blindness.
- **Weight & Encumbrance:** Carrying 36 stacks of raw Uranium physically slows your movement speed. Clans *must* use Jeeps, Trains, or Cargo Ships for heavy logistics, preventing solo players from acting like human cargo containers.
- **Ecological Collapse:** Over-hunting Kangaroos causes an explosive (and hostile) Emu population boom. Strip-mining entire forests without replanting permanently ruins the soil, turning the chunk into a Barren Wasteland that slowly drains crop yields.

## 18. City States, Governments & Global Geopolitics
- **The Ascendancy Curve:** 
  - *Nomads (1-3 players):* Off-the-grid survival.
  - *Clans (4-15 players):* Basic claims, `/clan` chest sharing.
  - *Alliances (Multiple Clans):* Shared radar networks, non-aggression pacts.
  - *City States (Settlements):* Claims with required infrastructure (Town Hall, Market) granting local `/economy` controls.
  - *Empires & IGOs (Intergovernmental Organizations):* Massive, multi-continent or multi-planet super-factions that dictate global law.
- **The Political Compass Engine:** An Empire can structure its government through the `/gov` command suite:
  - *Democracy:* Leadership is determined by automated, server-enforced `/vote` elections every real-world month. The Senate (a list of elected players) must majority-approve war declarations or tax changes.
  - *Autocracy/Dictatorship:* A single Emperor holds absolute power. No elections. Rebellions can only happen via a `/coup` mechanic (internal civil war).
  - *Oligarchy/Corporate Syndicate:* A board of directors (top wealthy players) votes on policy based on their physical resource shares.
- **Law & Order (The Justice System):**
  - Governments can draft and publish physical "Laws" in their Town Hall GUI (e.g., "Murder of neutrals is illegal", "Possession of Alien Tech is contraband").
  - *Police & Bounties:* Governments can assign the "Enforcer" role to players. If a law is broken in their territory, the offender gets a Wanted Level. Enforcers can arrest them (sending them to a player-built physical Jail cell) or kill them to collect a physical bounty from the state vault.
- **The Global Census:** The `/census` command shows real-time server demographics: Population, GDP (physical fuel/ore reserves), dominant government types, and active war declarations across the 17 planets.

## 19. The Cinematic Onboarding (The First Join)
- **The Initial Cutscene (Pre-Lobby):** When a player connects to the Lost Wilderness network for the very first time, they do not spawn in a lobby or a dirt field. Before they even see their own character, `PluginV2` intercepts their connection and forces a full-screen, unskippable, tile-based video cutscene directly into their HUD.
- **The Visual Sequence:**
  - The screen is pitch black. Deep, orchestral music (from the Server Resource Pack) fades in.
  - The blackness gives way to a high-fidelity, pixel-art animation of the cosmos.
  - The camera zooms rapidly past alien nebulas (Zeratari) and burning solar systems, settling on a scarred, burning Earth.
  - A narrator's voice (audio file) or dramatic text crawl reads: *"The Heavens are silent. The Abyss is waking. The Wilderness is yours to claim."*
  - The final title card **"LOST WILDERNESS"** slams onto the screen, shaking the UI.
- **The Awakening:** The cutscene fades to white. The player's UI is restored, and they physically "wake up" (their camera pans up from the ground) inside the Lobby, standing before The Lord or the Faction selection NPCs.
- **The Impact:** From second one, the player understands this is not a standard Minecraft survival server. It establishes the AAA-tier production value, the cosmic scale of the 17 planets, and the underlying biblical holy war before they punch a single tree.
