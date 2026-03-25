# Lost Wilderness: Familiars, Spirit Animals & The Pet System

## 1. The `/pets` Command Suite
- `/pets roster` — GUI showing all living pets, their health, active buffs, and current planet location.
- `/pets call [name]` — Summons a pet to your location if on the same planet. Cross-planet requires physical transport.
- `/pets rename [name]` — Custom name displayed above head in Clan color.
- `/pets graveyard` — Opens the Fallen Companions Ledger (see Section 3).
- `/pets bless [pet]` — Takes the pet to The Lord (an NPC or Sacred Altar) to have their Zodiac and personality revealed.
- `/pets pedigree [pet]` — Opens the full **Breeding Line GUI**. Displays a visual family tree showing every ancestor of the selected pet, their birth/death Calendar dates, their Zodiac signs, their personality traits, and their inherited stats at each generation. Essential for selective breeding programs. A pure-bred 10th-generation War Horse or Spirit Animal lineage is viewable in full here.
- `/pets stance [name] [aggressive/defensive/passive]` — Defines how the pet reacts in combat. Aggressive attacks any hostile mob or enemy player. Defensive only attacks those who damage the owner. Passive never attacks.
- `/pets mount [name]` — Allows mounting of specific large pets (e.g. War Horses, Emus, Drop Bears, Void Stalkers) if the player meets the appropriate Zoologist mastery requirement.
- `/pets formation [circle/line/wedge]` — For players managing multiple familiars, dictates how the pack physically positions itself around the player during travel or combat.
- `/pets harvest [name] [start/stop]` — Directs a tamed utility pet (like a Fairy or Endermite) to begin passively gathering specific nearby items (flowers, chorus fruit) within a small radius and depositing them in the owner's inventory.
- `/pets abandon [name]` — Releases the pet back into the wild. Removes its name tag, Clan coloring, and Spirit Animal status, returning it to standard neutral/hostile mob AI. It does not appear in the Graveyard if it dies after abandonment.

## 2. Tameable Pets & Familiars
Beyond vanilla wolves and cats, players can tame:
- **Drop Bears** (hostile until tamed, Outback biome)
- **Emus** (fast scouts, Southern Hemisphere)
- **Alien Void-Stalkers** (Zeratari planets, invisible in shadow)
- **Bioluminescent Fairies** (forest biomes, heal allies passively)
- **Nether Hounds** (Nether biomes, fire-immune)
- **Ghost Companions** (Haunted Swamps, intangible — can scout through walls)
- **Cosmic Serpents** (Asteroid Belt, drop Stardust when fed)

## 3. The Fallen Companions Ledger (`/pets graveyard`)
Every pet that dies is permanently moved to the Graveyard database. Never deleted.

### Data Tracked Per Pet:
- **Name & Species**
- **Tame Date:** `Year X, Month of [Zodiac], Day XX — HH:MM`
- **Death Date:** `Year X, Month of [Zodiac], Day XX — HH:MM`
- **Cause of Death:** e.g., "Slain by [PlayerName] using an Alien Plasma Rifle" / "Vaporized in Quasar Core blast"
- **Planet of Death**
- **Zodiac & Personality** (if previously revealed by The Lord)

### Physical Memorials:
- From the Graveyard GUI, players can extract a **Memorial Plaque** custom item.
- Placing it on any block spawns a permanent glowing **Text Display Hologram** showing the pet's full biography.
- Clans will build full pet cemeteries outside their city walls.

## 4. The Lord — Zodiac & Personality Revelation
- **The Ritual:** A player brings their tamed pet to **The Lord** (a sacred NPC found in specific holy temples or Celestial biomes). Right-clicking The Lord with the pet nearby triggers a reading.
- **The Result:** The Lord reveals the pet's:
  - *Zodiac Sign* (one of the 13, including Ophiuchus)
  - *Personality Trait* (e.g., "Fierce", "Gentle", "Cunning", "Loyal", "Chaotic")
- **The Lore:** The pet's Zodiac and personality are generated as hidden data when they are first tamed, but remain hidden until blessed. Players who never visit The Lord never know their pet's true nature.

## 5. Spirit Animals & Zodiac Synergy
- **The Link:** If a tamed pet's Zodiac matches the player's own Zodiac, they become that player's **Spirit Animal**.
- **Visual:** Spirit Animals receive a permanent glowing particle aura in the player's Clan color.
- **Mechanical Buffs:**
  - Spirit Animals share the player's AuraSkills buffs.
  - Spells cast on the player (healing, buffs) extend to the Spirit Animal.
  - During a Blood Eclipse, the Spirit Animal enters a frenzy (speed + damage buff).
  - Spirit Animals with "Loyal" personality will physically place themselves between their owner and incoming damage.
- **The Casus Belli:** Killing another player's Spirit Animal in a war zone is a valid, recognized Casus Belli for a formal `/war declare` — documented in the Discord Chronicle with cause listed as "Spirit Animal Slain."
