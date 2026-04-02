---
title: Professions & Traits
description: Profession system with trait specialisations and bonuses.
tags:
  - ideas
  - professions
  - speculative
status: speculative
phase: phase-3
owner: design
action: needs-design
---
# Lost Wilderness: Professions & Trait Mastery

## 1. The Trait Progression System
With over 300 traits planned, progression is mapped via a percentage-based mastery system. Players grind specific actions, complete planetary research, and invest skill points to increase their mastery percentage (0% to 100% Ultimate). 

## 2. Profession Deep Dive: The Zoologist
The Zoologist (a branch of the Ranger/Xenobiology tree) specializes in wildlife manipulation, breeding, and the taming of the untameable.

### Mastery Milestones:
- **Novice (0% - 24%): The Beastmaster**
  - *Ability:* Can tame standard wild and passive mobs that are usually untameable in vanilla Minecraft (Cows, Pigs, Goats, Sheep, Foxes, Deer, Kangaroos). 
  - *Mechanic:* These animals gain pet AI, follow the player, can be named, and will be formally recorded in the `/pets graveyard` if they die.

- **Apprentice (25% - 49%): The Demon Whisperer**
  - *Ability:* Unlocks the ability to tame **Nether Mobs**.
  - *Targets:* Hoglins, Striders, Magma Cubes, and custom Nether Hounds.
  - *Mechanic:* These pets offer extreme environmental utility (e.g., riding a tamed Hoglin through a Magma Tide event).

- **Adept (50% - 99%): The Void Walker**
  - *Ability:* Unlocks the ability to tame **End Mobs**.
  - *Targets:* Endermen, Shulker-mites, and custom Void-Stalkers.
  - *Mechanic:* End pets have teleportation tracking (they instantly warp to the player across vast distances) and provide defense during Cosmic Storms.

- **Ultimate (100% / Prestige Gate): The Xenobiologist**
  - *Ability:* Unlocks the taming of **Every non-boss mob in the game, including Alien Mobs.**
  - *Targets:* Cosmic Serpents from the Asteroid Belt, Antimatter anomalies, and apex predators from the 9 planets of the Zeratari Andromeda system.
  - *The Meta:* A clan going to war with a Zoologist who brings a tamed, heavily armored Zeratarian Apex Predator to the frontline will terrify the enemy. 

### Synergy with The Lord & Spirit Animals
The true endgame of the Zoologist is taking these bizarre, terrifying creatures to **The Lord** NPC. 
If an Ultimate Zoologist tames a horrifying Alien Mob from Planet 9, takes it to a temple, and discovers its Zodiac matches their own... that Alien becomes their **Spirit Animal**. It gains a glowing Clan-colored aura, intercepts bullets for them, and goes into a Blood Eclipse frenzy.

## 3. Universal Tamed Mob Breeding System
All tamed mobs, regardless of species, can breed once tamed. `PluginV2` handles the breeding logic server-side, bypassing vanilla restrictions that prevent non-breedable mobs from reproducing.

### Mechanics:
- **The Trigger:** Two tamed mobs of the same species owned by the same player or clan are fed their species-appropriate food (e.g., feeding two tamed Blazes a Blaze Rod + Fire Charge, two Endermen a Chorus Fruit, two Baby Aliens a Zeratari Crystal Shard).
- **The Offspring:** A baby variant spawns. Every mob species has a corresponding baby model (scaled-down via Resource Pack + Display Entity). Baby Blazes, Baby Endermen, Baby Aliens, Baby Squids, Baby Ghasts — all physically present in the world.
- **Inherited Stats:** Baby mobs inherit a blend of their parents' stats. If both parents are high-mastery Spirit Animals with strong Zodiac alignment bonuses, the offspring has a chance to inherit elevated base stats.
- **The Graveyard:** Baby mobs are tracked in the `/pets graveyard` from birth. Their birth date is recorded with Calendar timestamp.
- **Breeding Mastery Gate:**
  - *Passive mobs:* Any player can breed tamed passive mobs.
  - *Nether mobs (25%+ Zoologist):* Baby Blazes, Baby Hoglins, Baby Nether Hounds.
  - *End mobs (50%+ Zoologist):* Baby Endermen, Baby Void Stalkers.
  - *Alien mobs (Ultimate Xenobiologist):* Baby Zeratarian Apex Predators, Baby Cosmic Serpents, Baby Antimatter Anomalies.
- **The Economy:** Baby mobs can be sold on the `/market` as living commodities. A rare baby alien with high inherited stats from two Spirit Animal parents could be the most valuable trade item on the entire server.