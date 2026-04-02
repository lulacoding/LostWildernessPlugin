---
title: War & Military
description: Military branch specialisations, war mechanics, and combat systems.
tags:
  - ideas
  - clans
  - pvp
  - speculative
status: speculative
phase: phase-3
owner: design
action: needs-design
---
# Lost Wilderness: War & Military Architecture

## 1. The States of Conflict

Wars are not just simple PvP toggles; they are complex, escalating geopolitical states that define the global political landscape of the server. Conflicts dictate the flow of resources, the status of borders, and the survival of empires.

### Types of War
- **The Raid (Undeclared Conflict):** A conflict fought without using the formal `/war declare` command. Raids are small-scale, unsanctioned border skirmishes or looting runs. Because there is no formal declaration, attackers cannot capture chunks, deploy WMDs, or use heavy siege equipment. Raiding a peaceful nation without a formal declaration incurs a constant, heavy Sin-O-Meter penalty.
- **Cyber War (De Facto Conflict):** War does not always start with a formal `/war declare` command. If a Cyber-Tech Hacker successfully brute-forces an enemy's 64-channel Cloud Storage or Iron Dome network via a Decryption Array, the plugin automatically logs the trace route. If the defending clan detects the hack, a **Cyber War** state is instantly triggered by the server. This state is mechanically identical to a Cold War (espionage, embargoes, cyber-retaliation permitted), but it was initiated organically through hacking. The conflict remains a Cyber War until either side formally escalates it to a Hot War via command. When the hacking player exits their terminal, they are prompted with a GUI: *"A Cyber War has been declared. Name this conflict:"* — allowing them to set the war name just as a formal declaree would.
- **Cold War:** A state of extreme, simmering tension. Open PvP and physical base destruction are technically disabled or carry massive global Honor/Sin penalties. However, this state legally permits extreme Espionage, Cyberwarfare (hacking cloud storage, overriding Iron Domes), and devastating economic embargoes. A Cold War can instantly "go hot" if a covert operative is caught or a red line is crossed.
- **Hot War:** Open, brutal physical hostilities. Allows chunk sieging, complete base destruction, unrestricted PvP between factions, and the authorization of heavy artillery and WMD launches. 
- **Proxy War:** Mega-Alliances and Intergovernmental Organizations (IGOs) can secretly fund smaller clans or revolutionary groups to fight their geopolitical rivals. The backing IGO funnels physical resources, tanks, weapons, and "mercenary" players to the proxy faction without formally declaring a global conflict, allowing them to maintain plausible deniability.
- **Civil War:** A Nation or Alliance fracturing into two or more distinct, armed factions fighting for control of the central government and physical Town Hall.
- **Revolutionary War:** An asymmetrical conflict where a disorganized, grassroots civilian militia attempts to overthrow an established, heavily armed state apparatus.
- **World War:** The absolute pinnacle of conflict. A World War state is automatically triggered by the server the moment two **IGOs (Mega-Alliances)** formally declare a Hot War against each other. Because an IGO represents multiple Alliances and Nations, a World War drags a massive percentage of the server's population into the conflict. This state removes certain UN/Assembly restrictions, unlocks the deployment of the most devastating WMDs (like Plutonium Enriched Quasar Cores), and imposes server-wide economic shifts (e.g., global inflation, restricted neutral trade routes).
  - **The Naming Convention:** Standard wars are either given a custom name by the aggressor via command, or default to the `[Attacker]-[Defender] War [I, II, III...]`. However, when two IGOs clash, the server permanently records it in the global ledger as **World War I**, **World War II**, etc., permanently incrementing the global counter regardless of who started it.
  - **The Calendar War Subtitle System:** Every new in-game day, the `CalendarService` checks the active war ledger and broadcasts a subtitle to all relevant players based on the scale of the conflict:
    - **Raid/Standard War:** `⚔️ War Day [XX] — [War Name]` shown only to involved players.
    - **Nuclear War:** `☢️ Nuclear War Day [XX] — [War Name]` shown to all players in the affected server(s).
    - **Interdimensional War:** `🌀 Interdimensional War Day [XX]` shown to all players in affected dimensions.
    - **Galactic War:** `🪐 Galactic War Day [XX]` shown to all players on affected planets.
    - **World War:** `🌍 World War [I/II/III] — Day [XX]` shown only to Earth (Survival/Amplified) players.
    - **Universal War:** `🌌 Universal War Day [XX]` shown to all players across both galaxies.
    - **Multiversal War:** `🌠 Multiversal War Day [XX]` shown to EVERY player across ALL 34 dimensions and 17 planets simultaneously. No exceptions.
  - **War Escalation Tiers:**
    - *Nuclear War:* Triggered when a nuclear or alien WMD is deployed during a Hot War.
    - *Interdimensional War:* Triggered when active combat crosses between Normal and Amplified dimensions.
    - *Galactic War:* Triggered when active combat involves players on more than one planet.
    - *Universal War:* Triggered when combat spans both the Solar System and the Zeratari/Andromeda galaxy.
    - *Multiversal War:* Triggered when a Galactic Empire declares war on another Galactic Empire, pulling all known civilizations into the conflict.
- **Holy War (Crusade/Jihad):** A conflict declared by a recognized Religious Leader (if a Theocracy is established). Ignores standard territorial claims and focuses on the destruction or capture of specific religious monuments or "Heretic" players. Grants temporary buffs to zealots but risks massive international condemnation.
- **Trade War:** A purely economic conflict. Involves weaponized tariffs, blockades of specific trade routes, freezing of offshore Vault accounts, and the targeted sabotage of industrial supply chains.
- **Information War:** Fought purely through the `/propaganda` and `/espionage` systems. Focuses on destroying the target nation's morale, inciting riots, and leaking classified documents (e.g., coordinates of hidden bases) to the server.

### ⚔️ The `/war` Command Suite
*   `/war declare [Nation] [Type] [Casus Belli]` - Formally initiates a conflict. Requires a valid "Casus Belli" (Reason for War) to avoid maximum Sin-O-Meter penalties.
*   `/war hot [Nation]` - Escalates an existing conflict (like a Cold War) into open hostilities.
*   `/war cold [Nation]` - Initiates a Cold War, unlocking advanced espionage and embargo mechanics against the target.
*   `/war civil` - Officially recognizes a rebel faction, splitting the nation's ledger and initiating a Civil War state.
*   `/war proxy [Target Nation] [Funded Faction]` - Funnels untraceable funds and weapons from your national treasury to the funded faction.
*   `/war ceasefire [Nation] [Duration]` - Proposes a temporary halt to hostilities. If accepted, PvP is disabled between the two nations for the duration, but troops remain mobilized.
*   `/war surrender [Terms]` - Capitulates to the enemy. Opens a GUI to negotiate reparations, territory cessions, or vassalage.
*   `/war status [Nation]` - Displays the current war exhaustion, casualty count, resource drain, and active fronts of a conflict.
*   `/war history` - Opens a ledger detailing every war the server has seen, including casualties, duration, and the final treaties signed.
*   `/war stats [War Name]` - Displays full war statistics including total kills per team, blocks destroyed, WMDs deployed, planets contested, economic damage estimates, and top individual performers. Available in-game and on Discord via `!war stats [War Name]`.
*   **Dynamic War Tags:** Wars are not locked to a single type. As a conflict evolves, the server automatically appends new tags to the war entry. A war that starts as a Hot War between two clans, escalates with a nuke, spreads to another planet, and drags in an IGO could carry all of the following tags simultaneously: `[Hot War] [Nuclear War] [Galactic War] [Proxy War]`. All tags are reflected in the Discord Chronicle and the war stats summary.
*   **The Discord Historian (Chronicle System):** Every single war declaration, Cyber War trace, Coup attempt, Quasar Core launch, and Peace Treaty is automatically piped from `PluginV2` into a dedicated read-only `#server-history` channel on Discord.
*   **The War Statistics Engine:** For the duration of every conflict, `PluginV2` records in real-time:
    - Player kills per team (broken down by individual soldier)
    - Blocks destroyed per team (terrain, structures, machines)
    - WMDs deployed (type, coordinates, blast radius)
    - Planets and dimensions contested
    - Resources lost and captured
    - Economic damage index
    All data is queryable on Discord via `!war stats [War Name]`, `!war kills [War Name]`, and `!war leaderboard [War Name]` at any time during or after the conflict. When a war ends, a full post-match summary is auto-posted to the Discord `#war-archives` channel. This creates a permanent, searchable, chronologically accurate historical text of every geopolitical shift, betrayal, and war crime committed on the server.


## 3. Law & Order Under Fire

When a Nation is pushed to the brink of annihilation, standard laws, constitutions, and human rights are the first casualties.

### Martial Law & Tribunals
- **Martial Law:** If a Nation is in a Hot War, Civil War, or severe Revolution, the leader can declare Martial Law. This temporarily suspends the `/constitution`, locks the `/market` to prevent capital flight, imposes forced curfews, and allows Enforcers to kill-on-sight without accumulating Wanted levels.
- **The Military Court:** Under Martial Law, the standard Judicial Council and civilian courts are suspended. Trials are replaced by swift, brutal Military Courts.
- **War Crimes & The International Court:** Using WMDs on civilian centers, executing POWs, or using biological weapons are classified as War Crimes. The International server-wide IGO can issue warrants, leading to global bounties and trials at a neutral International Court.

### 🚨 The `/martial` Command Suite
*   `/martial declare [Reason]` - Suspends the constitution and enables emergency powers for the executive branch.
*   `/martial lift` - Restores civilian rule and standard server law within the nation.
*   `/martial curfew [Start Time] [End Time]` - Anyone caught outside their designated residential chunk during curfew is automatically flagged as hostile to Enforcers.
*   `/martial enforcer [Player]` - Temporarily deputizes a civilian or soldier with Enforcer privileges to maintain order.
*   `/martial execute [Player]` - Bypasses all jail time and court mechanics, instantly executing a subversive element (massive Sin penalty).

### ⚖️ The `/tribunal` Command Suite
*   `/tribunal convene [Target]` - Instantly summons a Military Court for a captured enemy, traitor, or deserter.
*   `/tribunal verdict [Guilty/Innocent]` - Decided by the presiding commanding officer, not a jury.
*   `/tribunal sentence [Execution/Gulag/Dishonorable Discharge]` - Applies the punishment immediately, bypassing standard prison systems and sending the player directly to a high-security forced labor camp or stripping all military ranks and perks.


## 5. Intelligence, WMDs, & Global Intervention

The deadliest weapons are not always physical. Information, propaganda, and the threat of total annihilation govern the late-game meta.

### Psychological & Asymmetric Warfare
- **Propaganda:** Nations can weaponize information to damage enemy morale. Successful propaganda campaigns increase the likelihood of enemy riots, decrease their factory output, and cause enemy soldiers to suffer minor debuffs (e.g., Slowness, Mining Fatigue) representing demoralization.
- **Espionage:** Spies can infiltrate enemy ranks by faking their faction tags, sabotaging factory outputs, stealing blueprints, or turning off the Iron Dome from the inside moments before an attack.

### Global Interventions & Treaties
- **The Doomsday Clock:** Launching a Quasar Core, Nuclear ICBM, or Biological Weapon during any conflict instantly drops the firing Nation's Honor to the absolute minimum and maxes out their Sin-O-Meter. They are flagged globally as a rogue state.
- **IGO Intervention:** If a Nation violates the "Rules of War" (e.g., glassing a neutral City State), the UN-equivalent IGO can authorize a server-wide Coalition War.
- **Peace Treaties & Reparations:** Wars end through mechanized treaties. The victor can force the loser to accept Reparations (e.g., 50% of all mined diamonds automatically transfer to the victor for 30 days), Demilitarization zones, or the transfer of colonial territories.

### 📰 The `/propaganda` Command Suite
*   `/propaganda campaign [Target Nation] [Theme]` - Spends political power to launch a campaign (e.g., "Fear", "Rebellion"). Slowly degrades the target's stability.
*   `/propaganda broadcast [Message]` - Hijacks the target nation's local chat or displays boss-bar messages to their citizens.
*   `/propaganda censor` - A defensive command that spends resources to block incoming psychological warfare, maintaining domestic morale.

### 🕵️ The `/espionage` Command Suite
*   `/espionage infiltrate [Nation]` - Equips a temporary disguise, allowing the spy to bypass border turrets and enter enemy territory undetected.
*   `/espionage sabotage [Coordinates]` - Plants a silent explosive or virus on an enemy factory/Iron Dome, disabling it for a set duration without triggering alarms.
*   `/espionage steal [Target]` - Attempts to copy physical blueprints, steal technology trees, or siphon small amounts of digital currency from a target's vault.
*   `/espionage extract` - Calls for an emergency extraction, teleporting the spy out but leaving behind a massive traceable evidence trail if used in a panic.


## 7. The Sin-O-Meter War Consequences

War is hell, and the server remembers your sins. The **Sin-O-Meter** is deeply integrated into every military action.

- **Justified vs. Unjustified Wars:** Declaring war with a valid Casus Belli (e.g., they attacked you first, they hold your stolen territory) results in minor Sin increases. Declaring an unprovoked Hot War against a peaceful neighbor skyrockets your Sin.
- **Civilian Casualties:** Killing unarmed players (those not in the military faction, with empty inventories, or in designated residential chunks) generates massive Sin. Too many civilian casualties labels the military leader a War Criminal.
- **WMD and Ecological Sin:** Using nuclear weapons, salting the earth, or intentionally destroying biomes generates irreversible, permanent Sin points for the Nation. 
- **The Wages of Sin:** A nation with a maxed-out Sin-O-Meter suffers severe mechanized consequences: global trade embargos are automatically enforced, NPCs refuse to trade with them, holy magic/healing is drastically weakened, and they become a valid target for a zero-penalty Holy War or Coalition Invasion by any other faction on the server.