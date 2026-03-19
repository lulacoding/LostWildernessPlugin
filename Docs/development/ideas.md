# Plugin ideas

A living document for feature, architecture, and workflow ideas. Each idea is tracked with a checklist where applicable.

---

## Dynamic command aliases

- [ ] Allow server admins to define command aliases in config.yml
- [ ] Aliases registered at runtime and updateable via reload
- [ ] Aliases respect permission and player-only settings

---

## Cross-server entity and vehicle portals

- [ ] Allow entities (horses, mobs) and vehicles (boats, ice boats) to pass through portals between Survival and Amplified
  - [ ] Entities retain data and status on transfer
  - [ ] Works with ice boat highways
  - [ ] No entity duplication or loss
  - [ ] Handles chunk loading/unloading safely

---

## Easter week and The Redeemer event

- [ ] Easter week event with special mechanics and lore (crop growth, animals, Redeemer trades, hemisphere/season bonuses, etc.)
- See design docs for full Redeemer and Easter-week specification.

---

## Custom build height and world limits

- [ ] Implement and document custom build height limits per dimension (resource/data packs)
- [ ] Nether, Overworld, End limits as per design
- [ ] In-game command to display current build height per dimension
- [ ] Document pack structure for server admins

---

## Custom achievements (in-game events)

- Ideas: [Government], [I survived the Fog], [Get out of my swamp], [Soul Blindness], [Carrots are the Key], [Nether Nuclear], [Hazmat], [Pharmacist], [That wasn't easy], [Undeed Steed], [Life Cycle], [World War], [Together Better], [Banded Strength], [1 Man Band], [Linked], [Lost Wilderness], [New Heights], and others — see design/Bible for full list.

---

## Major event and system ideas

- **Halloween Eclipse:** Eclipse-like darkness, witch huts miniboss, vexes, giant zombies, Nether portal hordes, etc.
- **Personality system:** First-join quiz, progression tiers (Apprentice → Master → Ultimate), custom advancement tab, elemental powers, completion rewards (100% / 200% / 300%).
- **Zodiac system:** Sign by join month/year, buffs, Epochians (Ophiuchus), clan/Easter bonuses.
- **Building and post-game:** Vertical slabs, post-completion immortality and world events.

---

## Diablo wither arena trigger (Nether roof)

- [ ] Intercept wither spawn on Nether roof; show “Diablo set up” sign
- [ ] If player has 6+ wither kills (DB), teleport to special arena for boss fight
- [ ] Kill tracking, arena teleport (e.g. Multiverse), lockout/queue, cleanup, tests

---

*Add new ideas below as needed.*
